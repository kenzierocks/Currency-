package me.kenzierocks.plugins.currencysnowmen.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.account.VirtualAccount;

import me.kenzierocks.plugins.currencysnowmen.extensions.ExtendedCurrency;
import me.kenzierocks.plugins.currencysnowmen.implementation.CSUniqueAccount;
import me.kenzierocks.plugins.currencysnowmen.implementation.CSVirtualAccount;

public class CSEconomyService implements EconomyService {

    private final Set<ContextCalculator<Account>> calculators = new HashSet<>();
    private final transient Set<ContextCalculator<Account>> calculatorsReadOnlyView =
            Collections.unmodifiableSet(this.calculators);
    private final Set<ExtendedCurrency> currencies = new HashSet<>();
    private final transient Set<Currency> readOnlyView =
            Collections.unmodifiableSet(this.currencies);
    private final Map<String, Account> accountMap = new HashMap<>();
    private ExtendedCurrency defaultC;

    public CSEconomyService() {
    }

    public void registerCurrency(ExtendedCurrency currency) {
        if (currency.isDefault()) {
            checkState(this.defaultC == null,
                    "already have a default currency");
            this.defaultC = currency;
        }
        this.currencies.add(currency);
    }

    public Set<ContextCalculator<Account>> getContextCalculators() {
        return this.calculatorsReadOnlyView;
    }

    @Override
    public void
            registerContextCalculator(ContextCalculator<Account> calculator) {
        this.calculators.add(calculator);
    }

    @Override
    public ExtendedCurrency getDefaultCurrency() {
        return checkNotNull(this.defaultC, "no default currency set");
    }

    @Override
    public Set<Currency> getCurrencies() {
        return this.readOnlyView;
    }

    @Override
    public Optional<UniqueAccount> getAccount(UUID uuid) {
        return Optional.ofNullable(this.accountMap.get(uuid.toString()))
                .filter(UniqueAccount.class::isInstance)
                .map(UniqueAccount.class::cast);
    }

    @Override
    public Optional<UniqueAccount> createAccount(UUID uuid) {
        return Optional
                .ofNullable(this.accountMap.computeIfAbsent(uuid.toString(),
                        k -> new CSUniqueAccount(uuid)))
                .filter(UniqueAccount.class::isInstance)
                .map(UniqueAccount.class::cast);
    }

    @Override
    public Optional<Account> getAccount(String identifier) {
        return Optional.ofNullable(this.accountMap.get(identifier));
    }

    @Override
    public Optional<VirtualAccount> createVirtualAccount(String identifier) {
        return Optional
                .ofNullable(this.accountMap.computeIfAbsent(identifier,
                        CSVirtualAccount::new))
                .filter(VirtualAccount.class::isInstance)
                .map(VirtualAccount.class::cast);
    }

}
