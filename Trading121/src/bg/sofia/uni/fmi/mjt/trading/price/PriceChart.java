package bg.sofia.uni.fmi.mjt.trading.price;

public class PriceChart implements PriceChartAPI {
    private double microsoftStockPrice;
    private double googleStockPrice;
    private double amazonStockPrice;

    public PriceChart(double microsoftStockPrice, double googleStockPrice, double amazonStockPrice) {
        this.microsoftStockPrice = microsoftStockPrice;
        this.googleStockPrice = googleStockPrice;
        this.amazonStockPrice = amazonStockPrice;
    }

    public double getCurrentPrice(String stockTicker) {
        if (stockTicker == null) return 0;
        if (stockTicker.equals("MSFT")) return (double) Math.round(microsoftStockPrice * 100) / 100;
        if (stockTicker.equals("AMZ")) return (double) Math.round(amazonStockPrice * 100) / 100;
        if (stockTicker.equals("GOOG")) return (double) Math.round(googleStockPrice * 100) / 100;
        return 0;
    }

    public boolean changeStockPrice(String stockTicker, int percentChange) {
        if (percentChange <= 0 || stockTicker == null || !(stockTicker.equals("MSFT") || stockTicker.equals("AMZ") || stockTicker.equals("GOOG")))
            return false;

        if (stockTicker.equals("MSFT")) {
            microsoftStockPrice += (double) Math.round((microsoftStockPrice * percentChange)) / 100;
        }
        if (stockTicker.equals("AMZ")) {
            amazonStockPrice += (double) Math.round((amazonStockPrice * percentChange)) / 100;
        }
        if (stockTicker.equals("GOOG")) {
            googleStockPrice += (double) Math.round((googleStockPrice * percentChange)) / 100;
        }

        return true;
    }
}