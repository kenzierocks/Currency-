package me.kenzierocks.plugins.currencysnowmen;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.inject.Inject;

import me.kenzierocks.plugins.currencysnowmen.implementation.CSEconomyService;
import me.kenzierocks.plugins.currencysnowmen.implementation.SnowballCurrency;

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
        CSEconomyService econService = CSEconomyService.INSTANCE;
        serviceManager.setProvider(this, EconomyService.class, econService);
        econService.registerCurrency(SnowballCurrency.INSTANCE);
        Sponge.getCommandManager().register(this,
                CommandSpec.builder().executor((src, args) -> {
                    String sw = args.<String> getOne("switch").orElse("");
                    UniqueAccount acc = econService
                            .createAccount(((Player) src).getUniqueId()).get();
                    if (sw.equals("version")) {
                        src.sendMessage(
                                Text.of("Running " + NAME + " v" + VERSION));
                    } else if (sw.equals("add") && src instanceof Player) {
                        acc.deposit(SnowballCurrency.INSTANCE, BigDecimal.ONE,
                                Cause.of(src, this));
                        src.sendMessage(Text.of(src.getName() + " now has "
                                + acc.getBalance(SnowballCurrency.INSTANCE)));
                    } else if (sw.equals("sub") && src instanceof Player) {
                        acc.withdraw(SnowballCurrency.INSTANCE, BigDecimal.ONE,
                                Cause.of(src, this));
                        src.sendMessage(Text.of(src.getName() + " now has "
                                + acc.getBalance(SnowballCurrency.INSTANCE)));
                    } else {
                        src.sendMessage(Text.of(TextColors.RED, "Nothin' doin'"));
                        return CommandResult.empty();
                    }
                    return CommandResult.success();
                }).arguments(GenericArguments.string(Text.of("switch")))
                        .build(),
                "currencysnowmen", "cs");
        this.logger.info("Loaded " + NAME + " v" + VERSION);
    }

    public Path getConfigDir() {
        return this.configDir;
    }

    public Path getAccountSerializationDir() {
        return this.configDir.resolve("accounts");
    }

}
