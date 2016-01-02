package me.kenzierocks.plugins.currencysnowmen.implementation;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.api.text.Text;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import me.kenzierocks.plugins.currencysnowmen.extensions.ExtendedCurrency;
import me.kenzierocks.plugins.currencysnowmen.implementation.Transactionals.TRData;
import me.kenzierocks.plugins.currencysnowmen.service.CSEconomyService;

public class CSAccount implements Account {

    private static TransactionResult handleAction(Cause cause, TRData data,
            Supplier<TransactionResult> provideInitialState,
            Supplier<TransactionResult> ifSuccessful) {
        TransactionResult result = provideInitialState.get();
        EconomyTransactionEvent transaction =
                Transactionals.createEvent(cause, result);
        boolean canceled = Sponge.getEventManager().post(transaction);
        if (canceled) {
            return Transactionals.fail(data);
        }
        if (result.getResult() == ResultType.SUCCESS) {
            result = ifSuccessful.get();
        }
        return result;
    }

    private transient final CSEconomyService runningService =
            Sponge.getServiceManager().provideUnchecked(CSEconomyService.class);

    private final String id;
    private final Text displayName;
    private final Table<Currency, Set<Context>, BigDecimal> currencyTable =
            HashBasedTable.create();

    protected CSAccount(String id) {
        this(id, Text.of(id));
    }

    protected CSAccount(String id, Text displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    private TransactionResult handleNonTransfer(Cause cause, BigDecimal from,
            BigDecimal to, Currency currency, Set<Context> contexts) {
        BigDecimal delta = to.subtract(from);
        TransactionType type = from.compareTo(to) > 0
                ? TransactionTypes.WITHDRAW : TransactionTypes.DEPOSIT;
        TRData data = new TRData(this, currency, delta, contexts, type);
        return handleAction(cause, data, () -> {
            if (currency instanceof ExtendedCurrency) {
                ExtendedCurrency extCur = (ExtendedCurrency) currency;
                if (!extCur.supportsNegatives()
                        && to.compareTo(BigDecimal.ZERO) < 0) {
                    return Transactionals.failNoFunds(data);
                }
                if (extCur.getMaximumAccountBalance()
                        .filter(max -> max.compareTo(to) < 0).isPresent()) {
                    return Transactionals.failMaxSize(data);
                }
            }
            return Transactionals.success(data);
        }, () -> {
            this.currencyTable.put(currency, contexts, to);
            return Transactionals.success(data);
        });
    }

    private TransactionResult handleTransfer(Cause cause, BigDecimal from,
            BigDecimal to, Currency currency, Set<Context> contexts,
            Account target) {
        BigDecimal delta = to.subtract(from);
        TransactionType type = from.compareTo(to) > 0
                ? TransactionTypes.WITHDRAW : TransactionTypes.DEPOSIT;
        TRData data = new TRData(this, currency, delta, contexts, type);
        if (!(target instanceof CSAccount)) {
            return Transactionals.fail(data);
        }
        CSAccount that = (CSAccount) target;
        BigDecimal thisAccNewVal = to;
        BigDecimal thatAccNewVal =
                that.getBalanceOrDefault(currency, contexts).subtract(delta);
        return handleAction(cause, data, () -> {
            if (currency instanceof ExtendedCurrency) {
                ExtendedCurrency extCur = (ExtendedCurrency) currency;
                if (!extCur.supportsNegatives() && (thisAccNewVal
                        .compareTo(BigDecimal.ZERO) < 0
                        || thatAccNewVal.compareTo(BigDecimal.ZERO) < 0)) {
                    return Transactionals.failNoFunds(data);
                }
                Optional<BigDecimal> maxBal = extCur.getMaximumAccountBalance();
                if (maxBal.filter(max -> max.compareTo(thisAccNewVal) < 0)
                        .isPresent()
                        || maxBal
                                .filter(max -> max.compareTo(thatAccNewVal) < 0)
                                .isPresent()) {
                    return Transactionals.failMaxSize(data);
                }
            }
            return Transactionals.success(data);
        }, () -> {
            this.currencyTable.put(currency, contexts, thisAccNewVal);
            that.currencyTable.put(currency, contexts, thatAccNewVal);
            return Transactionals.success(data);
        });
    }

    @Override
    public String getIdentifier() {
        return this.id;
    }

    @Override
    public Set<Context> getActiveContexts() {
        Set<ContextCalculator<Account>> ccs =
                this.runningService.getContextCalculators();
        Set<Context> contexts = new HashSet<>();
        for (ContextCalculator<Account> contextCalculator : ccs) {
            contextCalculator.accumulateContexts(this, contexts);
        }
        return contexts.stream()
                .filter(ctxt -> ccs.stream()
                        .anyMatch(calc -> calc.matches(ctxt, this)))
                .collect(Collectors.toSet());
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Override
    public BigDecimal getDefaultBalance(Currency currency) {
        if (currency instanceof ExtendedCurrency) {
            return ((ExtendedCurrency) currency).getDefaultBalance();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean hasBalance(Currency currency, Set<Context> contexts) {
        return this.currencyTable.contains(currency, contexts);
    }

    private BigDecimal getBalanceOrDefault(Currency currency,
            Set<Context> contexts) {
        return this.currencyTable.row(currency).getOrDefault(contexts,
                getDefaultBalance(currency));
    }

    @Override
    public BigDecimal getBalance(Currency currency, Set<Context> contexts) {
        return this.currencyTable.row(currency).getOrDefault(contexts,
                BigDecimal.ZERO);
    }

    @Override
    public Map<Currency, BigDecimal> getBalances(Set<Context> contexts) {
        return this.currencyTable.column(contexts);
    }

    @Override
    public TransactionResult setBalance(Currency currency, BigDecimal amount,
            Cause cause, Set<Context> contexts) {
        return handleNonTransfer(cause, getBalance(currency, contexts), amount,
                currency, contexts);
    }

    @Override
    public TransactionResult resetBalances(Cause cause, Set<Context> contexts) {
        boolean allOk = true;
        // TODO wait for the econ api to not suck
        TRData fakedData =
                this.currencyTable.rowMap().entrySet().stream().findFirst()
                        .map(e -> new TRData(this, e.getKey(),
                                getDefaultBalance(e.getKey()), contexts,
                                TransactionTypes.WITHDRAW))
                        .orElse(null);
        for (Currency currency : this.currencyTable.rowKeySet()) {
            if (!hasBalance(currency, contexts)) {
                // don't reset balances that don't exist
                continue;
            }
            allOk &= setBalance(currency, getDefaultBalance(currency), cause,
                    contexts).getResult() == ResultType.SUCCESS;
        }
        return fakedData == null ? null
                : (allOk ? Transactionals.success(fakedData)
                        : Transactionals.fail(fakedData));
    }

    @Override
    public TransactionResult resetBalance(Currency currency, Cause cause,
            Set<Context> contexts) {
        if (!hasBalance(currency, contexts)) {
            TRData data = new TRData(this, currency, BigDecimal.ZERO, contexts,
                    TransactionTypes.WITHDRAW);
            return Transactionals.success(data);
        }
        return setBalance(currency, getDefaultBalance(currency), cause,
                contexts);
    }

    @Override
    public TransactionResult deposit(Currency currency, BigDecimal amount,
            Cause cause, Set<Context> contexts) {
        BigDecimal start = getBalanceOrDefault(currency, contexts);
        return handleNonTransfer(cause, start, start.add(amount), currency,
                contexts);
    }

    @Override
    public TransactionResult withdraw(Currency currency, BigDecimal amount,
            Cause cause, Set<Context> contexts) {
        BigDecimal start = getBalanceOrDefault(currency, contexts);
        return handleNonTransfer(cause, start, start.subtract(amount), currency,
                contexts);
    }

    @Override
    public TransferResult transfer(Account to, Currency currency,
            BigDecimal amount, Cause cause, Set<Context> contexts) {
        BigDecimal start = getBalanceOrDefault(currency, contexts);
        TransactionResult res = handleTransfer(cause, start,
                start.subtract(amount), currency, contexts, to);
        return Transactionals.transfer(res, to);
    }

}
