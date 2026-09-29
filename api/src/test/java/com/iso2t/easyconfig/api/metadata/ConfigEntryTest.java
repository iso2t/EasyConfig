package com.iso2t.easyconfig.api.metadata;

import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.annotations.Ignore;
import com.iso2t.easyconfig.api.annotations.Translation;
import com.iso2t.easyconfig.api.value.wrappers.ColorValue;
import com.iso2t.easyconfig.api.value.wrappers.EnumValue;
import com.iso2t.easyconfig.api.value.wrappers.FloatValue;
import com.iso2t.easyconfig.api.value.wrappers.IntegerValue;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfigEntryTest {
	@Test
	void nestedFieldsConvertValidateAndReset () {
		var config = new Example();
		config.flight.lift.set(.2f);
		var entry = ConfigIntrospector.inspect(config).find("flight.lift").orElseThrow();
		assertNotNull(entry.field());
		assertEquals(List.of("flight", "lift"), entry.pathSegments());
		assertTrue(entry.trySetValue(".8").success());
		assertEquals(.8f, config.flight.lift.get());
		for (var invalid : List.of("NaN", "Infinity", "bad", "1.01", "-1")) assertTrue(entry.trySetValue(invalid).failed());
		assertEquals(.8f, entry.value());
		entry.resetValue();
		assertEquals(.15f, config.flight.lift.get());
		assertTrue(entry.isDefaultValue());
	}

	@Test
	void finalScalarsAreReadOnly () {
		var entry = ConfigIntrospector.inspect(new Example()).find("readonly").orElseThrow();
		assertEquals(1200f, entry.value());
		assertFalse(entry.editable());
		assertTrue(entry.trySetValue(900f).failed());
		assertTrue(entry.tryResetValue().failed());
	}

	@Test
	void integersRejectFractionsAndOverflow () {
		var entry = ConfigIntrospector.inspect(new Example()).find("offset").orElseThrow();
		assertTrue(entry.trySetValue("12").success());
		assertTrue(entry.trySetValue("12.5").failed());
		assertTrue(entry.trySetValue("2147483648").failed());
		assertEquals(12, entry.value());
	}

	@Test
	void numericConversionRejectsOverflowWithoutExplicitBounds () {
		var schema = ConfigIntrospector.inspect(new Example());
		assertTrue(schema.find("unboundedfloat").orElseThrow().trySetValue("1e100").failed());
		assertTrue(schema.find("unboundeddouble").orElseThrow().trySetValue("1e1000").failed());
	}

	@Test
	void enumsAndColorsUseTheirControls () {
		var config = new Example();
		var schema = ConfigIntrospector.inspect(config);
		var choice = schema.find("mode").orElseThrow();
		assertEquals(ConfigEntryKind.ENUM, choice.kind());
		assertTrue(choice.trySetValue("second").success());
		assertEquals(Mode.SECOND, config.mode.get());
		var color = schema.find("color").orElseThrow();
		assertEquals(ConfigEntryKind.COLOR, color.kind());
		assertTrue(color.trySetValue("#123456").success());
		assertEquals(0x123456, config.color.get() & 0xFFFFFF);
	}

	@Test
	void annotationsRemainAvailableWithoutExposingIgnoredState () {
		var schema = ConfigIntrospector.inspect(new Example());
		assertFalse(schema.syncable());
		assertTrue(schema.find("revision").isEmpty());
		assertEquals(1, schema.sections().size());
		var annotation = schema.find("flight.lift").orElseThrow().field().getAnnotation(Translation.class);
		assertEquals("config.example.lift", annotation.value());
		assertEquals("config.example.lift.tooltip", annotation.tooltip());
	}

	@Test
	void draftsAndDefaultsAreIndependent () {
		var first = new Example();
		var second = new Example();
		first.flight.lift.set(.5f);
		var schema = ConfigIntrospector.inspect(first);
		assertEquals(.15f, second.flight.lift.get());
		assertEquals(.15f, schema.find("flight.lift").orElseThrow().defaultValue());
		assertEquals(.5f, first.flight.lift.get());
	}

	enum Mode {
		FIRST,
		SECOND
	}

	@Config(name = "example")
	public static class Example {
		public final Flight          flight          = new Flight();
		public final float           readOnly        = 1200f;
		public final IntegerValue    offset          = IntegerValue.of(7, 0, 64);
		public final EnumValue<Mode> mode            = EnumValue.of(Mode.FIRST);
		public final ColorValue      color           = ColorValue.of(0xFFFFFF);
		public       float           unboundedFloat  = 1f;
		public       double          unboundedDouble = 1d;
		@Ignore
		public       long            revision;
	}

	public static class Flight {
		@Translation(value = "config.example.lift", tooltip = "config.example.lift.tooltip")
		public final FloatValue lift = FloatValue.of(.15f, 0f, 1f);
	}
}
