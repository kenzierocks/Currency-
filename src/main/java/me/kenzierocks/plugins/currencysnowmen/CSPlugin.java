/*
 * This file is part of Currency☃, licensed under the MIT License (MIT).
 *
 * Copyright (c) kenzierocks (Kenzie Togami) <http://kenzierocks.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.plugins.currencysnowmen;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

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
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.base.Supplier;
import com.google.inject.Inject;

import me.kenzierocks.plugins.currencysnowmen.implementation.CSAccount;
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
                    Supplier<UniqueAccount> acc = () -> econService
                            .createAccount(((Player) src).getUniqueId()).get();
                    if (sw.equals("version")) {
                        src.sendMessage(
                                Text.of("Running " + NAME + " v" + VERSION));
                    } else if (sw.equals("add") && src instanceof Player) {
                        UniqueAccount a = acc.get();
                        a.deposit(SnowballCurrency.INSTANCE, BigDecimal.ONE,
                                Cause.of(src, this));
                        src.sendMessage(Text.of(src.getName() + " now has "
                                + a.getBalance(SnowballCurrency.INSTANCE)));
                    } else if (sw.equals("sub") && src instanceof Player) {
                        UniqueAccount a = acc.get();
                        a.withdraw(SnowballCurrency.INSTANCE, BigDecimal.ONE,
                                Cause.of(src, this));
                        src.sendMessage(Text.of(src.getName() + " now has "
                                + a.getBalance(SnowballCurrency.INSTANCE)));
                    } else if (sw.equals("flush")) {
                        CSEconomyService.INSTANCE.getAccounts()
                                .forEach(a -> ((CSAccount) a).save());
                        CSEconomyService.INSTANCE.getAccounts()
                                // collect-stream to prevent co-mod exception
                                .collect(Collectors.toList()).stream()
                                .map(Account::getIdentifier).forEach(
                                        CSEconomyService.INSTANCE::removeAccount);
                        src.sendMessage(Text.of(
                                "Flushed all accounts. All data will be loaded from disk."));
                    } else {
                        src.sendMessage(
                                Text.of(TextColors.RED, "Nothin' doin'"));
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
