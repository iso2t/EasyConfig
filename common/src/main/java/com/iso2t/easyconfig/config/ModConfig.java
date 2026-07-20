package com.iso2t.easyconfig.config;

import com.iso2t.easyconfig.ColorProvider;
import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.api.Side;
import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.value.wrappers.BooleanValue;
import com.iso2t.easyconfig.api.value.wrappers.EnumValue;
import com.iso2t.easyconfig.api.value.wrappers.StringValue;

@Config(name = Constants.MOD_ID, side = Side.CLIENT)
public class ModConfig {

	@Comment(value = "Show the EasyConfig branding on the main menu", values = false)
	public BooleanValue SHOW_MAIN_MENU_BRANDING = BooleanValue.of(true);

	@Comment(value = "Debug options")
	public Debug DEBUG = new Debug();

	@Comment(value = "Config screen options")
	public ConfigScreen CONFIG_SCREEN = new ConfigScreen();

	public static class Debug {

		@Comment(value = "Show the EasyConfig information in the F3 menu", values = false)
		public BooleanValue SHOW_IN_F3 = BooleanValue.of(true);

		@Comment(value = "The side of the F3 menu to show EasyConfig information on. Required show_in_f3 to be true to take effect.")
		public EnumValue<F3Side> F3_SIDE = EnumValue.of(F3Side.RIGHT);

		public enum F3Side {
			LEFT,
			RIGHT
		}
	}

	public static class ConfigScreen {

		@Comment(value = "The color of the text in the config screen", values = false)
		public StringValue TEXT_COLOR = StringValue.of(ColorProvider.intToHex(0xFFFFFFFF));

		@Comment(value = "The color of the muted text in the config screen", values = false)
		public StringValue MUTED_TEXT_COLOR = StringValue.of(ColorProvider.intToHex(0xFFA0A0A0));

	}

}
