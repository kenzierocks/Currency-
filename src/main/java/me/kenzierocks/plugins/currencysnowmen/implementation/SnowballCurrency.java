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
