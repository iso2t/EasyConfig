package com.iso2t.easyconfig;

public class ColorProvider {

	public static String intToHex (int color) {
		return String.format("#%08X", (0xFFFFFF & color));
	}

	public static int hexToInt (String hex) {
		hex = hex.replace("#", "");
		return Integer.parseInt(hex, 16);
	}

}
