package me.kenzierocks.plugins.currencysnowmen.implementation;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.account.VirtualAccount;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.util.Identifiable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import me.kenzierocks.plugins.currencysnowmen.ExtendedCurrency;

public class CSEconomyService implements EconomyService {

    public static final CSEconomyService INSTANCE = new CSEconomyService();

    private final Set<ContextCalculator<Account>> calculators = new HashSet<>();
    private final transient Set<ContextCalculator<Account>> calculatorsReadOnlyView =
            Collections.unmodifiableSet(this.calculators);
    private final BiMap<String, ExtendedCurrency> currencies =
            HashBiMap.create();
    private final transient Set<Currency> readOnlyView =
            Collections.unmodifiableSet(this.currencies.values());
    private final Map<String, Account> accountMap = new HashMap<>();
    private ExtendedCurrency defaultC;

    private CSEconomyService() {
    }

    public void registerCurrency(ExtendedCurrency currency) {
        if (currency.isDefault()) {
            checkState(this.defaultC == null,
                    "already have a default currency");
            this.defaultC = currency;
        }
        this.currencies.put(currency.getIdentifer(), currency);
    }
    
    public ExtendedCurrency getCurrencyByIdentifier(String id) {
        return this.currencies.get(id);
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
                        k -> createPotentialUserAccount(uuid)))
                .filter(UniqueAccount.class::isInstance)
                .map(UniqueAccount.class::cast);
    }

    private UniqueAccount createPotentialUserAccount(UUID uuid) {
        Identifiable ident = null;
        ident = Sponge.getServer().getPlayer(uuid).orElse(null);
        if (ident == null) {
            ident = Sponge.getServiceManager()
                    .provideUnchecked(UserStorageService.class).get(uuid)
                    .orElse(null);
        }
        return ident == null ? new CSUniqueAccount(uuid)
                : new CSUniqueAccount(ident);
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
