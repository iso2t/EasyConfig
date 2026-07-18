package com.iso2t.easyconfig;

import com.iso2t.easyconfig.platform.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class Constants {

    public static final String MOD_ID = "easyconfig";
    public static final String MOD_NAME = "EasyConfig";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

	public static final Path CONFIGDIR = Services.PLATFORM.getConfigDir();
	public static final Path MODSDIR = Services.PLATFORM.getModsDir();
	public static final Path GAMEDIR = Services.PLATFORM.getGameDir();

}