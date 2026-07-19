package com.iso2t.easyconfig.config;

import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.api.Side;
import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.value.wrappers.BooleanValue;
import com.iso2t.easyconfig.api.value.wrappers.IntegerValue;

@Config(name = Constants.MOD_ID, side = Side.CLIENT)
public class ModConfig {

	@Comment("Enable EasyConfig")
	public BooleanValue ENABLED = BooleanValue.of(true);

	@Comment("Display debug settings for EasyConfig during startup")
	public BooleanValue DEBUG = BooleanValue.of(false);

	public Nested NESTED = new Nested();

	@Comment("A nested field")
	public static class Nested {

		@Comment("A nested value")
		public IntegerValue NESTED_VALUE = IntegerValue.of(1);
	}

}
