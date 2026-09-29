<p align="center"><img src="https://raw.githubusercontent.com/iso2t/EasyConfig/refs/heads/master/common/src/main/resources/easyconfig.png" alt="Logo"></p>

<p align="center">A class-based config API and in-game config screen for Minecraft mods.</p>

EasyConfig makes it easy to create, modify, and share config files for your mod. With an easily accessible in-game
config screen, users can adjust settings on the fly
without having to manually edit config files.

EasyConfig naturally integrates with NeoForge, not replacing their configs, but giving a cleaner alternative. For
Fabric,
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
    implementation "com.iso2t.easyconfig:easyconfig-fabric-26.3:1.263.0.8"
}
```

NeoForge:

```gradle
dependencies {
    implementation "com.iso2t.easyconfig:easyconfig-neoforge-26.3:1.263.0.8"
}
```

API only:

```gradle
dependencies {
    implementation "com.iso2t.easyconfig:api:1.263.4.1"
}
```

The Fabric and NeoForge artifacts include the API classes in the built mod jar. Use the standalone `api` artifact only
when you want the Java config API without EasyConfig's Minecraft mod implementation or in-game screens.

## Creating a Config

```java
import com.iso2t.easyconfig.api.Side;
import com.iso2t.easyconfig.api.annotations.Comment;
import com.iso2t.easyconfig.api.annotations.Ignore;
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
	
    @Ignore // Ignored by the config builder
    public IntegerValue maxAttempts = IntegerValue.of(10);

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

This loads the config, writes missing values, and saves comments. When using the Fabric or NeoForge artifact, it also
registers the config screen.

Screen registration is handled by the EasyConfig mod artifacts. If you use the standalone Java API outside Minecraft,
configure the config directory through `ConfigPlatform`.

```java
import com.iso2t.easyconfig.api.ConfigPlatform;
import java.nio.file.Path;

ConfigPlatform.configure(Path.of("config"), modId -> {});
```

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

Config screens are part of the Minecraft implementation, not the standalone Java API.

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

## Screens for externally managed settings

Controls are generated from annotated classes. `@Translation` supplies a label key,
an optional tooltip key, and an optional prefix for lowercase enum value names.
Nested classes create sections; typed value wrappers supply defaults and bounds.

```java
@Config(name = "flight", side = Side.CLIENT)
public class FlightConfig {
    @Translation(value = "my_mod.lift", tooltip = "my_mod.lift.tooltip")
    public final FloatValue lift = FloatValue.of(.15f, 0f, 1f);
}

var tab = new ConfigScreenTab<>(title, ConfigIntrospector.inspect(draft),
        this::saveSettings, () -> ConfigIntrospector.inspect(loadDraft()));
var screen = new ConfigScreen(parent, title, List.of(tab));
```

Use an independent draft when changes must wait for Save. The save callback receives
the current draft; reload replaces it. `validation(config -> Optional<Component>)`
can reject relationships between fields, and `editable(() -> allowed)` disables
editing and saving for read-only views. File storage and networking remain with the
consumer when using these callbacks. Normal `ConfigBuilder` registration continues
to use EasyConfig's file-backed manager.

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

For validation across multiple fields, call `load()`, validate the returned object,
then call `save(config)`. Scalar wrapper values are checked against their types and
ranges during loading; invalid values fail instead of being replaced silently.
Saves replace the file after serialization succeeds.

Useful methods:

- `load()`
- `loadAndSave()`
- `save(config)`
- `loadInto(config)`
- `loadAndSaveInto(config)`
- `schema(config)`

## License

EasyConfig is licensed under LGPL v3.0. See `LICENSE.md`.
