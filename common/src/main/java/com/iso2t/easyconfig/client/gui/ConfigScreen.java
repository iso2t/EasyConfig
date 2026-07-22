package com.iso2t.easyconfig.client.gui;

import com.iso2t.easyconfig.api.metadata.ConfigEntry;
import com.iso2t.easyconfig.api.metadata.ConfigEntryKind;
import com.iso2t.easyconfig.api.metadata.ConfigValueResult;
import com.iso2t.easyconfig.api.value.wrappers.ColorValue;
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

	private static final int HEADER_HEIGHT             = 56;
	private static final int FOOTER_HEIGHT             = 36;
	private static final int ROW_HEIGHT                = 28;
	private static final int FALLBACK_TEXT_COLOR       = 0xFFFFFFFF;
	private static final int FALLBACK_MUTED_TEXT_COLOR = 0xFFA0A0A0;
	private static final int CONTROL_SPACING          = 4;
	private static final int COLOR_SWATCH_WIDTH       = 20;
	private static final int RESET_BUTTON_WIDTH       = 20;

	private final Screen                   parent;
	private final List<ConfigScreenTab<?>> tabs;
	private       int                      selectedTab;
	private       ConfigEntryList          entryList;

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
		graphics.centeredText(font, title, width / 2, 12, textColor());
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

		private final ConfigEntry          entry;
		private final List<AbstractWidget> controls = new ArrayList<>();
		private final List<AbstractWidget> valueControls = new ArrayList<>();
		private       Button               resetButton;

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

			graphics.text(font, entry.displayName(), labelX, labelY, entry.editable() ? textColor() : mutedTextColor(), false);
			updateResetButton();
			for (int i = 0; i < valueControls.size(); i++) {
				AbstractWidget control = valueControls.get(i);
				placeControl(control, i);
				control.extractRenderState(graphics, mouseX, mouseY, tickProgress);
			}
			if (resetButton != null && resetButton.visible) {
				placeResetButton();
				resetButton.extractRenderState(graphics, mouseX, mouseY, tickProgress);
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
				addValueControl(readOnlyButton("Read only"));
				return;
			}

			switch (entry.kind()) {
				case BOOLEAN -> addValueControl(booleanControl());
				case ENUM -> addValueControl(enumControl());
				case COLOR -> addValueControls(colorControls());
				case NUMBER, STRING, CHARACTER -> addValueControl(textControl());
				case LIST, ARRAY, OBJECT, UNKNOWN -> addValueControl(readOnlyButton("Unsupported"));
				case SECTION -> {
				}
			}

			if (entry.scalarEditable()) {
				resetButton = Button.builder(Component.literal("↺"), ignored -> resetEntry()).bounds(0, 0, RESET_BUTTON_WIDTH, 20).build();
				updateResetButton();
				controls.add(resetButton);
			}
		}

		private AbstractWidget booleanControl () {
			boolean value = Boolean.TRUE.equals(entry.value());
			return CycleButton.onOffBuilder(value).displayOnlyValue().create(0, 0, valueControlWidth(), 20, Component.literal(entry.displayName()), (button, selected) -> {
				entry.trySetValue(selected);
				updateResetButton();
			});
		}

		private AbstractWidget enumControl () {
			List<Object> values = entry.allowedValues();
			return CycleButton.builder(value -> Component.literal(String.valueOf(value)), entry.value()).withValues(values).displayOnlyValue().create(0, 0, valueControlWidth(), 20, Component.literal(entry.displayName()), (button, selected) -> {
				entry.trySetValue(selected);
				updateResetButton();
			});
		}

		private AbstractWidget textControl () {
			EditBox box = new EditBox(font, 0, 0, valueControlWidth(), 20, Component.literal(entry.displayName()));
			box.setMaxLength(256);
			box.setValue(String.valueOf(entry.value()));
			box.setResponder(value -> {
				ConfigValueResult result = entry.trySetValue(value);
				box.setTextColor(result.success() ? textColor() : 0xFFFF5555);
				if (result.success()) updateResetButton();
			});
			return box;
		}

		private List<AbstractWidget> colorControls () {
			Button[] swatch = new Button[1];
			swatch[0] = Button.builder(colorPreview(entry.value()), ignored -> ConfigScreen.this.minecraft.gui.setScreen(new ColorPickerScreen(ConfigScreen.this, Component.literal(entry.displayName()), asColor(entry.value()), color -> {
				ConfigValueResult result = entry.trySetValue(color);
				if (result.success()) {
					swatch[0].setMessage(colorPreview(color));
					updateResetButton();
				}
			}))).bounds(0, 0, COLOR_SWATCH_WIDTH, 20).build();

			return List.of(swatch[0]);
		}

		private AbstractWidget readOnlyButton (String label) {
			Button button = Button.builder(Component.literal(label), ignored -> {
			}).bounds(0, 0, valueControlWidth(), 20).build();
			button.active = false;
			return button;
		}

		private void placeControl (AbstractWidget control, int index) {
			if (entry.kind() == ConfigEntryKind.COLOR) {
				control.setWidth(COLOR_SWATCH_WIDTH);
				control.setX(valueControlRight() - COLOR_SWATCH_WIDTH);
				control.setY(getContentYMiddle() - 10);
				return;
			}

			if (valueControls.size() == 2) {
				control.setWidth(index == 0 ? valueControlWidth() : COLOR_SWATCH_WIDTH);
				control.setX(index == 0 ? valueControlRight() - valueControlWidth() : valueControlRight() - COLOR_SWATCH_WIDTH);
				control.setY(getContentYMiddle() - 10);
				return;
			}

			control.setWidth(valueControlWidth());
			control.setX(valueControlRight() - valueControlWidth());
			control.setY(getContentYMiddle() - 10);
		}

		private void placeResetButton () {
			resetButton.setX(getContentRight() - RESET_BUTTON_WIDTH);
			resetButton.setY(getContentYMiddle() - 10);
		}

		private int controlWidth () {
			return Math.min(180, Math.max(120, getContentWidth() / 2));
		}

		private int valueControlWidth () {
			return resetVisible() ? controlWidth() - RESET_BUTTON_WIDTH - CONTROL_SPACING : controlWidth();
		}

		private int valueControlRight () {
			return resetVisible() ? getContentRight() - RESET_BUTTON_WIDTH - CONTROL_SPACING : getContentRight();
		}

		private Component colorPreview (Object value) {
			int color = asColor(value);
			return Component.literal("■").withStyle(style -> style.withColor(color & 0xFFFFFF));
		}

		private int asColor (Object value) {
			if (value instanceof Number number) return number.intValue();
			try {
				return ColorValue.parseHex(String.valueOf(value));
			} catch (RuntimeException _) {
				return 0xFFFFFFFF;
			}
		}

		private void addValueControl (AbstractWidget control) {
			valueControls.add(control);
			controls.add(control);
		}

		private void addValueControls (List<AbstractWidget> controls) {
			for (AbstractWidget control : controls) {
				addValueControl(control);
			}
		}

		private void resetEntry () {
			ConfigValueResult result = entry.tryResetValue();
			if (result.success()) {
				rebuildWidgets();
			}
		}

		private void updateResetButton () {
			if (resetButton == null) return;
			boolean visible = resetVisible();
			resetButton.visible = visible;
			resetButton.active = visible;
		}

		private boolean resetVisible () {
			return entry.scalarEditable() && !entry.isDefaultValue();
		}

	}

	private static int textColor () {
		return FALLBACK_TEXT_COLOR;
	}

	private static int mutedTextColor () {
		return FALLBACK_MUTED_TEXT_COLOR;
	}

}
