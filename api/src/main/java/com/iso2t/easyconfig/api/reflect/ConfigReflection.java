package com.iso2t.easyconfig.api.reflect;

import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.CommentValueProvider;
import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.annotations.Ignore;
import com.iso2t.easyconfig.api.value.AbstractValue;
import com.iso2t.easyconfig.api.value.ConfigValue;
import com.iso2t.easyconfig.api.value.NumberRange;
import com.iso2t.easyconfig.api.value.comment.*;
import com.iso2t.easyconfig.api.value.wrappers.*;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ConfigReflection {

	private ConfigReflection () {
	}

	public static List<Field> configFields (Class<?> type) {
		if (type.isAnnotationPresent(Ignore.class)) return List.of();

		List<Field> fields = new ArrayList<>();
		for (Field field : type.getDeclaredFields()) {
			if (field.isSynthetic()) continue;
			if (field.isAnnotationPresent(Ignore.class)) continue;
			if (field.getType().isAnnotationPresent(Ignore.class)) continue;
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) continue;
			field.setAccessible(true);
			fields.add(field);
		}
		return List.copyOf(fields);
	}

	public static boolean isNestedConfig (Class<?> type) {
		if (type == null) return false;
		if (type.isAnnotationPresent(Config.class)) return true;
		if (type.isPrimitive() || type.isEnum() || type.isArray()) return false;
		if (type.getName().startsWith("java.") || type.getName().startsWith("javax.")) return false;
		if (ConfigValue.class.isAssignableFrom(type)) return false;
		if (Collection.class.isAssignableFrom(type)) return false;
		try {
			type.getDeclaredConstructor();
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	public static Class<?> inferValueType (Field field) {
		Type genericType = field.getGenericType();
		if (genericType instanceof ParameterizedType parameterizedType) {
			Type[] arguments = parameterizedType.getActualTypeArguments();
			if (arguments.length == 1) {
				return extractClass(arguments[0]);
			}
		}

		String typeName = field.getType().getSimpleName();
		return switch (typeName) {
			case "ArrayValue" -> Object[].class;
			case "BooleanValue" -> Boolean.class;
			case "ByteValue" -> Byte.class;
			case "CharacterValue" -> Character.class;
			case "ColorValue" -> Integer.class;
			case "DoubleValue" -> Double.class;
			case "EnumValue" -> Enum.class;
			case "FloatValue" -> Float.class;
			case "IntegerValue" -> Integer.class;
			case "ListValue" -> List.class;
			case "LongValue" -> Long.class;
			case "ObjectValue" -> Object.class;
			case "ShortValue" -> Short.class;
			case "StringValue" -> String.class;
			default -> field.getType();
		};
	}

	public static Class<?> unwrapValueType (Class<?> wrapper) {
		Type superType = wrapper.getGenericSuperclass();
		if (superType instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() == AbstractValue.class) {
			return extractClass(parameterizedType.getActualTypeArguments()[0]);
		}
		throw new IllegalStateException("Cannot unwrap wrapper type " + wrapper);
	}

	public static List<String> collectComments (Field field, Object owner) {
		List<String> comments = new ArrayList<>();

		Comment comment = field.getAnnotation(Comment.class);
		if (comment != null) {
			comments.addAll(List.of(comment.value()));
		}

		if (isNestedConfig(field.getType())) {
			Comment typeComment = field.getType().getAnnotation(Comment.class);
			if (typeComment != null) {
				comments.addAll(List.of(typeComment.value()));
			}
		}

		if (comment != null && comment.values()) {
			appendValueComments(comments, field, owner, comment.provider());
		}

		return List.copyOf(comments);
	}

	public static Class<? extends CommentValueProvider<?>> detectProvider (Field field, Object fieldValue) {
		Class<?> fieldType = field.getType();

		if (NumberRange.class.isAssignableFrom(fieldType)) return NumberValues.class;
		if (fieldValue instanceof NumberRange) return NumberValues.class;

		if (ColorValue.class.isAssignableFrom(fieldType)) return ColorValues.class;
		if (fieldValue instanceof ColorValue) return ColorValues.class;

		if (fieldType.isEnum()) return EnumValues.class;
		if (EnumValue.class.isAssignableFrom(fieldType)) return EnumValues.class;
		if (fieldValue instanceof Enum) return EnumValues.class;
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

	public static <T> T instantiate (Class<T> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to instantiate " + type.getName(), e);
		}
	}

	private static void appendValueComments (List<String> comments, Field field, Object owner, Class<? extends CommentValueProvider<?>> providerClass) {
		try {
			Object fieldValue = field.get(owner);

			if (providerClass == AutoCommentValueProvider.class) {
				providerClass = detectProvider(field, fieldValue);
			}

			if (providerClass == null) return;

			@SuppressWarnings("unchecked") CommentValueProvider<Object> provider = (CommentValueProvider<Object>) providerClass.getDeclaredConstructor().newInstance();
			Object currentValue = fieldValue instanceof ConfigValue<?> configValue ? configValue.get() : fieldValue;

			Object toPass = currentValue;
			if (fieldValue != null) {
				Class<?> expectedType = getProviderExpectedType(providerClass);
				if (expectedType.isInstance(fieldValue) && !expectedType.isInstance(currentValue)) {
					toPass = fieldValue;
				}
			}

			String[] providedComments = provider.getCommentLines(field, toPass);
			if (providedComments != null) {
				for (String providedComment : providedComments) {
					if (providedComment != null) comments.add(providedComment);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to invoke comment provider " + providerClass.getName(), e);
		}
	}

	private static Class<?> getProviderExpectedType (Class<?> type) {
		for (Type genericInterface : type.getGenericInterfaces()) {
			if (genericInterface instanceof ParameterizedType parameterizedType && parameterizedType.getRawType() == CommentValueProvider.class) {
				return extractClass(parameterizedType.getActualTypeArguments()[0]);
			}
		}
		Class<?> superType = type.getSuperclass();
		if (superType != null && superType != Object.class) {
			return getProviderExpectedType(superType);
		}
		return Object.class;
	}

	private static Class<?> extractClass (Type type) {
		if (type instanceof Class<?> cls) return cls;
		if (type instanceof ParameterizedType parameterizedType) return extractClass(parameterizedType.getRawType());
		if (type instanceof WildcardType wildcardType) {
			Type[] bounds = wildcardType.getUpperBounds();
			if (bounds.length > 0) return extractClass(bounds[0]);
		}
		return Object.class;
	}

}
