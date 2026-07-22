package com.iso2t.easyconfig.client.gui;

import com.iso2t.easyconfig.api.manager.ConfigManager;
import com.iso2t.easyconfig.api.metadata.ConfigSchema;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ConfigScreenTab<T> {

	private final Component                 title;
	private final Consumer<T>               saveAction;
	private final Supplier<ConfigSchema<T>> reloadAction;
	private       ConfigSchema<T>           schema;

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
		saveAction.accept(schema.config());
	}

	public void reload () {
		schema = Objects.requireNonNull(reloadAction.get(), "reloadAction returned null");
	}

}
