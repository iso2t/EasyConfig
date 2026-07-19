package com.iso2t.easyconfig.api.files;

/**
 * An enumeration that represents various file types and their associated implementations.
 * Each enum constant maps to a specific subclass of {@link AbstractFileType}.
 * The enum provides methods to create instances of the file type, retrieve its class,
 * and access the associated file extension.
 */
public enum FileTypes {
	JSON5(Json5.class);

	private final Class<? extends AbstractFileType> type;

	FileTypes (Class<? extends AbstractFileType> type) {
		this.type = type;
	}

	/**
	 * Creates and returns a new instance of the file type associated with this enumeration constant.
	 * The file type is instantiated using its no-argument constructor via reflection.
	 *
	 * @return a new instance of a subclass of {@link AbstractFileType} corresponding to this file type
	 * @throws IllegalStateException if the instantiation of the file type fails
	 */
	public AbstractFileType create () {
		try {
			return type.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to instantiate file type " + type.getName(), e);
		}
	}

	/**
	 * Retrieves the class type associated with this file type enumeration constant.
	 * The returned class type is a subclass of {@link AbstractFileType} that represents
	 * the specific implementation of the file type.
	 *
	 * @return the {@link Class} object representing the subclass of {@link AbstractFileType}
	 *         associated with this file type enumeration constant
	 */
	public Class<? extends AbstractFileType> type () {
		return type;
	}

	/**
	 * Retrieves the file extension associated with the file type represented by this enumeration constant.
	 * The file extension is determined by creating a new instance of the file type and invoking its
	 * {@code extension()} method.
	 *
	 * @return the file extension as a {@code String} corresponding to this file type
	 */
	public String extension () {
		return create().extension();
	}
}
