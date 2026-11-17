package io.github.tavstaldev.yggraHeads.config.models;

import io.github.tavstaldev.yggraLib.bootstrap.YggraConfig;
import lombok.Getter;

@Getter
public class GeneralConfig {
    private final String locale;
    private final boolean usePlayerLocale;
    private final boolean checkForUpdates;
    private final boolean debug;
    private final String prefix;
    private final boolean useEconomy;

    public GeneralConfig(YggraConfig parent) {
        locale = parent.resolveGet("general.locale", "eng");
        usePlayerLocale = parent.resolveGet("general.use-player-locale", true);
        checkForUpdates = parent.resolveGet("general.check-updates", true);
        debug = parent.resolveGet("general.debug-mode", false);
        prefix = parent.resolveGet("general.prefix", "&bYggra&3Heads &8»&7");
        useEconomy = parent.resolveGet("general.use-economy", false);
    }

    public GeneralConfig(String locale, boolean usePlayerLocale, boolean checkForUpdates, boolean debug,
                         String prefix, boolean useEconomy) {
        this.locale = locale;
        this.usePlayerLocale = usePlayerLocale;
        this.checkForUpdates = checkForUpdates;
        this.debug = debug;
        this.prefix = prefix;
        this.useEconomy = useEconomy;
    }
}
