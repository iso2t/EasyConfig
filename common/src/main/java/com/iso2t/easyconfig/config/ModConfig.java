package com.iso2t.easyconfig.config;

import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.value.wrappers.BooleanValue;

@Config(name = "easyconfig")
public class ModConfig {

	@Comment("Display debug settings for EasyConfig during startup")
	public BooleanValue DEBUG = new BooleanValue(false);

}
