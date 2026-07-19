package com.iso2t.easyconfig.config;

import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.api.Side;
import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.value.wrappers.BooleanValue;
import com.iso2t.easyconfig.api.value.wrappers.EnumValue;

@Config(name = Constants.MOD_ID, side = Side.CLIENT)
public class ModConfig {

	@Comment(value = "Show the EasyConfig branding on the main menu", values = false)
	public BooleanValue SHOW_MAIN_MENU_BRANDING = BooleanValue.of(true);

	@Comment(value = "Debug options")
	public Debug DEBUG = new Debug();

	public static class Debug {

		@Comment(value = "Show the EasyConfig information in the F3 menu", values = false)
		public BooleanValue SHOW_IN_F3 = BooleanValue.of(true);

		@Comment(value = "The side of the F3 menu to show EasyConfig information on. Required show_in_f3 to be true to take effect.")
		public EnumValue<F3Side> F3_SIDE = EnumValue.of(F3Side.RIGHT);

		public enum F3Side {
			LEFT, RIGHT
		}
	}

}
