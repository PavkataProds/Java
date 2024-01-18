package bg.sofia.uni.fmi.mjt.trading.stock;

import java.time.LocalDateTime;

public class StockPurchaseClass implements StockPurchase {
    private final String ticker;
    private final int quantity;
    private final LocalDateTime purchaseTimestamp;
    private final double purchasePricePerUnit;

    public StockPurchaseClass(String ticker, int quantity, LocalDateTime purchaseTimestamp, double purchasePricePerUnit) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.purchaseTimestamp = purchaseTimestamp;
        this.purchasePricePerUnit = purchasePricePerUnit;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDateTime getPurchaseTimestamp() {
        return purchaseTimestamp;
    }

    public double getPurchasePricePerUnit() {
        return purchasePricePerUnit;
    }

    public double getTotalPurchasePrice() {
        return purchasePricePerUnit * quantity;
    }

    public String getStockTicker() {
        return ticker;
    }
}