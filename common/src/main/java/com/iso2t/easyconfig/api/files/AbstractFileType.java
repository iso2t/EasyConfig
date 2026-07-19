package com.iso2t.easyconfig.api.files;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Represents an abstract base class for handling file operations for different file types.
 * This class defines the structure for reading, writing, and converting configurations
 * between files and an internal representation, as well as converting values from configuration nodes
 * to specific types.
 */
public abstract class AbstractFileType {
	private final String extension;

	public AbstractFileType (String extension) {
		this.extension = extension;
	}

	public String extension () {
		return extension;
	}

	/**
	 * Reads a configuration file from the specified path and returns its contents
	 * as a {@link ConfigNode} object. The file is parsed according to the specific
	 * implementation of the file type.
	 *
	 * @param file the path to the configuration file to read
	 * @return a {@link ConfigNode} representing the contents of the configuration file
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	public abstract ConfigNode read (Path file) throws IOException;

	/**
	 * Writes a configuration represented by the given {@link ConfigNode} to the specified file path.
	 * The implementation defines how the {@link ConfigNode} object is serialized into the file format
	 * associated with the specific file type.
	 *
	 * @param file the path to the file where the configuration will be written
	 * @param root the {@link ConfigNode} representing the root of the configuration to be serialized
	 * @throws IOException if an I/O error occurs during the writing process
	 */
	public abstract void write (Path file, ConfigNode root) throws IOException;

	/**
	 * Reads the value of a specific type from the provided {@link ConfigNode}.
	 * This method attempts to extract and deserialize the value stored in the given node
	 * into an instance of the specified class type.
	 *
	 * @param node the {@link ConfigNode} from which the value is to be read
	 * @param type the {@link Class} representing the type to which the value will be deserialized
	 * @return the deserialized value of type {@code T} extracted from the given configuration node
	 * @throws IOException if an error occurs during the deserialization process
	 */
	public abstract <T> T readValue (ConfigNode node, Class<T> type) throws IOException;

	/**
	 * Converts a value from the given {@link ConfigNode} into an instance of the specified type.
	 * This method performs deserialization by interpreting the data stored in the {@link ConfigNode}
	 * and mapping it to the desired class type.
	 *
	 * @param <T> the target type to which the value will be converted
	 * @param node the {@link ConfigNode} representing the value to be converted
	 * @param type the {@link Class} object representing the target type
	 * @return an instance of the specified type {@code T} created from the value in the {@link ConfigNode}
	 */
	public abstract <T> T convertValue (ConfigNode node, Class<T> type);
}
