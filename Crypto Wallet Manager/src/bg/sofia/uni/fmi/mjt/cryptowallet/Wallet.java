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
    private final String DELIMITER = "/";
    private static final int BALANCE_INDEX = 4;
    private static final int FIRST_CRYPTO_INDEX = 5;
    private static final int WALLET_FROM_CSV_STEP = 2;

    public Wallet() {
        currencyBalance = new HashMap<>();
        currencyBalance.put(CurrencyCode.USD, BigDecimal.valueOf(0));
        total = BigDecimal.valueOf(0);
    }

    public Wallet(BigDecimal newTotal, Map<CurrencyCode, BigDecimal> loadCurrencyBalance) {
        total = newTotal;
        currencyBalance = loadCurrencyBalance;
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

    public static Wallet fromCSV(String[] tokens) {
        Map<CurrencyCode, BigDecimal> loadCurrencyBalance = new HashMap<>();

        for (int i = FIRST_CRYPTO_INDEX; i < tokens.length; i += WALLET_FROM_CSV_STEP) {
            loadCurrencyBalance.put(CurrencyCode.valueOf(tokens[i]),
                    BigDecimal.valueOf(Long.parseLong(tokens[i + 1])));
        }
        BigDecimal newTotal = BigDecimal.valueOf(Long.parseLong((tokens[BALANCE_INDEX])));

        return new Wallet(newTotal, loadCurrencyBalance);
    }

    public String toCSV() {
        StringBuilder result = new StringBuilder();

        result.append(DELIMITER).append(total);
        for (var entry : currencyBalance.entrySet()) {
            result.append(DELIMITER).append(entry.getKey()).append(DELIMITER).append(entry.getValue());
        }

        return result.toString();
    }
}
