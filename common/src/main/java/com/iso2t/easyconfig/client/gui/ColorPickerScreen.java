package com.iso2t.easyconfig.client.gui;

import com.iso2t.easyconfig.api.value.wrappers.ColorValue;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ColorPickerScreen extends Screen {

	private static final int PREVIEW_WIDTH  = 120;
	private static final int PREVIEW_HEIGHT = 24;

	private final Screen            parent;
	private final Consumer<Integer> onChanged;

	private int         alpha;
	private int         red;
	private int         green;
	private int         blue;
	private EditBox     hexBox;
	private ColorSlider alphaSlider;
	private ColorSlider redSlider;
	private ColorSlider greenSlider;
	private ColorSlider blueSlider;
	private boolean     updatingHex;

	public ColorPickerScreen (Screen parent, Component title, int color, Consumer<Integer> onChanged) {
		super(title);
		this.parent = parent;
		this.onChanged = onChanged;
		setColor(color);
	}

	@Override
	protected void init () {
		int controlWidth = Math.min(240, width - 40);
		int x = (width - controlWidth) / 2;
		int y = 52;

		hexBox = new EditBox(font, x, y, controlWidth, 20, Component.literal("Hex"));
		hexBox.setMaxLength(9);
		hexBox.setValue(ColorValue.formatHex(color()));
		hexBox.setResponder(this::setFromHex);
		addRenderableWidget(hexBox);

		redSlider = addRenderableWidget(new ColorSlider(x, y + 32, controlWidth, Component.literal("Red"), red, value -> {
			red = value;
			emitChanged();
		}));
		greenSlider = addRenderableWidget(new ColorSlider(x, y + 56, controlWidth, Component.literal("Green"), green, value -> {
			green = value;
			emitChanged();
		}));
		blueSlider = addRenderableWidget(new ColorSlider(x, y + 80, controlWidth, Component.literal("Blue"), blue, value -> {
			blue = value;
			emitChanged();
		}));
		alphaSlider = addRenderableWidget(new ColorSlider(x, y + 104, controlWidth, Component.literal("Alpha"), alpha, value -> {
			alpha = value;
			emitChanged();
		}));

		addRenderableWidget(Button.builder(Component.translatable("easyconfig.config_screen.done"), ignored -> onClose()).bounds((width - 80) / 2, height - 32, 80, 20).build());
	}

	@Override
	public void onClose () {
		minecraft.gui.setScreen(parent);
	}

	@Override
	public void extractRenderState (GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickProgress) {
		graphics.centeredText(font, title, width / 2, 14, 0xFFFFFFFF);
		int previewX = (width - PREVIEW_WIDTH) / 2;
		int previewY = 28;
		graphics.fill(previewX, previewY, previewX + PREVIEW_WIDTH, previewY + PREVIEW_HEIGHT, color());
		graphics.outline(previewX, previewY, PREVIEW_WIDTH, PREVIEW_HEIGHT, 0xFFFFFFFF);
		super.extractRenderState(graphics, mouseX, mouseY, tickProgress);
	}

	private void setFromHex (String value) {
		if (updatingHex) return;

		try {
			setColor(ColorValue.parseHex(value));
			updateSliders();
			hexBox.setTextColor(0xFFFFFFFF);
			onChanged.accept(color());
		} catch (RuntimeException _) {
			hexBox.setTextColor(0xFFFF5555);
		}
	}

	private void emitChanged () {
		updateHexBox();
		onChanged.accept(color());
	}

	private void updateHexBox () {
		updatingHex = true;
		hexBox.setValue(ColorValue.formatHex(color()));
		hexBox.setTextColor(0xFFFFFFFF);
		updatingHex = false;
	}

	private void updateSliders () {
		if (redSlider != null) redSlider.setChannel(red);
		if (greenSlider != null) greenSlider.setChannel(green);
		if (blueSlider != null) blueSlider.setChannel(blue);
		if (alphaSlider != null) alphaSlider.setChannel(alpha);
	}

	private void setColor (int color) {
		alpha = color >>> 24 & 0xFF;
		red = color >>> 16 & 0xFF;
		green = color >>> 8 & 0xFF;
		blue = color & 0xFF;
	}

	private int color () {
		return alpha << 24 | red << 16 | green << 8 | blue;
	}

	private static final class ColorSlider extends AbstractSliderButton {

		private final Component         label;
		private final Consumer<Integer> onChanged;

		private ColorSlider (int x, int y, int width, Component label, int value, Consumer<Integer> onChanged) {
			super(x, y, width, 20, Component.empty(), value / 255.0D);
			this.label = label;
			this.onChanged = onChanged;
			updateMessage();
		}

		@Override
		protected void updateMessage () {
			setMessage(Component.literal(label.getString() + ": " + channel()));
		}

		@Override
		protected void applyValue () {
			onChanged.accept(channel());
		}

		private void setChannel (int channel) {
			value = clamp(channel) / 255.0D;
			updateMessage();
		}

		private int channel () {
			return clamp((int) Math.round(value * 255.0D));
		}

		private static int clamp (int channel) {
			return Math.max(0, Math.min(255, channel));
		}
	}
}
