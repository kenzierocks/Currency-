package me.kenzierocks.plugins.currencysnowmen.implementation;

import java.math.BigDecimal;
import java.util.Set;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransferResult;

final class Transactionals {

    public static class TRData {

        private final Account account;
        private final Currency currency;
        private final BigDecimal amount;
        private final Set<Context> contexts;
        private final TransactionType transactionType;

        public TRData(Account account, Currency currency, BigDecimal amount,
                Set<Context> contexts, TransactionType transactionType) {
            this.account = account;
            this.currency = currency;
            this.amount = amount;
            this.contexts = contexts;
            this.transactionType = transactionType;
        }

        public Account getAccount() {
            return this.account;
        }

        public Currency getCurrency() {
            return this.currency;
        }

        public BigDecimal getAmount() {
            return this.amount;
        }

        public Set<Context> getContexts() {
            return this.contexts;
        }

        public TransactionType getType() {
            return this.transactionType;
        }

    }

    private static class TransactionRImpl extends TRData
            implements TransactionResult {

        private final ResultType resultType;

        private TransactionRImpl(Account account, Currency currency,
                BigDecimal amount, Set<Context> contexts, ResultType resultType,
                TransactionType transactionType) {
            super(account, currency, amount, contexts, transactionType);
            this.resultType = resultType;
        }

        @Override
        public ResultType getResult() {
            return this.resultType;
        }

    }

    private static final class TransferRImpl extends TransactionRImpl
            implements TransferResult {

        private final Account accTo;

        private TransferRImpl(Account account, Currency currency,
                BigDecimal amount, Set<Context> contexts, ResultType resultType,
                TransactionType transactionType, Account accountTo) {
            super(account, currency, amount, contexts, resultType,
                    transactionType);
            this.accTo = accountTo;
        }

        @Override
        public Account getAccountTo() {
            return this.accTo;
        }

    }

    public static TransactionResult fail(TRData data) {
        return fail(data.getAccount(), data.getCurrency(), data.getAmount(),
                data.getContexts(), data.getType());
    }

    public static TransactionResult fail(Account acc, Currency cur,
            BigDecimal amount, Set<Context> context, TransactionType type) {
        return new TransactionRImpl(acc, cur, amount, context,
                ResultType.FAILED, type);
    }

    public static TransactionResult failNoFunds(TRData data) {
        return failNoFunds(data.getAccount(), data.getCurrency(),
                data.getAmount(), data.getContexts(), data.getType());
    }

    public static TransactionResult failNoFunds(Account acc, Currency cur,
            BigDecimal amount, Set<Context> context, TransactionType type) {
        return new TransactionRImpl(acc, cur, amount, context,
                ResultType.ACCOUNT_NO_FUNDS, type);
    }

    public static TransactionResult failMaxSize(TRData data) {
        return failMaxSize(data.getAccount(), data.getCurrency(),
                data.getAmount(), data.getContexts(), data.getType());
    }

    public static TransactionResult failMaxSize(Account acc, Currency cur,
            BigDecimal amount, Set<Context> context, TransactionType type) {
        return new TransactionRImpl(acc, cur, amount, context,
                ResultType.ACCOUNT_NO_SPACE, type);
    }

    public static TransactionResult failContextMismatch(TRData data) {
        return failContextMismatch(data.getAccount(), data.getCurrency(),
                data.getAmount(), data.getContexts(), data.getType());
    }

    public static TransactionResult failContextMismatch(Account acc,
            Currency cur, BigDecimal amount, Set<Context> context,
            TransactionType type) {
        return new TransactionRImpl(acc, cur, amount, context,
                ResultType.CONTEXT_MISMATCH, type);
    }

    public static TransactionResult success(TRData data) {
        return success(data.getAccount(), data.getCurrency(), data.getAmount(),
                data.getContexts(), data.getType());
    }

    public static TransactionResult success(Account acc, Currency cur,
            BigDecimal amount, Set<Context> context, TransactionType type) {
        return new TransactionRImpl(acc, cur, amount, context,
                ResultType.SUCCESS, type);
    }

    public static TransferResult transfer(TransactionResult res,
            Account accTo) {
        return new TransferRImpl(res.getAccount(), res.getCurrency(),
                res.getAmount(), res.getContexts(), res.getResult(),
                res.getType(), accTo);
    }

    public static EconomyTransactionEvent createEvent(Cause cause,
            TransactionResult result) {
        return new EconomyTransactionEvent() {

            @Override
            public Cause getCause() {
                return cause;
            }

            @Override
            public TransactionResult getTransactionResult() {
                return result;
            }

        };
    }

    private Transactionals() {
    }

}
