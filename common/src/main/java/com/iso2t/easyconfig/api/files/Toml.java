package com.iso2t.easyconfig.api.files;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.toml.TomlFactory;
import com.iso2t.easyconfig.api.files.properties.ISupportsComments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a file type implementation for reading and writing TOML configuration files.
 * This class provides functionality to parse TOML files into configuration nodes, convert
 * values between nodes and object representations, and write configuration nodes back
 * to TOML files, ensuring compliance with the TOML specification.
 * <p>
 * The class extends {@code AbstractFileType} and implements {@code ISupportsComments},
 * making it suitable for file type handling in systems supporting hierarchical
 * configurations with inline and multiline comments.
 *
 * @since 26.1.0.0
 * @author iso2t
 */
public class Toml extends AbstractFileType implements ISupportsComments {
	private static final Pattern BARE_KEY = Pattern.compile("[A-Za-z0-9_-]+");

	private final ObjectMapper mapper;
	private final ObjectMapper valueMapper;

	public Toml () {
		super("toml");
		mapper = new ObjectMapper(new TomlFactory());
		valueMapper = new ObjectMapper();
	}

	@Override
	public ConfigNode read (Path file) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			return toConfigNode(mapper.readTree(reader));
		}
	}

	@Override
	public void write (Path file, ConfigNode root) throws IOException {
		Path parent = file.getParent();
		if (parent != null && Files.notExists(parent)) {
			Files.createDirectories(parent);
		}

		try (BufferedWriter writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			writeRoot(root, writer);
		}
	}

	@Override
	public <T> T readValue (ConfigNode node, Class<T> type) {
		return convertValue(node, type);
	}

	@Override
	public <T> T convertValue (ConfigNode node, Class<T> type) {
		return mapper.convertValue(node.rawValue(), type);
	}

	private ConfigNode toConfigNode (JsonNode node) {
		if (node == null || node.isNull()) return ConfigNode.nullNode();

		if (node.isObject()) {
			ConfigNode object = ConfigNode.object();
			for (Map.Entry<String, JsonNode> field : node.properties()) {
				object.put(field.getKey(), toConfigNode(field.getValue()));
			}
			return object;
		}

		if (node.isArray()) {
			ConfigNode array = ConfigNode.array();
			for (JsonNode element : node) {
				array.add(toConfigNode(element));
			}
			return array;
		}

		if (node.isTextual()) return ConfigNode.value(node.textValue());
		if (node.isNumber()) return ConfigNode.value(node.numberValue());
		if (node.isBoolean()) return ConfigNode.value(node.booleanValue());

		return ConfigNode.value(mapper.convertValue(node, Object.class));
	}

	private void writeRoot (ConfigNode root, BufferedWriter writer) throws IOException {
		if (!root.isObject()) {
			throw new IOException("TOML root must be an object");
		}

		writeValues(root, writer);
		writeTables(root, writer, List.of());
	}

	private void writeValues (ConfigNode object, BufferedWriter writer) throws IOException {
		boolean wroteValue = false;
		for (ConfigNode.Entry entry : object.entries()) {
			if (entry.value().isObject()) continue;
			if (entry.value().isNull()) continue;

			writeComments(entry, writer);
			writer.write(writeKey(entry.key()));
			writer.write(" = ");
			writeInlineNode(entry.value(), writer);
			writer.newLine();
			wroteValue = true;
		}

		if (wroteValue && hasObjectEntries(object)) {
			writer.newLine();
		}
	}

	private void writeTables (ConfigNode object, BufferedWriter writer, List<String> path) throws IOException {
		Iterator<ConfigNode.Entry> entries = object.entries().iterator();
		while (entries.hasNext()) {
			ConfigNode.Entry entry = entries.next();
			if (!entry.value().isObject()) continue;

			List<String> tablePath = append(path, entry.key());
			writeComments(entry, writer);
			writer.write("[");
			writer.write(writePath(tablePath));
			writer.write("]");
			writer.newLine();
			writeValues(entry.value(), writer);
			writeTables(entry.value(), writer, tablePath);

			if (entries.hasNext()) {
				writer.newLine();
			}
		}
	}

	private void writeInlineNode (ConfigNode node, BufferedWriter writer) throws IOException {
		switch (node.type()) {
			case ARRAY -> writeArray(node, writer);
			case VALUE -> writeScalar(node.value(), writer);
			case OBJECT -> writeInlineTable(node, writer);
			case NULL -> throw new IOException("TOML does not support null values");
		}
	}

	private void writeArray (ConfigNode node, BufferedWriter writer) throws IOException {
		writer.write("[");

		Iterator<ConfigNode> elements = node.elements().iterator();
		while (elements.hasNext()) {
			writeInlineNode(elements.next(), writer);
			if (elements.hasNext()) writer.write(", ");
		}

		writer.write("]");
	}

	private void writeInlineTable (ConfigNode node, BufferedWriter writer) throws IOException {
		writer.write("{ ");

		Iterator<ConfigNode.Entry> entries = node.entries().iterator();
		while (entries.hasNext()) {
			ConfigNode.Entry entry = entries.next();
			if (entry.value().isNull()) continue;

			writer.write(writeKey(entry.key()));
			writer.write(" = ");
			writeInlineNode(entry.value(), writer);
			if (entries.hasNext()) writer.write(", ");
		}

		writer.write(" }");
	}

	private void writeScalar (Object value, BufferedWriter writer) throws IOException {
		if (value == null) {
			throw new IOException("TOML does not support null values");
		}

		if (value instanceof Boolean || value instanceof Number) {
			writer.write(value.toString());
			return;
		}

		if (value instanceof Character || value instanceof CharSequence || value instanceof Enum<?>) {
			writer.write(quote(value.toString()));
			return;
		}

		if (value instanceof Iterable<?> iterable) {
			writeIterable(iterable, writer);
			return;
		}

		if (value.getClass().isArray()) {
			writeJavaArray(value, writer);
			return;
		}

		writeInlineNode(toConfigNode(valueMapper.valueToTree(value)), writer);
	}

	private void writeIterable (Iterable<?> iterable, BufferedWriter writer) throws IOException {
		writer.write("[");

		Iterator<?> iterator = iterable.iterator();
		while (iterator.hasNext()) {
			writeInlineNode(ConfigNode.value(iterator.next()), writer);
			if (iterator.hasNext()) writer.write(", ");
		}

		writer.write("]");
	}

	private void writeJavaArray (Object array, BufferedWriter writer) throws IOException {
		writer.write("[");

		for (int i = 0; i < Array.getLength(array); i++) {
			writeInlineNode(ConfigNode.value(Array.get(array, i)), writer);
			if (i < Array.getLength(array) - 1) writer.write(", ");
		}

		writer.write("]");
	}

	private void writeComments (ConfigNode.Entry entry, BufferedWriter writer) throws IOException {
		for (String comment : entry.comments()) {
			for (String line : comment.split("\\R", -1)) {
				writer.write("# ");
				writer.write(line);
				writer.newLine();
			}
		}
	}

	private boolean hasObjectEntries (ConfigNode object) {
		for (ConfigNode.Entry entry : object.entries()) {
			if (entry.value().isObject()) return true;
		}
		return false;
	}

	private List<String> append (List<String> path, String key) {
		java.util.ArrayList<String> copy = new java.util.ArrayList<>(path);
		copy.add(key);
		return List.copyOf(copy);
	}

	private String writePath (List<String> path) {
		return path.stream().map(this::writeKey).collect(java.util.stream.Collectors.joining("."));
	}

	private String writeKey (String key) {
		return BARE_KEY.matcher(key).matches() ? key : quote(key);
	}

	private String quote (String value) {
		StringBuilder result = new StringBuilder(value.length() + 2);
		result.append('"');
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '\\' -> result.append("\\\\");
				case '"' -> result.append("\\\"");
				case '\b' -> result.append("\\b");
				case '\t' -> result.append("\\t");
				case '\n' -> result.append("\\n");
				case '\f' -> result.append("\\f");
				case '\r' -> result.append("\\r");
				default -> {
					if (c < 0x20) {
						result.append(String.format("\\u%04X", (int) c));
					} else {
						result.append(c);
					}
				}
			}
		}
		result.append('"');
		return result.toString();
	}
}
