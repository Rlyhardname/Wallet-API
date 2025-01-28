package com.dimitrovsolutions.controller;

import com.dimitrovsolutions.model.requests.*;
import com.dimitrovsolutions.model.dto.WalletDto;
import com.dimitrovsolutions.service.GlobalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class Controller {

    private final GlobalService globalService;
    private final Validator validator;

    public Controller(GlobalService globalService, Validator validator) {
        this.globalService = globalService;
        this.validator = validator;
    }

    @PostMapping("/users/new")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        validator.validateCredentials(request.getEmail(), request.getPassword());
        validator.validateEgn(request.getEgn());
        validator.validateNames(request.getFirstName(), request.getLastName());

        return globalService.registerUser(request.getEmail(), request.getPassword(), request.getEgn(),
                request.getFirstName(), request.getLastName());
    }

    @PostMapping("/wallets/new/{currency}")
    public ResponseEntity<String> addWallet(@RequestBody CredentialsRequest credentials,
                                            @PathVariable("currency") String currency) {
        validator.validateCredentials(credentials.getEmail(), credentials.getPassword());
        validator.validateCurrency(currency);

        return globalService.addWallet(credentials.getEmail(), credentials.getPassword(), currency);
    }

    @PostMapping("/wallets/{currency}")
    public ResponseEntity<?> getWallet(@RequestBody CredentialsRequest credentials,
                                       @PathVariable("currency") String currency) {
        validator.validateCredentials(credentials.getEmail(), credentials.getPassword());
        validator.validateCurrency(currency);

        return globalService.getWallet(credentials.getEmail(), credentials.getPassword(), currency);
    }

    @PostMapping("/wallets/")
    public List<WalletDto> getWallets(@RequestBody CredentialsRequest credentials) {
        validator.validateCredentials(credentials.getEmail(), credentials.getPassword());

        return globalService.getWallets(credentials.getEmail(), credentials.getPassword());
    }

    @PostMapping("/wallets/deposit/")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request) {
        validator.validateCredentials(request.getEmail(), request.getPassword());
        validator.validateCard(request.getCardHolderFirstName(),
                request.getCardHolderLastName(),
                request.getCardNumber(),
                request.getCsc(),
                request.getExpiryDate());
        validator.validateCurrency(request.getCurrency());
        validator.validateAmount(request.getAmount());

        return globalService.deposit(request.getEmail(), request.getPassword(),
                request.getCardNumber(), request.getCsc(),
                request.getCurrency(), request.getAmount());
    }

    @PostMapping("/wallets/withdraw/")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest request) {
        validator.validateCredentials(request.getEmail(), request.getPassword());
        validator.validateCard(request.getCardHolderFirstName(),
                request.getCardHolderLastName(),
                request.getCardNumber(),
                "withdraw",
                request.getExpiryDate());
        validator.validateCurrency(request.getCurrency());
        validator.validateAmount(request.getAmount());

        return globalService.withdraw(request.getEmail(), request.getPassword(),
                request.getCardNumber(),
                request.getCurrency(), request.getAmount());
    }
}