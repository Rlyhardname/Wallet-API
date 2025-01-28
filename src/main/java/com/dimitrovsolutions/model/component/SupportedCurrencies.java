package com.dimitrovsolutions.model.component;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Used for validating currencies at controller layer.
 */
@Component
public class SupportedCurrencies {
    public static final String DEFAULT_CURRENCY = "EUR";

    List<String> currencies = Collections.synchronizedList(
            List.of("USD", "LEV", "EUR")
    );

    public boolean containsCurrency(String currency) {
        if (currency == null) {
            return false;
        }

        return currencies.contains(currency.toUpperCase());
    }
}