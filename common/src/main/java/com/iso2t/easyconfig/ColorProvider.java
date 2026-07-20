package com.iso2t.easyconfig;

public class ColorProvider {

	public static String intToHex (int color) {
		return String.format("#%08X", color);
	}

	public static int hexToInt (String hex) {
		hex = hex.replace("#", "");
		if (hex.length() == 6) hex = "FF" + hex;
		return (int) Long.parseLong(hex, 16);
	}

}
