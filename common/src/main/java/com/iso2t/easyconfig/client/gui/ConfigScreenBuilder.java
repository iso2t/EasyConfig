package com.iso2t.easyconfig.client.gui;

import com.iso2t.easyconfig.api.metadata.ConfigEntry;
import com.iso2t.easyconfig.api.metadata.ConfigEntryKind;
import com.iso2t.easyconfig.api.metadata.ConfigSchema;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/** Builds screens for configs whose persistence or networking is owned by the calling mod. */
public final class ConfigScreenBuilder {
	private final Component title;
	private final List<ConfigEntry> entries = new ArrayList<>();
	private final Map<ConfigEntry, Component> labels = new HashMap<>(), tooltips = new HashMap<>();
	private final Map<ConfigEntry, Function<Object, Component>> valueLabels = new HashMap<>();
	private Screen parent;
	private boolean editable = true;
	private Runnable save = () -> {};
	private Supplier<Optional<Component>> validation = Optional::empty;
	private Supplier<? extends Screen> reload;

	public ConfigScreenBuilder (Component title) { this.title = Objects.requireNonNull(title); }
	public ConfigScreenBuilder parent (Screen parent) { this.parent = parent; return this; }
	public ConfigScreenBuilder editable (boolean editable) { this.editable = editable; return this; }
	public ConfigScreenBuilder onSave (Runnable save) { this.save = Objects.requireNonNull(save); return this; }
	public ConfigScreenBuilder onReload (Supplier<? extends Screen> reload) { this.reload = Objects.requireNonNull(reload); return this; }
	public ConfigScreenBuilder validation (Supplier<Optional<Component>> validation) { this.validation = Objects.requireNonNull(validation); return this; }

	public ConfigScreenBuilder add (ConfigEntry entry, Component label, Component tooltip) {
		entries.add(Objects.requireNonNull(entry));
		labels.put(entry, Objects.requireNonNull(label));
		if (tooltip != null) tooltips.put(entry, tooltip);
		return this;
	}

	public ConfigScreenBuilder valueLabel (ConfigEntry entry, Function<Object, Component> label) {
		valueLabels.put(entry, Objects.requireNonNull(label));
		return this;
	}

	public ConfigScreenBuilder description (Component text) {
		return add(ConfigEntry.builder("description_" + entries.size(), "").kind(ConfigEntryKind.SECTION).build(), text, text);
	}

	public List<ConfigEntry> entries () { return List.copyOf(entries); }

	public ConfigScreen build () {
		var schema = ConfigSchema.ofEntries(entries);
		var tab = new ConfigScreenTab<>(title, schema, ignored -> save.run(), () -> schema)
				.editable(editable).validation(validation).labels(labels::get).tooltips(tooltips::get)
				.valueLabels((entry, value) -> valueLabels.getOrDefault(entry, v -> Component.literal(String.valueOf(v))).apply(value));
		return new ConfigScreen(parent, title, List.of(tab)) {
			@Override
			protected void reloadSelected () {
				if (reload != null) Minecraft.getInstance().gui.setScreen(reload.get());
				else super.reloadSelected();
			}
		};
	}
}
