package com.iso2t.easyconfig.client.gui;

import com.iso2t.easyconfig.api.annotations.Translation;
import com.iso2t.easyconfig.api.manager.ConfigManager;
import com.iso2t.easyconfig.api.metadata.ConfigEntry;
import com.iso2t.easyconfig.api.metadata.ConfigSchema;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ConfigScreenTab<T> {

	private final Component                        title;
	private final Consumer<T>                      saveAction;
	private final Supplier<ConfigSchema<T>>        reloadAction;
	private       ConfigSchema<T>                  schema;
	private       Function<T, Optional<Component>> validation  = config -> Optional.empty();
	private       Function<T, Component>           description = config -> Component.empty();
	private       BooleanSupplier                  editable    = () -> true;

	public ConfigScreenTab<T> validation (Function<T, Optional<Component>> validation) {
		this.validation = Objects.requireNonNull(validation);
		return this;
	}

	public ConfigScreenTab<T> description (Function<T, Component> description) {
		this.description = Objects.requireNonNull(description);
		return this;
	}

	public Component description () {
		return description.apply(schema.config());
	}

	public ConfigScreenTab<T> editable (BooleanSupplier editable) {
		this.editable = Objects.requireNonNull(editable);
		return this;
	}

	public boolean editable () {
		return editable.getAsBoolean();
	}

	public Optional<Component> error () {
		return validation.apply(schema.config());
	}

	public Component label (ConfigEntry entry) {
		var translation = entry.field().getAnnotation(Translation.class);
		return translation == null ? Component.literal(entry.displayName()) : Component.translatable(translation.value());
	}

	public Component tooltip (ConfigEntry entry) {
		var translation = entry.field().getAnnotation(Translation.class);
		return translation == null || translation.tooltip().isEmpty() ? Component.literal(String.join("\n", entry.comments())) : Component.translatable(translation.tooltip());
	}

	public Component valueLabel (ConfigEntry entry, Object value) {
		var translation = entry.field().getAnnotation(Translation.class);
		return translation != null && !translation.valuePrefix().isEmpty() && value instanceof Enum<?> constant ? Component.translatable(translation.valuePrefix() + constant.name().toLowerCase(Locale.ROOT)) : Component.literal(String.valueOf(value));
	}

	public ConfigScreenTab (Component title, ConfigSchema<T> schema, Consumer<T> saveAction, Supplier<ConfigSchema<T>> reloadAction) {
		this.title = Objects.requireNonNull(title, "title");
		this.schema = Objects.requireNonNull(schema, "schema");
		this.saveAction = Objects.requireNonNull(saveAction, "saveAction");
		this.reloadAction = Objects.requireNonNull(reloadAction, "reloadAction");
	}

	public static <T> ConfigScreenTab<T> of (Component title, ConfigManager<T> manager) {
		T config = manager.loadAndSave();
		return of(title, manager, config);
	}

	public static <T> ConfigScreenTab<T> of (Component title, ConfigManager<T> manager, T config) {
		return new ConfigScreenTab<>(title, manager.schema(config), manager::save, () -> {
			manager.loadAndSaveInto(config);
			return manager.schema(config);
		});
	}

	public static <T> ConfigScreenTab<T> of (String title, ConfigManager<T> manager) {
		return of(Component.literal(title), manager);
	}

	public static <T> ConfigScreenTab<T> of (String title, ConfigManager<T> manager, T config) {
		return of(Component.literal(title), manager, config);
	}

	public Component title () {
		return title;
	}

	public ConfigSchema<T> schema () {
		return schema;
	}

	public void save () {
		if (editable() && error().isEmpty()) saveAction.accept(schema.config());
	}

	public void reload () {
		schema = Objects.requireNonNull(reloadAction.get(), "reloadAction returned null");
	}

}
