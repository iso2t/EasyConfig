package com.iso2t.easyconfig.api.metadata;

import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.Ignore;
import com.iso2t.easyconfig.api.files.ConfigNode;
import com.iso2t.easyconfig.api.files.FileTypes;
import com.iso2t.easyconfig.api.files.Toml;
import com.iso2t.easyconfig.api.manager.ConfigManager;
import com.iso2t.easyconfig.api.value.wrappers.ColorValue;
import com.iso2t.easyconfig.api.value.wrappers.EnumValue;
import com.iso2t.easyconfig.api.value.wrappers.FloatValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TomlPersistenceTest {
	@TempDir
	Path directory;

	@Test
	void createsDefaultsCommentsAndReloadsTypedValues () throws Exception {
		var path = directory.resolve("nested/config.toml");
		var manager = new ConfigManager<>(Example.class, path, FileTypes.TOML);
		var config = manager.loadAndSave();
		assertTrue(Files.readString(path).contains("# Flight settings"));
		assertFalse(Files.readString(path).contains("revision"));
		config.flight.lift.set(.325f);
		config.mode.set(Mode.SECOND);
		config.color.setHex("#123456");
		manager.save(config);
		var loaded = manager.load();
		assertEquals(.325f, loaded.flight.lift.get());
		assertEquals(Mode.SECOND, loaded.mode.get());
		assertTrue(Files.readString(path).contains("mode = \"SECOND\""));
		assertTrue(Files.readString(path).contains("# Default: FIRST"));
		assertTrue(Files.readString(path).contains("# Default: 0.15"));
		assertTrue(Files.readString(path).contains("# Allowed values: FIRST, SECOND"));
		assertEquals(0xFF123456, loaded.color.get());
	}

	@Test
	void invalidValuesDoNotGetSilentlyReplaced () throws Exception {
		var path = directory.resolve("config.toml");
		var manager = new ConfigManager<>(Example.class, path, FileTypes.TOML);
		for (String text : new String[] { "mode = \"missing\"", "mode = 1", "[flight]\nlift = 2", "[flight]\nlift = \".5\"", "[flight]\nlift = nan", "flight = false", "color = \"invalid\"" }) {
			Files.writeString(path, text);
			assertThrows(IllegalArgumentException.class, manager::loadAndSave, text);
			assertEquals(text, Files.readString(path));
		}
	}

	@Test
	void emptyTomlAndMissingFieldsUseDefaults () throws Exception {
		var path = directory.resolve("config.toml");
		Files.writeString(path, "");
		var manager = new ConfigManager<>(Example.class, path, FileTypes.TOML);
		assertEquals(.15f, manager.loadAndSave().flight.lift.get());
		Files.writeString(path, "future = 17\n[flight]\nnote = \"keep\"");
		manager.loadAndSave();
		var saved = new Toml().read(path);
		assertEquals(17, ((Number) saved.get("future").rawValue()).intValue());
		assertEquals("keep", saved.get("flight").get("note").rawValue());
	}

	@Test
	void failedSerializationPreservesFileAndRemovesTemporaryFile () throws Exception {
		var path = directory.resolve("config.toml");
		Files.writeString(path, "original = true");
		var manager = new ConfigManager<>(Example.class, path, new Toml() {
			@Override
			public void write (Path target, ConfigNode root) throws IOException {
				Files.writeString(target, "partially written");
				throw new IOException("Simulated storage failure");
			}
		});
		assertThrows(IllegalStateException.class, () -> manager.save(new Example()));
		assertEquals("original = true", Files.readString(path));
		try (var files = Files.list(directory)) {
			assertEquals(1, files.count());
		}
	}

	@Test
	void invalidMemoryValuesCannotBeSaved () throws Exception {
		var path = directory.resolve("config.toml");
		var manager = new ConfigManager<>(Example.class, path, FileTypes.TOML);
		var config = manager.loadAndSave();
		String original = Files.readString(path);
		config.flight.lift.set(2f);
		assertThrows(IllegalArgumentException.class, () -> manager.save(config));
		assertEquals(original, Files.readString(path));
	}

	enum Mode {
		FIRST,
		SECOND;

		@Override
		public String toString () {
			return "Display " + name();
		}
	}

	public static class Example {
		@Comment("Flight settings")
		public Flight          flight = new Flight();
		@Comment("Flight mode")
		public EnumValue<Mode> mode   = EnumValue.of(Mode.FIRST);
		public ColorValue      color  = ColorValue.of(0xFFFFFFFF);
		@Ignore
		public long            revision;
	}

	public static class Flight {
		@Comment("Lift reduction")
		public FloatValue lift = FloatValue.of(.15f, 0f, 1f);
	}
}
