<p align="center"><img src="https://raw.githubusercontent.com/iso2t/EasyConfig/refs/heads/master/common/src/main/resources/easyconfig.png" alt="Logo"></p>

<p align="center">A class-based config API and in-game config screen for Minecraft mods.</p>

EasyConfig makes it easy to create, modify, and share config files for your mod. With an easily accessible in-game config screen, users can adjust settings on the fly
without having to manually edit config files.

EasyConfig naturally integrates with NeoForge, not replacing their configs, but giving a cleaner alternative. For Fabric,
EasyConfig integrates with ModMenu for in-game config access, while still remaining an optional dependency.

## What It Provides

- One config API for Fabric and NeoForge.
- In-game config screens for mods that use EasyConfig.
- Tabs for mods with more than one config.
- Fabric Mod Menu integration.
- Lower-level APIs for custom config loading, saving, and metadata.

## For Developers

Add the Maven repository:

```gradle
repositories {
    maven {
        name = "iso2t"
        url = "https://maven.iso2t.com/releases"
    }
}
```

Fabric:

```gradle
dependencies {
    modImplementation "com.iso2t.easyconfig:easyconfig-fabric-[minecraft version]:[mod version]"
}
```

NeoForge:

```gradle
dependencies {
    implementation "com.iso2t.easyconfig:easyconfig-neoforge-[minecraft version]:[mod version]"
}
```

API only:

```gradle
dependencies {
    compileOnly "com.iso2t.easyconfig:easyconfig-api-[minecraft version]:[mod version]"
}
```

## Creating a Config

```java
import com.iso2t.easyconfig.api.Side;
import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.Config;
import com.iso2t.easyconfig.api.value.wrappers.BooleanValue;
import com.iso2t.easyconfig.api.value.wrappers.EnumValue;
import com.iso2t.easyconfig.api.value.wrappers.IntegerValue;

@Config(name = "example", side = Side.COMMON)
public class ExampleConfig {
    @Comment(value = "Enable the feature", values = false)
    public BooleanValue enabled = BooleanValue.of(true);

    @Comment("Maximum entries to process")
    public IntegerValue maxEntries = IntegerValue.of(64, 1, 256);

    @Comment("Feature mode")
    public EnumValue<Mode> mode = EnumValue.of(Mode.NORMAL);

    public enum Mode {
        QUIET,
        NORMAL,
        AGGRESSIVE
    }
}
```

Build the config during mod initialization:

```java
import com.iso2t.easyconfig.api.ConfigBuilder;

public final class ExampleMod {
    public static final String MOD_ID = "examplemod";
    public static ExampleConfig CONFIG;

    public static void init() {
        CONFIG = ConfigBuilder.build(ExampleConfig.class, MOD_ID);
    }
}
```

This loads the config, writes missing values, saves comments, and registers the config screen.

## Config Files

Config file names come from `@Config`.

```java
@Config(name = "example", side = Side.CLIENT)
```

generates:

```text
example-client.json5
```

Side suffixes:

- `Side.COMMON`: no suffix
- `Side.CLIENT`: `-client`
- `Side.SERVER`: `-server`

JSON5 is the default format:

```java
ConfigBuilder.build(ExampleConfig.class, MOD_ID);
```

You can pass a file type explicitly:

```java
import com.iso2t.easyconfig.api.files.FileTypes;

ConfigBuilder.build(ExampleConfig.class, MOD_ID, FileTypes.JSON5);
ConfigBuilder.build(ExampleConfig.class, MOD_ID, FileTypes.TOML);
```

## Nested Sections

Nested classes become sections when they are config-like objects with a no-argument constructor.

```java
@Comment("Debug options")
public Debug debug = new Debug();

public static class Debug {
    @Comment(value = "Show debug output", values = false)
    public BooleanValue enabled = BooleanValue.of(false);
}
```

`@Comment` can be used on fields and nested sections.

## Config Screens

Configs created through `ConfigBuilder` are registered for screen support by default.

Disable screen registration:

```java
import com.iso2t.easyconfig.api.ConfigBuildOptions;

ConfigBuilder.build(
    ExampleConfig.class,
    MOD_ID,
    ConfigBuildOptions.unregistered()
);
```

Set a custom tab title:

```java
ConfigBuilder.build(
    ExampleConfig.class,
    MOD_ID,
    ConfigBuildOptions.defaults().screenTitle("Gameplay")
);
```

Multiple configs registered under the same mod id appear as tabs.

NeoForge uses the native mod-list config button. Fabric uses Mod Menu when it is installed.

## Manual Control

Use `ConfigManager` directly when you need a custom path or lifecycle:

```java
import com.iso2t.easyconfig.api.files.FileTypes;
import com.iso2t.easyconfig.api.manager.ConfigManager;

ConfigManager<ExampleConfig> manager = new ConfigManager<>(
    ExampleConfig.class,
    configPath,
    FileTypes.JSON5
);

ExampleConfig config = manager.loadAndSave();
manager.save(config);
```

Useful methods:

- `load()`
- `loadAndSave()`
- `save(config)`
- `loadInto(config)`
- `loadAndSaveInto(config)`
- `schema(config)`

## License

EasyConfig is licensed under LGPL v3.0. See `LICENSE.md`.
