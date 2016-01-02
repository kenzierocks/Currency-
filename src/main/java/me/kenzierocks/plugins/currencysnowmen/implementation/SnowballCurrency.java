package me.kenzierocks.plugins.currencysnowmen.implementation;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Optional;

import org.spongepowered.api.text.Text;

import me.kenzierocks.plugins.currencysnowmen.ExtendedCurrency;

/**
 * An example of a currency implemented in Currency☃.
 */
public final class SnowballCurrency implements ExtendedCurrency {

    private static final Text DISPLAY_NAME = Text.of("Snowball");
    private static final Text DISPLAY_NAME_PLURAL = Text.of("Snowballs");
    private static final Text SYMBOL = Text.of("☃");
    private static final Text NEGATIVE_SYMBOL = Text.of("-☃");
    private static final DecimalFormat DECIMAL_FORMAT =
            new DecimalFormat("0.00");

    public static final SnowballCurrency INSTANCE = new SnowballCurrency();

    private SnowballCurrency() {
    }

    @Override
    public Text getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public Text getPluralDisplayName() {
        return DISPLAY_NAME_PLURAL;
    }

    @Override
    public Text getSymbol() {
        return SYMBOL;
    }

    private Text format(BigDecimal amount, DecimalFormat format) {
        return (amount.compareTo(BigDecimal.ZERO) >= 0 ? SYMBOL
                : NEGATIVE_SYMBOL).toBuilder()
                        .append(Text.of(format.format(amount.abs()))).build();
    }

    @Override
    public Text format(BigDecimal amount) {
        return format(amount, DECIMAL_FORMAT);
    }

    @Override
    public Text format(BigDecimal amount, int numFractionDigits) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(numFractionDigits);
        df.setMinimumFractionDigits(numFractionDigits);
        return format(amount, df);
    }

    @Override
    public int getDefaultFractionDigits() {
        return 2;
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    public boolean supportsNegatives() {
        return true;
    }

    @Override
    public Optional<BigDecimal> getMaximumAccountBalance() {
        return Optional.empty();
    }

    @Override
    public BigDecimal getDefaultBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public String getIdentifer() {
        return "snowballs";
    }

}
