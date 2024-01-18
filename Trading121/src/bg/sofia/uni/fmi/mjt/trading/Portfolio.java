package bg.sofia.uni.fmi.mjt.trading;

import java.time.LocalDateTime;

import bg.sofia.uni.fmi.mjt.trading.price.PriceChartAPI;
import bg.sofia.uni.fmi.mjt.trading.stock.StockPurchase;
import bg.sofia.uni.fmi.mjt.trading.stock.AmazonStockPurchase;
import bg.sofia.uni.fmi.mjt.trading.stock.GoogleStockPurchase;
import bg.sofia.uni.fmi.mjt.trading.stock.MicrosoftStockPurchase;

import java.util.Arrays;

public class Portfolio implements PortfolioAPI {
    private double budget;
    private final int maxSize;
    private int currentSize;
    private final PriceChartAPI priceChart;
    private final StockPurchase[] stockPurchases;
    private final String owner;

    public Portfolio(String owner, PriceChartAPI priceChart, double budget, int maxSize) {
        this.budget = budget;
        this.priceChart = priceChart;
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.stockPurchases = new StockPurchase[maxSize];
        this.owner = owner;
    }

    public Portfolio(String owner, PriceChartAPI priceChart, StockPurchase[] stockPurchases, double budget, int maxSize) {
        this.budget = budget;
        this.priceChart = priceChart;
        this.maxSize = maxSize;
        this.currentSize = stockPurchases.length;
        this.stockPurchases = Arrays.copyOf(stockPurchases, maxSize);
        this.owner = owner;
    }

    public StockPurchase buyStock(String stockTicker, int quantity) {
        if (stockTicker == null || quantity <= 0 || stockPurchases.length == currentSize) return null;

        double currentPrice = priceChart.getCurrentPrice(stockTicker);

        if (currentPrice <= 0) return null;

        double updatedBudget = budget - (currentPrice * quantity);

        if (updatedBudget < 0) return null;

        this.budget = updatedBudget;
        priceChart.changeStockPrice(stockTicker, 5);

        stockPurchases[currentSize] = switch (stockTicker) {
            case "GOOG" -> new GoogleStockPurchase(quantity, LocalDateTime.now(), currentPrice);
            case "AMZ" -> new AmazonStockPurchase(quantity, LocalDateTime.now(), currentPrice);
            case "MSFT" -> new MicrosoftStockPurchase(quantity, LocalDateTime.now(), currentPrice);
            default -> null;
        };

        return stockPurchases[currentSize++];
    }

    public StockPurchase[] getAllPurchases() {
        return Arrays.copyOf(stockPurchases, currentSize);
    }

    public StockPurchase[] getAllPurchases(LocalDateTime startTimestamp, LocalDateTime endTimestamp) {
        int size = 0;

        for (int i = 0; i < currentSize; i++) {
            LocalDateTime timestamp = stockPurchases[i].getPurchaseTimestamp();
            if (timestamp.isBefore(endTimestamp) && timestamp.isAfter(startTimestamp)) {
                size++;
            }
        }

        StockPurchase[] stocks = new StockPurchase[size];
        if (size == 0) return stocks;

        int current = 0;
        for (int i = 0; i < currentSize; i++) {
            LocalDateTime timestamp = stockPurchases[i].getPurchaseTimestamp();
            if (timestamp.isBefore(endTimestamp) && timestamp.isAfter(startTimestamp)) {
                stocks[current++] = stockPurchases[i];
            }
        }

        return stocks;
    }

    public double getNetWorth() {
        double netWorth = 0;

        double currentGooglePrice = priceChart.getCurrentPrice("GOOG");
        double currentAmazonPrice = priceChart.getCurrentPrice("AMZ");
        double currentMicrosoftPrice = priceChart.getCurrentPrice("MSFT");

        for (int i = 0; i < currentSize; i++) {
            double addition;
            String ticker = stockPurchases[i].getStockTicker();
            if (ticker.equals("GOOG")) addition = stockPurchases[i].getQuantity() * currentGooglePrice;
            else if (ticker.equals("AMZ")) addition = stockPurchases[i].getQuantity() * currentAmazonPrice;
            else if (ticker.equals("MSFT")) addition = stockPurchases[i].getQuantity() * currentMicrosoftPrice;
            else addition = 0;
            netWorth += addition;
        }

        return netWorth;
    }

    public double getRemainingBudget() {
        return (double) Math.round(this.budget * 100) / 100;
    }

    public String getOwner() {
        return this.owner;
    }
}