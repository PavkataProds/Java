package bg.sofia.uni.fmi.mjt.cryptowallet.response;

import bg.sofia.uni.fmi.mjt.cryptowallet.CurrencyCode;
import bg.sofia.uni.fmi.mjt.cryptowallet.exception.FailedRequestException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApiCall {
    private final HttpClient httpClient;
    private final String apiKey;
    private static final int MINUTES_OF_RESPONSE_VALIDITY = 30;
    private static final int MAX_RESULTS = 2000;
    private static final double MINIMUM_PRICE_FOR_ONE = 0.0001;
    private static final int MAXIMUM_PRICE_FOR_ONE = 250_000;
    private static final int BAD_REQUEST_CODE = 400;
    private static final int INTERNET_SERVER_ERROR_CODE = 500;
    private Map<CurrencyCode, BigDecimal> marketChart;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ApiCall(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.marketChart = new HashMap<>();
    }

    public Map<CurrencyCode, BigDecimal> getMarketChart() throws FailedRequestException {
        if (marketChart.isEmpty()) {
            makeApiCall(new Query(apiKey));
        }

        return marketChart;
    }

    private void makeApiCall(Query query) throws FailedRequestException {
        try {
            URI uriAll = query.constructUri();

            HttpRequest request = HttpRequest.newBuilder().uri(uriAll).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int responseCode = response.statusCode();
            System.out.println(uriAll);
            System.out.println(responseCode);

            handleResponseCode(responseCode);

            JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

            fetchMarketChart(jsonArray);

            scheduler.schedule(() -> scheduleApiDeletion(), MINUTES_OF_RESPONSE_VALIDITY, TimeUnit.MINUTES);

        } catch (FailedRequestException e) {
            throw new FailedRequestException(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getClass());
            throw new RuntimeException("An error has occurred when making a query to API");
        }
    }

    private void fetchMarketChart(JsonArray jsonArray) {
        int currencyCounter = 0;
        //System.out.println("JSON Array Size: " + jsonArray.size());
        while (currencyCounter < jsonArray.size() && currencyCounter < MAX_RESULTS) {
            JsonObject current = (JsonObject) jsonArray.get(currencyCounter++);
            //System.out.println("Processing item: " + current);

            if (current.get("type_is_crypto").getAsInt() != 1 || !current.has("price_usd")
                    || Double.compare(current.get("price_usd").getAsDouble(), MINIMUM_PRICE_FOR_ONE) < 0
                    || Double.compare(current.get("price_usd").getAsDouble(), MAXIMUM_PRICE_FOR_ONE) > 0) {
                //System.out.println("Skipping item: " + current);
                continue;
            }

            try {
                CurrencyCode currency = CurrencyCode.valueOf(current.get("asset_id").getAsString());
                BigDecimal price = current.get("price_usd").getAsBigDecimal();
                marketChart.put(currency, price);
            } catch (IllegalArgumentException e) {
                //System.out.println("Invalid currency code: " + current.get("asset_id").getAsString());
            }
        }
    }


    private void handleResponseCode(int responseCode) throws FailedRequestException {
        if (responseCode >= BAD_REQUEST_CODE && responseCode < INTERNET_SERVER_ERROR_CODE) {
            throw new FailedRequestException("Could not fetch error because of a client side error");
        } else if (responseCode >= INTERNET_SERVER_ERROR_CODE) {
            throw new FailedRequestException("Could not fetch error because of a server side error");
        }
    }

    private void scheduleApiDeletion() {
        marketChart.clear();
    }

    public void shutdownScheduler() {
        scheduler.shutdownNow();
    }

//    public static void main(String[] args) {
//        String apiKey = "E92452D2-DF77-4022-8A8A-E2A4CFB06D51";
//        HttpClient httpClient = HttpClient.newHttpClient();
//
//        ApiCall apiCall = new ApiCall(httpClient, apiKey);
//
//        try {
//            Map<CurrencyCode, BigDecimal> marketChart = apiCall.getMarketChart();
//
//            System.out.println("Market Chart:");
//            for (Map.Entry<CurrencyCode, BigDecimal> entry : marketChart.entrySet()) {
//                System.out.printf("Currency: %s, Price: %s USD%n", entry.getKey(), entry.getValue());
//            }
//
//        } catch (FailedRequestException e) {
//            System.err.println("Failed to fetch market chart: " + e.getMessage());
//        } catch (RuntimeException e) {
//            System.err.println("Unexpected error: " + e.getMessage());
//        } finally {
//            apiCall.shutdownScheduler();
//        }
//    }

}
