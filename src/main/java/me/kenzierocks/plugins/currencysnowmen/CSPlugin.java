package me.kenzierocks.plugins.currencysnowmen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.economy.EconomyService;

import com.google.inject.Inject;

import me.kenzierocks.plugins.currencysnowmen.currencies.SnowballCurrency;
import me.kenzierocks.plugins.currencysnowmen.service.CSEconomyService;

@Plugin(id = CSPlugin.ID, name = CSPlugin.NAME, version = CSPlugin.VERSION)
public class CSPlugin {

    public static final String ID = "@ID@";
    public static final String NAME = "@NAME@";
    public static final String VERSION = "@VERSION@";
    private static CSPlugin INSTANCE;

    public static CSPlugin getInstance() {
        return INSTANCE;
    }

    @Inject
    private Logger logger;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDir;

    {
        INSTANCE = this;
    }

    private SpongeExecutorService executor;

    public Logger getLogger() {
        return this.logger;
    }

    public SpongeExecutorService getExecutor() {
        if (this.executor == null) {
            this.executor = Sponge.getScheduler().createSyncExecutor(this);
        }
        return this.executor;
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        this.logger.info("Loading " + NAME + " v" + VERSION);
        try {
            Files.createDirectories(this.configDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot use the plugin with no configs!",
                    e);
        }
        ServiceManager serviceManager = Sponge.getServiceManager();
        serviceManager.setProvider(this, EconomyService.class,
                new CSEconomyService());
        serviceManager.setProvider(this, CSEconomyService.class,
                new CSEconomyService());
        serviceManager.provideUnchecked(CSEconomyService.class)
                .registerCurrency(new SnowballCurrency());
        this.logger.info("Loaded " + NAME + " v" + VERSION);
    }

    public Path getConfigDir() {
        return this.configDir;
    }

}
