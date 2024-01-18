package bg.sofia.uni.fmi.mjt.trading.stock;

import java.time.LocalDateTime;

public class GoogleStockPurchase extends StockPurchaseClass {
    public GoogleStockPurchase(int quantity, LocalDateTime purchaseTimestamp, double purchasePricePerUnit) {
        super("GOOG", quantity, purchaseTimestamp, purchasePricePerUnit);
    }
}