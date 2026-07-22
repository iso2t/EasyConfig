package com.iso2t.easyconfig.api.manager;

import com.iso2t.easyconfig.api.files.AbstractFileType;
import com.iso2t.easyconfig.api.files.ConfigNode;
import com.iso2t.easyconfig.api.files.FileTypes;
import com.iso2t.easyconfig.api.files.Json5;
import com.iso2t.easyconfig.api.metadata.ConfigIntrospector;
import com.iso2t.easyconfig.api.metadata.ConfigSchema;
import com.iso2t.easyconfig.api.reflect.ConfigReflection;
import com.iso2t.easyconfig.api.value.ConfigValue;
import com.iso2t.easyconfig.api.value.SerializedConfigValue;
import com.iso2t.easyconfig.api.value.wrappers.ListValue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConfigManager<T> {

	private final Class<T>         type;
	private final Path             file;
	private final AbstractFileType fileType;

	public ConfigManager (Class<T> type, Path file) {
		this(type, file, Json5.class);
	}

	public ConfigManager (Class<T> type, Path file, FileTypes fileType) {
		this(type, file, fileType.create());
	}

	public ConfigManager (Class<T> type, Path file, Class<? extends AbstractFileType> fileType) {
		this(type, file, instantiateFileType(fileType));
	}

	public ConfigManager (Class<T> type, Path file, AbstractFileType fileType) {
		this.type = type;
		this.file = file;
		this.fileType = fileType;
	}

	/**
	 * Load (or create) the config instance
	 */
	public T load () {
		T cfg = instantiate(type);
		try {
			if (Files.exists(file)) {
				ConfigNode root = fileType.read(file);
				populate(cfg, root);
			}
		} catch (IOException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to load config " + file, e);
		}
		return cfg;
	}

	/**
	 * Load the config, then write it back so missing fields and comments are generated.
	 */
	public T loadAndSave () {
		T cfg = instantiate(type);
		try {
			ConfigNode existingRoot = readExistingRoot();
			if (existingRoot != null) {
				populate(cfg, existingRoot);
			}
			fileType.write(file, merge(existingRoot, buildObject(cfg)));
		} catch (IOException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to load and save config " + file, e);
		}
		return cfg;
	}

	/**
	 * Write out with comments
	 */
	public void save (T config) {
		try {
			fileType.write(file, merge(readExistingRoot(), buildObject(config)));
		} catch (IOException | IllegalAccessException e) {
			throw new IllegalStateException("Failed to save config " + file, e);
		}

	}

	public void loadInto (T config) {
		T loaded = load();
		try {
			copyObject(loaded, config);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Failed to reload config " + file, e);
		}
	}

	public void loadAndSaveInto (T config) {
		loadInto(config);
		save(config);
	}

	public ConfigSchema<T> schema (T config) {
		return ConfigIntrospector.inspect(config);
	}

	public ConfigSchema<T> loadSchema () {
		return schema(load());
	}

	public ConfigSchema<T> loadAndSaveSchema () {
		return schema(loadAndSave());
	}

	public Class<T> type () {
		return type;
	}

	public Path file () {
		return file;
	}

	private ConfigNode readExistingRoot () throws IOException {
		if (!Files.exists(file)) return null;
		ConfigNode root = fileType.read(file);
		if (root == null || !root.isObject()) return null;
		return root;
	}

	private ConfigNode merge (ConfigNode existing, ConfigNode generated) {
		if (existing == null || !existing.isObject() || !generated.isObject()) {
			return generated.copy();
		}

		ConfigNode merged = ConfigNode.object();
		Set<String> generatedKeys = new HashSet<>();

		for (ConfigNode.Entry generatedEntry : generated.entries()) {
			String key = generatedEntry.key();
			generatedKeys.add(key);

			ConfigNode existingValue = existing.get(key);
			ConfigNode mergedValue = mergeValue(existingValue, generatedEntry.value());
			merged.put(key, mergedValue, generatedEntry.comments());
		}

		for (ConfigNode.Entry existingEntry : existing.entries()) {
			if (!generatedKeys.contains(existingEntry.key())) {
				merged.put(existingEntry.key(), existingEntry.value().copy(), existingEntry.comments());
			}
		}

		return merged;
	}

	private ConfigNode mergeValue (ConfigNode existing, ConfigNode generated) {
		if (existing.isObject() && generated.isObject()) {
			return merge(existing, generated);
		}

		return generated.copy();
	}

	private static AbstractFileType instantiateFileType (Class<? extends AbstractFileType> cls) {
		try {
			return cls.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to instantiate file type " + cls.getName(), e);
		}
	}

	private <U> U instantiate (Class<U> cls) {
		return ConfigReflection.instantiate(cls);
	}

	private void populate (Object obj, ConfigNode node) throws IOException, IllegalAccessException {
		for (Field f : ConfigReflection.configFields(obj.getClass())) {
			String key = f.getName().toLowerCase();
			ConfigNode child = node.get(key);

			if (ConfigReflection.isNestedConfig(f.getType())) {
				populateNestedConfig(obj, f, child);
				continue;
			}

			if (ListValue.class.isAssignableFrom(f.getType())) {
				populateListValue(obj, f, child);
				continue;
			}

			if (ConfigValue.class.isAssignableFrom(f.getType())) {
				populateScalarValue(obj, f, child);
				continue;
			}

			if (!child.isNull()) {
				populatePlainField(obj, f, child);
			}
		}
	}

	private void populateNestedConfig (Object obj, Field f, ConfigNode child) throws IllegalAccessException, IOException {
		Object nested = f.get(obj);
		if (nested == null) {
			nested = instantiate(f.getType());
			f.set(obj, nested);
		}
		if (child.isObject()) {
			populate(nested, child);
		}
	}

	private void populateListValue (Object obj, Field f, ConfigNode child) throws IOException, IllegalAccessException {
		if (!child.isArray()) return;

		Type gt = f.getGenericType();
		if (!(gt instanceof ParameterizedType pt)) {
			throw new IOException("Missing generic type for ListValue on field " + f.getName());
		}
		Type arg = pt.getActualTypeArguments()[0];
		if (!(arg instanceof Class<?> declaredElem)) {
			throw new IOException("Cannot handle generic type " + arg + " on field " + f.getName());
		}

		Class<?> elemType = declaredElem;
		if (ConfigValue.class.isAssignableFrom(declaredElem)) {
			elemType = ConfigReflection.unwrapValueType(declaredElem);
		}

		java.util.List<Object> built = new ArrayList<>();
		try {
			for (ConfigNode elNode : child.elements()) {
				built.add(parseListElement(elNode, elemType));
			}
		} catch (IOException | RuntimeException e) {
			return;
		}

		Object raw = f.get(obj);
		if (raw instanceof ListValue<?> lv) {
			@SuppressWarnings("unchecked") ListValue<Object> listVal = (ListValue<Object>) lv;
			listVal.set(built);
		} else {
			throw new IllegalStateException("Field " + f.getName() + " is not a ListValue: " + raw.getClass());
		}
	}

	private Object parseListElement (ConfigNode elNode, Class<?> elemType) throws IOException, IllegalAccessException {
		if (ConfigReflection.isNestedConfig(elemType)) {
			Object element = instantiate(elemType);
			populate(element, elNode);
			return element;
		}

		if (elNode.isValue()) {
			return fileType.readValue(elNode, elemType);
		} else if (elNode.isObject()) {
			ConfigNode valNode = elNode.get("value");
			if (valNode.isValue()) {
				return fileType.readValue(valNode, elemType);
			} else {
				return fileType.convertValue(elNode, elemType);
			}
		} else {
			return fileType.convertValue(elNode, elemType);
		}
	}

	private void populateScalarValue (Object obj, Field f, ConfigNode child) throws IllegalAccessException {
		Object raw = f.get(obj);
		if (!child.isNull()) {
			try {
				if (raw instanceof SerializedConfigValue<?> serializedConfigValue) {
					@SuppressWarnings("unchecked") SerializedConfigValue<Object> writable = (SerializedConfigValue<Object>) serializedConfigValue;
					writable.deserialize(child.rawValue());
					return;
				}

				@SuppressWarnings("unchecked") ConfigValue<Object> cv = (ConfigValue<Object>) raw;
				Object v = fileType.readValue(child, ConfigReflection.inferValueType(f));
				cv.set(v);
			} catch (IOException | RuntimeException _) {
			}
		}
	}

	private void populatePlainField (Object obj, Field f, ConfigNode child) throws IllegalAccessException {
		try {
			f.set(obj, fileType.readValue(child, f.getType()));
		} catch (IOException | RuntimeException _) {
		}
	}

	private ConfigNode buildObject (Object obj) throws IllegalAccessException {
		ConfigNode object = ConfigNode.object();

		for (Field f : ConfigReflection.configFields(obj.getClass())) {
			String key = f.getName().toLowerCase();
			object.put(key, buildFieldValue(obj, f), ConfigReflection.collectComments(f, obj));
		}

		return object;
	}

	private ConfigNode buildFieldValue (Object obj, Field f) throws IllegalAccessException {
		if (ConfigReflection.isNestedConfig(f.getType())) {
			Object nested = f.get(obj);
			if (nested == null) {
				nested = instantiate(f.getType());
				f.set(obj, nested);
			}
			return buildObject(nested);
		}

		if (ConfigValue.class.isAssignableFrom(f.getType())) {
			Object raw = f.get(obj);
			if (raw instanceof SerializedConfigValue<?> serializedConfigValue) {
				return buildValue(serializedConfigValue.serialized());
			}
			if (raw instanceof ConfigValue<?> configValue) {
				return buildValue(configValue.get());
			}
		}

		return buildValue(f.get(obj));
	}

	private ConfigNode buildValue (Object value) throws IllegalAccessException {
		if (value instanceof Collection<?> collection) {
			ConfigNode array = ConfigNode.array();
			for (Object element : collection) {
				array.add(buildValue(element));
			}
			return array;
		}

		if (value != null && ConfigReflection.isNestedConfig(value.getClass())) {
			return buildObject(value);
		}

		return ConfigNode.value(value);
	}

	private void copyObject (Object source, Object target) throws IllegalAccessException {
		for (Field field : ConfigReflection.configFields(source.getClass())) {
			Object sourceValue = field.get(source);

			if (ConfigReflection.isNestedConfig(field.getType())) {
				Object targetValue = field.get(target);
				if (targetValue == null) {
					targetValue = instantiate(field.getType());
					field.set(target, targetValue);
				}
				copyObject(sourceValue, targetValue);
				continue;
			}

			Object targetValue = field.get(target);
			if (sourceValue instanceof ConfigValue<?> sourceConfigValue && targetValue instanceof ConfigValue<?> targetConfigValue) {
				@SuppressWarnings("unchecked") ConfigValue<Object> writable = (ConfigValue<Object>) targetConfigValue;
				writable.set(sourceConfigValue.get());
				continue;
			}

			field.set(target, sourceValue);
		}
	}
}
