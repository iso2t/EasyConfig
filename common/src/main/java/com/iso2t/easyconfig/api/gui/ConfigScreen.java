package com.iso2t.easyconfig.api.gui;

import com.iso2t.easyconfig.ColorProvider;
import com.iso2t.easyconfig.EasyConfig;
import com.iso2t.easyconfig.api.metadata.ConfigEntry;
import com.iso2t.easyconfig.api.metadata.ConfigEntryKind;
import com.iso2t.easyconfig.api.metadata.ConfigValueResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigScreen extends Screen {

	private static final int HEADER_HEIGHT = 56;
	private static final int FOOTER_HEIGHT = 36;
	private static final int ROW_HEIGHT = 28;
	private static final int TEXT_COLOR = ColorProvider.hexToInt(EasyConfig.getConfig().CONFIG_SCREEN.TEXT_COLOR.get());
	private static final int MUTED_TEXT_COLOR = ColorProvider.hexToInt(EasyConfig.getConfig().CONFIG_SCREEN.MUTED_TEXT_COLOR.get());

	private final Screen parent;
	private final List<ConfigScreenTab<?>> tabs;
	private int selectedTab;
	private ConfigEntryList entryList;

	public ConfigScreen (Screen parent, Component title, List<ConfigScreenTab<?>> tabs) {
		super(title);
		this.parent = parent;
		if (tabs == null || tabs.isEmpty()) {
			throw new IllegalArgumentException("ConfigScreen requires at least one tab");
		}
		this.tabs = List.copyOf(tabs);
	}

	@Override
	protected void init () {
		rebuildWidgets();
	}

	@Override
	public void onClose () {
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void extractRenderState (GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress) {
		graphics.centeredText(font, title, width / 2, 12, TEXT_COLOR);
		super.extractRenderState(graphics, mouseX, mouseY, tickProgress);
	}

	@Override
	protected void rebuildWidgets () {
		clearWidgets();
		addTabs();
		addEntryList();
		addFooter();
	}

	private void addTabs () {
		int x = 8;
		int y = 30;
		int maxTabWidth = Math.max(80, Math.min(140, (width - 16) / tabs.size()));

		for (int i = 0; i < tabs.size(); i++) {
			int index = i;
			ConfigScreenTab<?> tab = tabs.get(i);
			Button button = Button.builder(tab.title(), ignored -> selectTab(index)).bounds(x, y, maxTabWidth - 4, 20).build();
			button.active = index != selectedTab;
			addRenderableWidget(button);
			x += maxTabWidth;
		}
	}

	private void addEntryList () {
		entryList = new ConfigEntryList(minecraft, width, height - HEADER_HEIGHT - FOOTER_HEIGHT, HEADER_HEIGHT);
		for (ConfigEntry entry : selectedTab().schema().entries()) {
			entryList.addConfigEntry(entry);
		}
		addRenderableWidget(entryList);
	}

	private void addFooter () {
		int y = height - 28;
		int buttonWidth = 80;
		int spacing = 6;
		int totalWidth = buttonWidth * 4 + spacing * 3;
		int x = (width - totalWidth) / 2;

		addRenderableWidget(Button.builder(Component.translatable("easyconfig.config_screen.save"), ignored -> saveSelected()).bounds(x, y, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("easyconfig.config_screen.reload"), ignored -> reloadSelected()).bounds(x + (buttonWidth + spacing), y, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("easyconfig.config_screen.reset"), ignored -> resetSelected()).bounds(x + (buttonWidth + spacing) * 2, y, buttonWidth, 20).build());
		addRenderableWidget(Button.builder(Component.translatable("easyconfig.config_screen.done"), ignored -> onClose()).bounds(x + (buttonWidth + spacing) * 3, y, buttonWidth, 20).build());
	}

	private void selectTab (int index) {
		if (selectedTab == index) return;
		selectedTab = index;
		rebuildWidgets();
	}

	private void saveSelected () {
		selectedTab().save();
	}

	private void reloadSelected () {
		selectedTab().reload();
		rebuildWidgets();
	}

	private void resetSelected () {
		for (ConfigEntry entry : selectedTab().schema().editableEntries()) {
			entry.tryResetValue();
		}
		rebuildWidgets();
	}

	private ConfigScreenTab<?> selectedTab () {
		return tabs.get(selectedTab);
	}

	private final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntryRow> {

		private ConfigEntryList (Minecraft minecraft, int width, int height, int y) {
			super(minecraft, width, height, y, ROW_HEIGHT);
			centerListVertically = false;
		}

		private void addConfigEntry (ConfigEntry entry) {
			addEntry(new ConfigEntryRow(entry));
		}

		@Override
		public int getRowWidth () {
			return Math.min(520, width - 40);
		}

	}

	private final class ConfigEntryRow extends ContainerObjectSelectionList.Entry<ConfigEntryRow> {

		private final ConfigEntry entry;
		private final List<AbstractWidget> controls = new ArrayList<>();

		private ConfigEntryRow (ConfigEntry entry) {
			this.entry = Objects.requireNonNull(entry, "entry");
			createControls();
		}

		@Override
		public void extractContent (@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float tickProgress) {
			int labelX = getContentX();
			int labelY = getContentYMiddle() - font.lineHeight / 2;

			if (entry.kind() == ConfigEntryKind.SECTION) {
				graphics.text(font, entry.displayName(), labelX, labelY, 0xFFFFD966, false);
				return;
			}

			graphics.text(font, entry.displayName(), labelX, labelY, entry.editable() ? TEXT_COLOR : MUTED_TEXT_COLOR, false);
			for (AbstractWidget control : controls) {
				placeControl(control);
				control.extractRenderState(graphics, mouseX, mouseY, tickProgress);
			}
		}

		@Override
		public List<? extends GuiEventListener> children () {
			return controls;
		}

		@Override
		public List<? extends NarratableEntry> narratables () {
			return controls;
		}

		@Override
		public void visitWidgets (java.util.function.Consumer<AbstractWidget> widgetVisitor) {
			controls.forEach(widgetVisitor);
		}

		private void createControls () {
			if (!entry.editable()) {
				controls.add(readOnlyButton("Read only"));
				return;
			}

			switch (entry.kind()) {
				case BOOLEAN -> controls.add(booleanControl());
				case ENUM -> controls.add(enumControl());
				case NUMBER, STRING, CHARACTER -> controls.add(textControl());
				case LIST, ARRAY, OBJECT, UNKNOWN -> controls.add(readOnlyButton("Unsupported"));
				case SECTION -> {
				}
			}
		}

		private AbstractWidget booleanControl () {
			boolean value = Boolean.TRUE.equals(entry.value());
			return CycleButton.onOffBuilder(value)
				.displayOnlyValue()
				.create(0, 0, controlWidth(), 20, Component.literal(entry.displayName()), (button, selected) -> entry.trySetValue(selected));
		}

		private AbstractWidget enumControl () {
			List<Object> values = entry.allowedValues();
			return CycleButton.builder(value -> Component.literal(String.valueOf(value)), entry.value())
				.withValues(values)
				.displayOnlyValue()
				.create(0, 0, controlWidth(), 20, Component.literal(entry.displayName()), (button, selected) -> entry.trySetValue(selected));
		}

		private AbstractWidget textControl () {
			EditBox box = new EditBox(font, 0, 0, controlWidth(), 20, Component.literal(entry.displayName()));
			box.setMaxLength(256);
			box.setValue(String.valueOf(entry.value()));
			box.setResponder(value -> {
				ConfigValueResult result = entry.trySetValue(value);
				box.setTextColor(result.success() ? TEXT_COLOR : 0xFFFF5555);
			});
			return box;
		}

		private AbstractWidget readOnlyButton (String label) {
			Button button = Button.builder(Component.literal(label), ignored -> {}).bounds(0, 0, controlWidth(), 20).build();
			button.active = false;
			return button;
		}

		private void placeControl (AbstractWidget control) {
			control.setX(getContentRight() - controlWidth());
			control.setY(getContentYMiddle() - 10);
		}

		private int controlWidth () {
			return Math.min(180, Math.max(120, getContentWidth() / 2));
		}

	}

}
