package bg.sofia.uni.fmi.mjt.cryptowallet;

import bg.sofia.uni.fmi.mjt.cryptowallet.exception.InsufficientBalanceException;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static bg.sofia.uni.fmi.mjt.cryptowallet.provider.HttpCaller.getPricesRateSync;

public class Wallet implements WalletAPI, Serializable {
    private final Map<CurrencyCode, BigDecimal> currencyBalance;
    private BigDecimal total;

    public Wallet() {
        currencyBalance = new HashMap<>();
        currencyBalance.put(CurrencyCode.USD, BigDecimal.valueOf(0));
        total = BigDecimal.valueOf(0);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Map<CurrencyCode, BigDecimal> currentCurrencyBalance() {
        return Collections.unmodifiableMap(currencyBalance);
    }

    public String currentCurrencyBalanceString() {
        StringBuilder result = new StringBuilder();
        for (CurrencyCode currencyCode : currencyBalance.keySet()) {
            if (BigDecimal.valueOf(0).compareTo(currencyBalance.get(currencyCode)) < 0) {
                result.append(currencyCode)
                        .append(": ")
                        .append(currencyBalance.get(currencyCode))
                        .append("\n");
            }
        }
        return result.toString();
    }

    public BigDecimal currentBalance() {
        BigDecimal result = BigDecimal.valueOf(0);
        for (CurrencyCode currencyCode : currencyBalance.keySet()) {
            result = result.add(currencyBalance.get(currencyCode));
        }
        return result;
    }

    public void convertBalance(CurrencyCode to, CurrencyCode from, BigDecimal amount)
            throws IOException, InterruptedException, URISyntaxException {
        if (to == null || from == null || !currencyBalance.containsKey(from)
                || amount.compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal oldAmount = currencyBalance.get(from);
        if (oldAmount.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }

        BigDecimal newAmount = amount.multiply(getPricesRateSync(from, to));
        currencyBalance.put(from, oldAmount.subtract(amount));
        if (currencyBalance.containsKey(to)) {
            currencyBalance.put(to, currencyBalance.get(to).add(newAmount));
        } else {
            currencyBalance.put(to, newAmount);
        }
    }

    public void convertBalance(CurrencyCode to, CurrencyCode from)
            throws IOException, URISyntaxException, InterruptedException {
        convertBalance(to, from, currencyBalance.get(from));
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(0)) <= 0) {
            throw new IllegalArgumentException();
        }
        currencyBalance.put(CurrencyCode.USD, currencyBalance.get(CurrencyCode.USD).add(amount));
        total = total.add(amount);
    }
}
