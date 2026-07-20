package com.iso2t.easyconfig.platform;

import com.iso2t.easyconfig.api.neoforge.NeoForgeConfigScreens;
import com.iso2t.easyconfig.api.registry.ConfigRegistry;
import com.iso2t.easyconfig.platform.services.IPlatformHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class NeoForgePlatformHelper implements IPlatformHelper {

	@Override
	public String getPlatformName () {
		return "NeoForge";
	}

	@Override
	public boolean isModLoaded (String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public List<String> getDependentMods (String modId) {
		return ModList.get().getMods().stream().filter(mod -> mod.getDependencies().stream().anyMatch(dependency -> dependency.getType() == IModInfo.DependencyType.REQUIRED && dependency.getModId().equals(modId))).map(IModInfo::getModId).distinct().toList();
	}

	@Override
	public boolean registerConfigScreen (String modId) {
		return registerConfigScreenFor(modId);
	}

	public static boolean registerConfigScreenFor (String modId) {
		Objects.requireNonNull(modId, "modId");
		if (FMLEnvironment.getDist() != Dist.CLIENT) return false;

		return ModList.get().getModContainerById(modId).map(container -> {
			NeoForgeConfigScreens.register(container);
			return true;
		}).orElse(false);
	}

	public static void registerKnownConfigScreens () {
		if (FMLEnvironment.getDist() != Dist.CLIENT) return;

		registerMinecraftOptionsScreen();
		ConfigRegistry.all().keySet().forEach(NeoForgePlatformHelper::registerConfigScreenFor);
	}

	public static boolean registerMinecraftOptionsScreen () {
		if (FMLEnvironment.getDist() != Dist.CLIENT) return false;

		return ModList.get().getModContainerById("minecraft").map(container -> {
			NeoForgeConfigScreens.registerMinecraftOptions(container);
			return true;
		}).orElse(false);
	}

	@Override
	public boolean isDevelopmentEnvironment () {
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
