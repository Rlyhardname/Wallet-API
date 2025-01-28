package com.dimitrovsolutions.model.utils;

import com.dimitrovsolutions.model.component.SupportedCurrencies;
import com.dimitrovsolutions.model.dto.WalletDto;
import com.dimitrovsolutions.model.entity.Wallet;

import java.util.List;
import java.util.Optional;

/**
 * Maps Wallet to WalletDto objects and vise versa, Used mostly in service layer.
 */
public class WalletMapper {
    public static List<WalletDto> mapTo(List<Wallet> wallets) {
        return wallets.stream()
                .map(wallet -> new WalletDto(
                        wallet.getId().toString(),
                        wallet.getValue(),
                        wallet.getCurrency()))
                .toList();
    }

    public static WalletDto mapTo(Wallet wallet) {
        return new WalletDto(wallet.getAccountId().toString(), wallet.getValue(), wallet.getCurrency());
    }

    public static Wallet mapTo(Optional<Wallet> opt, String... currencyArgs) {
        String currency = currencyArgs.length == 0 ? SupportedCurrencies.DEFAULT_CURRENCY : currencyArgs[0];
        return opt.orElseThrow(
                () -> new MissingWalletException(currency)
        );
    }
}