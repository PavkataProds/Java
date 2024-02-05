package bg.sofia.uni.fmi.mjt.cryptowallet;

public enum CurrencyCode {
    USD,
    EUR,
    BTC,
    ETH,
    USDT,
    BNB,
    SOL,
    XRP,
    ADA,
    AVAX,
    LINK,
    TRX,
    DOT,
    MATIC,
    ICP,
    XLM,
    INJ,
    FIL,
    VET,
    EGLD,
    RUNE,
    HNT,
    FTM,
    THETA,
    AXS,
    APE,
    MANA,
    EOS,
    CFX,
    ENJ,
    LUNC,
    ILV,
    GMT,
    COMP,
    ZIL,
    SHIB,
    LTC,
    DAI,
    BCH,
    UNI,
    LEO,
    ETC,
    ATOM,
    APT,
    IMX,
    OKB,
    TIA,
    NEAR,
    OP,
    TAO;

    public static String listOfferings() {
        StringBuilder answer = new StringBuilder();
        for (CurrencyCode cd : CurrencyCode.values()) {
            answer.append(cd).append(", ");
        }
        if (!answer.isEmpty()) {
            int length = answer.length();
            answer.deleteCharAt(length - 2);
            answer.deleteCharAt(length - 1);
        }
        return answer.toString();
    }

    public static boolean contains(String value) {
        for (CurrencyCode cd : CurrencyCode.values()) {
            if (cd.toString().equals(value)) {
                return true;
            }
        }
        return false;
    }
}

