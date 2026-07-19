package com.iso2t.easyconfig.platform;

import com.iso2t.easyconfig.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModDependency;

import java.nio.file.Path;
import java.util.List;

public class FabricPlatformHelper implements IPlatformHelper {

	@Override
	public String getPlatformName () {
		return "Fabric";
	}

	@Override
	public boolean isModLoaded (String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public List<String> getDependentMods (String modId) {
		return FabricLoader.getInstance().getAllMods().stream()
				.filter(mod -> mod.getMetadata().getDependencies().stream()
						.anyMatch(dependency -> dependency.getKind() == ModDependency.Kind.DEPENDS
								&& dependency.getModId().equals(modId)))
				.map(mod -> mod.getMetadata().getId())
				.distinct()
				.toList();
	}

	@Override
	public boolean isDevelopmentEnvironment () {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public Path getConfigDir () {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	public Path getModsDir () {
		return Path.of(FabricLoader.getInstance().getGameDir().toString(), "mods");
	}

	@Override
	public Path getGameDir () {
		return FabricLoader.getInstance().getGameDir();
	}
}
