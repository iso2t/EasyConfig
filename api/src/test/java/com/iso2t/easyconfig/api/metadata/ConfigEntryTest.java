package com.iso2t.easyconfig.api.metadata;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ConfigEntryTest {
	@Test void draftsConvertValidateAndReset () {
		var entry = ConfigEntry.builder("flight.lift", .2f).defaultValue(.15f).range(0, 1).build();
		assertNull(entry.field());
		assertEquals(List.of("flight", "lift"), entry.pathSegments());
		assertTrue(entry.trySetValue(".8").success());
		assertEquals(.8f, entry.value());
		for (var invalid : List.of("NaN", "Infinity", "bad", "1.01", "-1")) assertTrue(entry.trySetValue(invalid).failed());
		assertEquals(.8f, entry.value());
		entry.resetValue();
		assertEquals(.15f, entry.value());
		assertTrue(entry.isDefaultValue());
	}
	@Test void readOnlyDraftsExposeValuesButCannotChange () {
		var entry = ConfigEntry.builder("weight", 1200f).defaultValue(1000f).editable(false).build();
		assertEquals(1200f, entry.value());
		assertFalse(entry.editable());
		assertTrue(entry.trySetValue(900f).failed());
		assertTrue(entry.tryResetValue().failed());
		assertEquals(1200f, entry.value());
	}
	@Test void integersRejectFractionsAndOverflow () {
		var entry = ConfigEntry.builder("offset", 7).range(0, 64).build();
		assertTrue(entry.trySetValue("12").success());
		assertTrue(entry.trySetValue("12.5").failed());
		assertTrue(entry.trySetValue("2147483648").failed());
		assertEquals(12, entry.value());
	}
	@Test void numericConversionRejectsOverflowWithoutExplicitBounds () {
		assertTrue(ConfigEntry.builder("float", 1f).build().trySetValue("1e100").failed());
		assertTrue(ConfigEntry.builder("double", 1d).build().trySetValue("1e1000").failed());
	}
	@Test void enumsAndColorsUseTheirControls () {
		var choice = ConfigEntry.builder("mode", Mode.FIRST).build();
		assertEquals(ConfigEntryKind.ENUM, choice.kind());
		assertTrue(choice.trySetValue("second").success());
		assertEquals(Mode.SECOND, choice.value());
		var color = ConfigEntry.builder("color", 0xFFFFFF).kind(ConfigEntryKind.COLOR).build();
		assertTrue(color.trySetValue("#123456").success());
		assertEquals(0x123456, (int) color.value() & 0xFFFFFF);
	}
	@Test void schemaRejectsDuplicatePathsAndInvalidDefaults () {
		var entry = ConfigEntry.builder("value", true).build();
		assertThrows(IllegalArgumentException.class, () -> ConfigSchema.ofEntries(List.of(entry, entry)));
		assertThrows(IllegalArgumentException.class, () -> ConfigEntry.builder("value", 1).defaultValue(100).range(0, 5).build());
		assertFalse(ConfigSchema.ofEntries(List.of(entry)).syncable());
	}
	@Test void reflectedConfigsRemainEditable () {
		var config = new Example();
		var schema = ConfigIntrospector.inspect(config);
		schema.find("value").orElseThrow().setValue("5");
		assertEquals(5, config.value);
		schema.find("value").orElseThrow().resetValue();
		assertEquals(2, config.value);
	}
	enum Mode { FIRST, SECOND }
	public static class Example { public int value = 2; }
}
