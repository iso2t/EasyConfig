package com.iso2t.easyconfig.api.manager;

import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.CommentValueProvider;
import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.files.AbstractFileType;
import com.iso2t.easyconfig.api.files.ConfigNode;
import com.iso2t.easyconfig.api.files.FileTypes;
import com.iso2t.easyconfig.api.files.Json5;
import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.ConfigValue;
import com.iso2t.easyconfig.api.value.NumberRange;
import com.iso2t.easyconfig.api.value.comment.AutoCommentValueProvider;
import com.iso2t.easyconfig.api.value.comment.ArrayValues;
import com.iso2t.easyconfig.api.value.comment.BooleanValues;
import com.iso2t.easyconfig.api.value.comment.CharacterValues;
import com.iso2t.easyconfig.api.value.comment.EnumValues;
import com.iso2t.easyconfig.api.value.comment.ListValues;
import com.iso2t.easyconfig.api.value.comment.NumberValues;
import com.iso2t.easyconfig.api.value.comment.ObjectValues;
import com.iso2t.easyconfig.api.value.comment.StringValues;
import com.iso2t.easyconfig.api.value.wrappers.ArrayValue;
import com.iso2t.easyconfig.api.value.wrappers.BooleanValue;
import com.iso2t.easyconfig.api.value.wrappers.CharacterValue;
import com.iso2t.easyconfig.api.value.wrappers.EnumValue;
import com.iso2t.easyconfig.api.value.wrappers.ListValue;
import com.iso2t.easyconfig.api.value.wrappers.ObjectValue;
import com.iso2t.easyconfig.api.value.wrappers.StringValue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager<T> {
	
    private final Class<T> type;
    private final Path file;
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
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate " + cls.getName(), e);
        }
    }

    private void populate (Object obj, ConfigNode node) throws IOException, IllegalAccessException {
        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String key = f.getName().toLowerCase();
            ConfigNode child = node.get(key);

            if (isNestedConfig(f.getType())) {
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
            elemType = unwrapValueType(declaredElem);
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
        if (isNestedConfig(elemType)) {
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

    private void populateScalarValue (Object obj, Field f, ConfigNode child) throws IllegalAccessException, IOException {
        @SuppressWarnings("unchecked")
        ConfigValue<Object> cv = (ConfigValue<Object>) f.get(obj);
        if (!child.isNull()) {
            try {
                Object v = fileType.readValue(child, inferGenericType(f));
                cv.set(v);
            } catch (IOException | RuntimeException e) {
                return;
            }
        }
    }

    private void populatePlainField (Object obj, Field f, ConfigNode child) throws IllegalAccessException {
        try {
            f.set(obj, fileType.readValue(child, f.getType()));
        } catch (IOException | RuntimeException e) {
            return;
        }
    }

    private Class<?> inferGenericType (Field f) {
        Type gt = f.getGenericType();
        if (gt instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> argClass) {
                return argClass;
            }
        }

        String typeName = f.getType().getSimpleName();
        return switch (typeName) {
			case "ArrayValue" -> f.getType().getComponentType();
            case "BooleanValue" -> Boolean.class;
			case "ByteValue" -> Byte.class;
			case "CharacterValue" -> Character.class;
			case "DoubleValue" -> Double.class;
			case "EnumValue" -> Enum.class;
			case "FloatValue" -> Float.class;
            case "IntegerValue" -> Integer.class;
			case "ListValue" -> java.util.List.class;
			case "LongValue" -> Long.class;
			case "ObjectValue" -> Object.class;
			case "ShortValue" -> Short.class;
            case "StringValue" -> String.class;
            default -> throw new IllegalStateException("Unknown ConfigValue type " + typeName);
        };
    }

    private ConfigNode buildObject (Object obj) throws IllegalAccessException {
        ConfigNode object = ConfigNode.object();

        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            String key = f.getName().toLowerCase();
            object.put(key, buildFieldValue(obj, f), collectComments(f, obj));
        }

        return object;
    }

    private ConfigNode buildFieldValue (Object obj, Field f) throws IllegalAccessException {
        if (isNestedConfig(f.getType())) {
            Object nested = f.get(obj);
            if (nested == null) {
                nested = instantiate(f.getType());
                f.set(obj, nested);
            }
            return buildObject(nested);
        }

        if (ConfigValue.class.isAssignableFrom(f.getType())) {
            @SuppressWarnings("unchecked")
            ConfigValue<Object> cv = (ConfigValue<Object>) f.get(obj);
            return buildValue(cv.get());
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

        if (value != null && isNestedConfig(value.getClass())) {
            return buildObject(value);
        }

        return ConfigNode.value(value);
    }

    private List<String> collectComments (Field f, Object obj) throws IllegalAccessException {
        List<String> comments = new ArrayList<>();

        Comment comment = f.getAnnotation(Comment.class);
        if (comment != null) {
            comments.addAll(List.of(comment.value()));
        }

        if (isNestedConfig(f.getType())) {
            Comment typeComment = f.getType().getAnnotation(Comment.class);
            if (typeComment != null) {
                comments.addAll(List.of(typeComment.value()));
            }
        }

        if (comment != null && comment.values()) {
            appendValueComments(comments, f, obj, comment.provider());
        }

        return comments;
    }

    private void appendValueComments (List<String> comments, Field f, Object obj, Class<? extends CommentValueProvider<?>> providerClass)
            throws IllegalAccessException {
        try {
            Object fieldValue = f.get(obj);

            if (providerClass == AutoCommentValueProvider.class) {
                providerClass = detectProvider(f, fieldValue);
            }

            if (providerClass == null) return;

            @SuppressWarnings("unchecked")
            CommentValueProvider<Object> provider =
                    (CommentValueProvider<Object>) providerClass.getDeclaredConstructor().newInstance();
            Object currentValue = fieldValue instanceof ConfigValue<?> cv ? cv.get() : fieldValue;

            Object toPass = currentValue;
            if (fieldValue != null) {
                Class<?> expectedType = getProviderExpectedType(providerClass);
                if (expectedType.isInstance(fieldValue) && !expectedType.isInstance(currentValue)) {
                    toPass = fieldValue;
                }
            }

            String[] providedComments = provider.getCommentLines(f, toPass);
            if (providedComments != null) {
                for (String providedComment : providedComments) {
                    if (providedComment != null) comments.add(providedComment);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to invoke comment provider " + providerClass.getName(), e);
        }
    }

    private boolean isNestedConfig (Class<?> cls) {
        if (cls == null) return false;
        if (cls.isAnnotationPresent(Config.class)) return true;
        if (cls.isPrimitive() || cls.isEnum() || cls.isArray()) return false;
        if (cls.getName().startsWith("java.") || cls.getName().startsWith("javax.")) return false;
        if (ConfigValue.class.isAssignableFrom(cls)) return false;
        if (Collection.class.isAssignableFrom(cls)) return false;
        try {
            cls.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Class<?> unwrapValueType (Class<?> wrapper) {
        Type sup = wrapper.getGenericSuperclass();
        if (sup instanceof ParameterizedType pt && pt.getRawType() == AbstractValue.class) {
            Type t = pt.getActualTypeArguments()[0];
            if (t instanceof Class<?> c) return c;
        }
        throw new IllegalStateException("Cannot unwrap wrapper type " + wrapper);
    }

    private Class<? extends CommentValueProvider<?>> detectProvider (Field f, Object fieldValue) {
        Class<?> fieldType = f.getType();

        if (NumberRange.class.isAssignableFrom(fieldType)) return NumberValues.class;
        if (fieldValue instanceof NumberRange) return NumberValues.class;

        if (fieldType.isEnum()) return EnumValues.class;
        if (EnumValue.class.isAssignableFrom(fieldType)) return EnumValues.class;
        if (fieldValue instanceof java.lang.Enum) return EnumValues.class;
        if (fieldValue instanceof EnumValue) return EnumValues.class;

        if (BooleanValue.class.isAssignableFrom(fieldType)) return BooleanValues.class;
        if (fieldType == Boolean.class || fieldType == boolean.class) return BooleanValues.class;
        if (fieldValue instanceof Boolean) return BooleanValues.class;

        if (StringValue.class.isAssignableFrom(fieldType)) return StringValues.class;
        if (CharSequence.class.isAssignableFrom(fieldType)) return StringValues.class;
        if (fieldValue instanceof CharSequence) return StringValues.class;

        if (CharacterValue.class.isAssignableFrom(fieldType)) return CharacterValues.class;
        if (fieldType == Character.class || fieldType == char.class) return CharacterValues.class;
        if (fieldValue instanceof Character) return CharacterValues.class;

        if (ArrayValue.class.isAssignableFrom(fieldType)) return ArrayValues.class;
        if (fieldType.isArray()) return ArrayValues.class;
        if (fieldValue != null && fieldValue.getClass().isArray()) return ArrayValues.class;

        if (ListValue.class.isAssignableFrom(fieldType)) return ListValues.class;
        if (Collection.class.isAssignableFrom(fieldType)) return ListValues.class;
        if (fieldValue instanceof Collection<?>) return ListValues.class;

        if (ObjectValue.class.isAssignableFrom(fieldType)) return ObjectValues.class;
        if (fieldValue instanceof ObjectValue<?>) return ObjectValues.class;

        return null;
    }

    private Class<?> getProviderExpectedType (Class<?> cls) {
        for (Type t : cls.getGenericInterfaces()) {
            if (t instanceof ParameterizedType pt && pt.getRawType() == CommentValueProvider.class) {
                return extractClass(pt.getActualTypeArguments()[0]);
            }
        }
        Class<?> superCls = cls.getSuperclass();
        if (superCls != null && superCls != Object.class) {
            return getProviderExpectedType(superCls);
        }
        return Object.class;
    }

    private Class<?> extractClass (Type type) {
        if (type instanceof Class<?> c) return c;
        if (type instanceof ParameterizedType pt) return extractClass(pt.getRawType());
        if (type instanceof WildcardType wt) {
            Type[] bounds = wt.getUpperBounds();
            if (bounds.length > 0) return extractClass(bounds[0]);
        }
        return Object.class;
    }
}
