package com.iso2t.easyconfig.client;

import com.iso2t.easyconfig.Constants;
import com.iso2t.easyconfig.EasyConfig;
import com.iso2t.easyconfig.config.ModConfig;
import com.iso2t.easyconfig.platform.Services;

import java.util.List;

public final class DebugMenuInfo {

	private DebugMenuInfo () {
	}

	public static void add (List<String> leftLines, List<String> rightLines) {
		var config = EasyConfig.getConfig();
		if (config == null || config.DEBUG == null || !Boolean.TRUE.equals(config.DEBUG.SHOW_IN_F3.get())) {
			return;
		}

		var target = config.DEBUG.F3_SIDE.get() == ModConfig.Debug.F3Side.LEFT ? leftLines : rightLines;
		addSeparator(target);

		var children = Services.PLATFORM.getDependentModCount(Constants.MOD_ID);
		target.add(String.format("%s (%s %s)", Constants.MOD_NAME, children, children == 1 ? "mod" : "mods"));
	}

	private static void addSeparator (List<String> lines) {
		if (!lines.isEmpty() && !lines.getLast().isBlank()) {
			lines.add("");
		}
	}

}
