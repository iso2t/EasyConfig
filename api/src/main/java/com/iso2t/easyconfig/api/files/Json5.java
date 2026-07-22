package com.iso2t.easyconfig.api.files;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.iso2t.easyconfig.api.files.properties.ISupportsComments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a JSON5 file type, which extends the functionality of JSON by allowing
 * additional features such as comments, unquoted property names, and trailing commas.
 * This class provides methods for reading, writing, and converting JSON5 configuration files
 * while preserving or generating comments in the output.
 * <p>
 * The JSON5 file type is implemented using Jackson's {@link ObjectMapper} and a customized
 * {@link JsonFactory} that enables JSON5-specific parsing and serialization options.
 *
 * @since 26.1.0.0
 * @author iso2t
 */
public class Json5 extends AbstractFileType implements ISupportsComments {
	private static final Pattern UNQUOTED_KEY = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");

	private final ObjectMapper mapper;

	public Json5 () {
		super("json5");

		JsonFactory factory = JsonFactory.builder().enable(JsonReadFeature.ALLOW_JAVA_COMMENTS).enable(JsonReadFeature.ALLOW_SINGLE_QUOTES).enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES).enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS).enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS).enable(JsonReadFeature.ALLOW_TRAILING_COMMA).build();

		mapper = new ObjectMapper(factory).enable(SerializationFeature.INDENT_OUTPUT);
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
			writeNode(root, writer, 0);
			writer.newLine();
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

	private void writeNode (ConfigNode node, BufferedWriter writer, int indent) throws IOException {
		switch (node.type()) {
			case OBJECT -> writeObject(node, writer, indent);
			case ARRAY -> writeArray(node, writer, indent);
			case VALUE -> writer.write(mapper.writeValueAsString(node.value()));
			case NULL -> writer.write("null");
		}
	}

	private void writeObject (ConfigNode node, BufferedWriter writer, int indent) throws IOException {
		writer.write("{");
		writer.newLine();

		Iterator<ConfigNode.Entry> entries = node.entries().iterator();
		while (entries.hasNext()) {
			ConfigNode.Entry entry = entries.next();
			writeComments(entry, writer, indent + 1);
			indent(writer, indent + 1);
			writeKey(entry.key(), writer);
			writeNode(entry.value(), writer, indent + 1);

			if (entries.hasNext()) writer.write(",");
			writer.newLine();
		}

		indent(writer, indent);
		writer.write("}");
	}

	private void writeArray (ConfigNode node, BufferedWriter writer, int indent) throws IOException {
		writer.write("[");
		if (!node.elements().isEmpty()) {
			writer.newLine();
			Iterator<ConfigNode> elements = node.elements().iterator();
			while (elements.hasNext()) {
				ConfigNode element = elements.next();
				indent(writer, indent + 1);
				writeNode(element, writer, indent + 1);

				if (elements.hasNext()) writer.write(",");
				writer.newLine();
			}
			indent(writer, indent);
		}
		writer.write("]");
	}

	private void writeComments (ConfigNode.Entry entry, BufferedWriter writer, int indent) throws IOException {
		for (String comment : entry.comments()) {
			for (String line : comment.split("\\R", -1)) {
				indent(writer, indent);
				writer.write("// " + line);
				writer.newLine();
			}
		}
	}

	private void writeKey (String key, BufferedWriter writer) throws IOException {
		if (UNQUOTED_KEY.matcher(key).matches()) {
			writer.write(key);
		} else {
			writer.write(mapper.writeValueAsString(key));
		}
		writer.write(": ");
	}

	private void indent (BufferedWriter writer, int levels) throws IOException {
		for (int i = 0; i < levels; i++) writer.write("    ");
	}
}
