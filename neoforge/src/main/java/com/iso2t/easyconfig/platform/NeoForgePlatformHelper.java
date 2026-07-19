package com.iso2t.easyconfig.platform;

import com.iso2t.easyconfig.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.List;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public List<String> getDependentMods(String modId) {
        return ModList.get().getMods().stream()
                .filter(mod -> mod.getDependencies().stream()
                        .anyMatch(dependency -> dependency.getType() == IModInfo.DependencyType.REQUIRED
                                && dependency.getModId().equals(modId)))
                .map(IModInfo::getModId)
                .distinct()
                .toList();
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }

	@Override
	public Path getConfigDir () {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public Path getModsDir () {
		return FMLPaths.MODSDIR.get();
	}

	@Override
	public Path getGameDir () {
		return FMLPaths.GAMEDIR.get();
	}
}
