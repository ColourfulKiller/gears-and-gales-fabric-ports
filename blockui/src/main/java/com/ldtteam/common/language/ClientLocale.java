package com.ldtteam.common.language;

import net.minecraft.client.Minecraft;

public class ClientLocale
{
    public static String getLocale()
    {
        // Trust me, Minecraft.getInstance() can be null, when you run Data Generators!
        // Fabric: mod entrypoints run before the options are loaded; the fallback table is English until MC's own
        // language system takes over (LanguageHandler.setMClanguageLoaded)
        final Minecraft mc = Minecraft.getInstance();
        return mc == null || mc.options == null ? "en_us" : mc.options.languageCode;
    }
}
