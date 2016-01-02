package me.kenzierocks.plugins.currencysnowmen;

import java.math.BigDecimal;
import java.util.Optional;

import org.spongepowered.api.service.economy.Currency;

public interface ExtendedCurrency extends Currency {
    
    boolean supportsNegatives();
    
    Optional<BigDecimal> getMaximumAccountBalance();

    BigDecimal getDefaultBalance();
    
    String getIdentifer();

}
