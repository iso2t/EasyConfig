package com.iso2t.easyconfig.api.files;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a configuration node that can store hierarchical data in the
 * form of objects, arrays, values, or nulls. ConfigNode supports complex
 * structures and data manipulation by providing methods to interact with
 * its contents.
 * <p>
 * ConfigNode instances are immutable, except for nodes of types OBJECT
 * and ARRAY, which can have their contents modified dynamically.
 */
public final class ConfigNode {

	/**
	 * Represents the type of a configuration node.
	 * <p>
	 * This enumeration defines four possible types for a {@code ConfigNode}:
	 * <ul>
	 * <li>{@code OBJECT}: Indicates that the node represents a structured object with key-value pairs.</li>
	 * <li>{@code ARRAY}: Indicates that the node represents an ordered collection of elements.</li>
	 * <li>{@code VALUE}: Indicates that the node represents a single scalar value (e.g., String, Number, Boolean).</li>
	 * <li>{@code NULL}: Indicates that the node has a null or absent value.</li>
	 * </ul>
	 * <p>
	 * Each type provides contextual information about the structure or data stored in a {@code ConfigNode}
	 * and can be used to guide logic or behavior when interacting with configuration data.
	 */
	public enum Type {
		OBJECT,
		ARRAY,
		VALUE,
		NULL
	}

	private static final ConfigNode NULL = new ConfigNode(Type.NULL, null, null, null);

	private final Type type;
	private final Map<String, Entry> entries;
	private final List<ConfigNode> elements;
	private final Object value;

	private ConfigNode (Type type, Map<String, Entry> entries, List<ConfigNode> elements, Object value) {
		this.type = type;
		this.entries = entries;
		this.elements = elements;
		this.value = value;
	}

	/**
	 * Creates a new {@code ConfigNode} instance of type {@code OBJECT}.
	 * The created node has an empty set of key-value pairs.
	 *
	 * @return a new {@code ConfigNode} object representing an empty structured object.
	 */
	public static ConfigNode object () {
		return new ConfigNode(Type.OBJECT, new LinkedHashMap<>(), null, null);
	}

	/**
	 * Creates a new {@code ConfigNode} instance of type {@code ARRAY}.
	 * The created node represents an ordered collection of elements and is initially empty.
	 *
	 * @return a new {@code ConfigNode} object representing an empty array structure.
	 */
	public static ConfigNode array () {
		return new ConfigNode(Type.ARRAY, null, new ArrayList<>(), null);
	}

	/**
	 * Creates a new {@code ConfigNode} instance of type {@code VALUE}, encapsulating
	 * the given non-null scalar value. If the provided value is {@code null}, a {@code NULL}
	 * node is returned instead.
	 *
	 * @param value the scalar value to be encapsulated within the {@code ConfigNode}.
	 *              This can be of any object type (e.g., String, Number, Boolean).
	 * @return a new {@code ConfigNode} object of type {@code VALUE} if {@code value} is non-null;
	 *         otherwise, a {@code NULL} node.
	 */
	public static ConfigNode value (Object value) {
		if (value == null) return nullNode();
		return new ConfigNode(Type.VALUE, null, null, value);
	}

	/**
	 * Returns a {@code ConfigNode} instance representing a {@code NULL} node.
	 * This node is a singleton used to represent the absence of a value.
	 *
	 * @return a {@code ConfigNode} instance representing a {@code NULL} node.
	 */
	public static ConfigNode nullNode () {
		return NULL;
	}

	/**
	 * Retrieves the type of this {@code ConfigNode}.
	 * The type represents the structural or data category of the node.
	 *
	 * @return the {@link Type} of this {@code ConfigNode}.
	 */
	public Type type () {
		return type;
	}

	/**
	 * Determines if this {@code ConfigNode} instance is of type {@code OBJECT}.
	 *
	 * @return {@code true} if this node's type is {@code OBJECT}; {@code false} otherwise.
	 */
	public boolean isObject () {
		return type == Type.OBJECT;
	}

	/**
	 * Determines whether this {@code ConfigNode} represents an ordered collection of elements
	 * (i.e., is of type {@code ARRAY}).
	 *
	 * @return {@code true} if this node is of type {@code ARRAY}; {@code false} otherwise.
	 */
	public boolean isArray () {
		return type == Type.ARRAY;
	}

	/**
	 * Determines whether this {@code ConfigNode} is of type {@code VALUE}.
	 *
	 * @return {@code true} if this node's type is {@code VALUE}; {@code false} otherwise.
	 */
	public boolean isValue () {
		return type == Type.VALUE;
	}

	/**
	 * Determines whether this {@code ConfigNode} represents a {@code NULL} value.
	 *
	 * @return {@code true} if this node is of type {@code NULL}; {@code false} otherwise.
	 */
	public boolean isNull () {
		return type == Type.NULL;
	}

	/**
	 * Associates the specified key with the given {@code ConfigNode} in this object node.
	 * If the provided {@code ConfigNode} is {@code null}, a {@code NULL} node is associated with the key instead.
	 *
	 * @param key the non-null key with which the specified {@code ConfigNode} is to be associated
	 * @param node the {@code ConfigNode} to associate with the specified key; may be {@code null}
	 */
	public void put (String key, ConfigNode node) {
		put(key, node, List.of());
	}

	/**
	 * Associates the specified key with the given {@code ConfigNode} in this object node,
	 * along with additional comments. If the provided {@code ConfigNode} is {@code null},
	 * a {@code NULL} node is associated with the key instead.
	 *
	 * @param key the non-null key with which the specified {@code ConfigNode} is to be associated
	 * @param node the {@code ConfigNode} to associate with the specified key; may be {@code null}
	 * @param comments a collection of comments to associate with the entry; may be {@code null} or empty
	 */
	public void put (String key, ConfigNode node, Collection<String> comments) {
		require(Type.OBJECT);
		entries.put(Objects.requireNonNull(key, "key"), new Entry(key, node == null ? nullNode() : node, comments));
	}

	/**
	 * Retrieves the value associated with the specified key in this {@code ConfigNode}.
	 * If this node is not of type {@code OBJECT} or if the key does not exist, a {@code NULL} node is returned.
	 *
	 * @param key the key whose associated value is to be retrieved; must be non-null
	 * @return the {@code ConfigNode} associated with the specified key if present and this node is of type {@code OBJECT};
	 *         otherwise, a {@code NULL} node
	 */
	public ConfigNode get (String key) {
		if (!isObject()) return nullNode();
		Entry entry = entries.get(key);
		if (entry == null) return nullNode();
		return entry.value();
	}

	/**
	 * Checks if this {@code ConfigNode} of type {@code OBJECT} contains the specified key.
	 *
	 * @param key the key to check for existence; must be non-null
	 * @return {@code true} if this node is of type {@code OBJECT} and the specified key exists; {@code false} otherwise
	 */
	public boolean contains (String key) {
		return isObject() && entries.containsKey(key);
	}

	/**
	 * Retrieves an unmodifiable collection of all key-value entries associated with this {@code ConfigNode}.
	 * This method is applicable only for nodes of type {@code OBJECT}.
	 *
	 * @return an unmodifiable collection of {@link Entry} objects representing the key-value pairs of this node
	 * @throws IllegalStateException if this {@code ConfigNode} is not of type {@code OBJECT}
	 */
	public Collection<Entry> entries () {
		require(Type.OBJECT);
		return Collections.unmodifiableCollection(entries.values());
	}

	/**
	 * Adds the specified {@code ConfigNode} to this node's list of elements. If the
	 * provided {@code ConfigNode} is {@code null}, a {@code NULL} node is added instead.
	 * This operation is only valid for nodes of type {@code ARRAY}.
	 *
	 * @param node the {@code ConfigNode} to be added to this node's elements; may be {@code null}
	 * @throws IllegalStateException if this node is not of type {@code ARRAY}
	 */
	public void add (ConfigNode node) {
		require(Type.ARRAY);
		elements.add(node == null ? nullNode() : node);
	}

	/**
	 * Retrieves an unmodifiable list of elements contained within this {@code ConfigNode}.
	 * This operation is valid only for nodes of type {@code ARRAY}.
	 *
	 * @return an unmodifiable list of {@code ConfigNode} elements if this node is of type {@code ARRAY}.
	 * @throws IllegalStateException if this node is not of type {@code ARRAY}.
	 */
	public List<ConfigNode> elements () {
		require(Type.ARRAY);
		return Collections.unmodifiableList(elements);
	}

	/**
	 * Retrieves the scalar value encapsulated within this {@code ConfigNode}.
	 * This method is applicable only if the node is of type {@code VALUE}.
	 *
	 * @return the encapsulated scalar value if this node is of type {@code VALUE}.
	 * @throws IllegalStateException if this node is not of type {@code VALUE}.
	 */
	public Object value () {
		require(Type.VALUE);
		return value;
	}

	/**
	 * Converts this {@code ConfigNode} into its raw object representation based on its type.
	 * For nodes of type {@code OBJECT}, this method returns a {@code Map<String, Object>}
	 * where each key-value pair corresponds to the entries in this node, with values recursively
	 * converted using {@code rawValue()}. For nodes of type {@code ARRAY}, it returns a
	 * {@code List<Object>} where each element is the raw representation of the corresponding
	 * child node. Nodes of type {@code VALUE} return their encapsulated scalar value, and
	 * {@code NULL} nodes return {@code null}.
	 *
	 * @return an {@code Object} representing the raw value of this node. This is either:
	 *         - a {@code Map<String, Object>} if the node is of type {@code OBJECT},
	 *         - a {@code List<Object>} if the node is of type {@code ARRAY},
	 *         - the scalar value if the node is of type {@code VALUE},
	 *         - or {@code null} if the node is of type {@code NULL}.
	 */
	public Object rawValue () {
		return switch (type) {
			case OBJECT -> {
				Map<String, Object> raw = new LinkedHashMap<>();
				for (Entry entry : entries.values()) {
					raw.put(entry.key(), entry.value().rawValue());
				}
				yield raw;
			}
			case ARRAY -> {
				List<Object> raw = new ArrayList<>();
				for (ConfigNode element : elements) {
					raw.add(element.rawValue());
				}
				yield raw;
			}
			case VALUE -> value;
			case NULL -> null;
		};
	}

	/**
	 * Creates a deep copy of this {@code ConfigNode} including all its nested children.
	 *
	 * @return a new {@code ConfigNode} instance that is a deep copy of this node.
	 */
	public ConfigNode copy () {
		return switch (type) {
			case OBJECT -> {
				ConfigNode copy = object();
				for (Entry entry : entries.values()) {
					copy.put(entry.key(), entry.value().copy(), entry.comments());
				}
				yield copy;
			}
			case ARRAY -> {
				ConfigNode copy = array();
				for (ConfigNode element : elements) {
					copy.add(element.copy());
				}
				yield copy;
			}
			case VALUE -> value(value);
			case NULL -> nullNode();
		};
	}

	/**
	 * Ensures that this {@code ConfigNode} instance is of the specified type.
	 * If the current type of the node does not match the required type, an
	 * {@link IllegalStateException} is thrown.
	 *
	 * @param requiredType the {@link Type} to verify against the current type of this node
	 * @throws IllegalStateException if the current type of this node does not match {@code requiredType}
	 */
	private void require (Type requiredType) {
		if (type != requiredType) {
			throw new IllegalStateException("Expected " + requiredType + " config node, got " + type);
		}
	}

	/**
	 * Represents an immutable key-value pair with associated comments.
	 * This class is designed to store a configuration entry consisting of a key,
	 * a value represented as a `ConfigNode`, and optional comments.
	 */
	public static final class Entry {
		private final String key;
		private final ConfigNode value;
		private final List<String> comments;

		private Entry (String key, ConfigNode value, Collection<String> comments) {
			this.key = key;
			this.value = value;
			this.comments = copyComments(comments);
		}

		public String key () {
			return key;
		}

		public ConfigNode value () {
			return value;
		}

		public List<String> comments () {
			return comments;
		}

		private static List<String> copyComments (Collection<String> comments) {
			if (comments == null || comments.isEmpty()) return List.of();

			List<String> copy = new ArrayList<>();
			for (String comment : comments) {
				if (comment != null) copy.add(comment);
			}
			return List.copyOf(copy);
		}
	}
}
