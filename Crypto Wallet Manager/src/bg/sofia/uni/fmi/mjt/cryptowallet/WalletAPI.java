package bg.sofia.uni.fmi.mjt.cryptowallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;

public interface WalletAPI {
    void convertBalance(CurrencyCode to, CurrencyCode from, BigDecimal amount)
            throws IOException, InterruptedException, URISyntaxException;

    void convertBalance(CurrencyCode to, CurrencyCode from)
            throws IOException, URISyntaxException, InterruptedException;
}
