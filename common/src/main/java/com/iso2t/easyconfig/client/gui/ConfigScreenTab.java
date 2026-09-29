package com.iso2t.easyconfig.client.gui;

import com.iso2t.easyconfig.api.manager.ConfigManager;
import com.iso2t.easyconfig.api.metadata.ConfigSchema;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import com.iso2t.easyconfig.api.metadata.ConfigEntry;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigScreenTab<T> {

	private final Component                 title;
	private final Consumer<T>               saveAction;
	private final Supplier<ConfigSchema<T>> reloadAction;
	private       ConfigSchema<T>           schema;
	private Supplier<Optional<Component>> validation = Optional::empty;
	private Function<ConfigEntry, Component> labels = entry -> Component.literal(entry.displayName());
	private Function<ConfigEntry, Component> tooltips = entry -> Component.literal(String.join("\n", entry.comments()));
	private java.util.function.BiFunction<ConfigEntry, Object, Component> valueLabels = (entry, value) -> Component.literal(String.valueOf(value));
	private boolean editable = true;

	public ConfigScreenTab<T> validation (Supplier<Optional<Component>> validation) { this.validation = Objects.requireNonNull(validation); return this; }
	public ConfigScreenTab<T> labels (Function<ConfigEntry, Component> labels) { this.labels = Objects.requireNonNull(labels); return this; }
	public ConfigScreenTab<T> tooltips (Function<ConfigEntry, Component> tooltips) { this.tooltips = Objects.requireNonNull(tooltips); return this; }
	public ConfigScreenTab<T> valueLabels (java.util.function.BiFunction<ConfigEntry, Object, Component> labels) { valueLabels = Objects.requireNonNull(labels); return this; }
	public ConfigScreenTab<T> editable (boolean editable) { this.editable = editable; return this; }
	public boolean editable () { return editable; }
	public Optional<Component> error () { return validation.get(); }
	public Component label (ConfigEntry entry) { return labels.apply(entry); }
	public Component tooltip (ConfigEntry entry) { return tooltips.apply(entry); }
	public Component valueLabel (ConfigEntry entry, Object value) { return valueLabels.apply(entry, value); }

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
		if (editable && error().isEmpty()) saveAction.accept(schema.config());
	}

	public void reload () {
		schema = Objects.requireNonNull(reloadAction.get(), "reloadAction returned null");
	}

}
