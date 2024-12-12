package bg.sofia.uni.fmi.mjt.cryptowallet.response;

import bg.sofia.uni.fmi.mjt.cryptowallet.CurrencyCode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiCallTransaction {
    private static final String SCHEME = "https";
    private static final String AUTHORITY = "rest.coinapi.io";
    private static final String PATH = "/v1/exchangerate/";
    private static final String APP_KEY = "E92452D2-DF77-4022-8A8A-E2A4CFB06D51";
    //private static final String APP_KEY2 = "D71394C7-C614-4EC2-B72D-3B3B6453733C";
    private static final String RATE_PARAMETER = "rate";
    private static final String RATES_PARAMETER = "rates";
    private static final String ASSET_ID_QUOTE_PARAMETER = "asset_id_quote";

    private static URI generateUri(CurrencyCode to, CurrencyCode from) throws URISyntaxException {
        StringBuilder query = new StringBuilder(PATH);
        if (to != null) {
            query.append(to)
                    .append("/");
        }
        query.append(from.toString())
                .append("/apikey-")
                .append(APP_KEY);
        return new URI(SCHEME, AUTHORITY,
                query.toString(), null);
    }

    public static BigDecimal getPricesRateSync(CurrencyCode to, CurrencyCode from)
            throws IOException, InterruptedException, URISyntaxException {
        if (to == null || from == null) {
            throw new IllegalArgumentException();
        }
        if (to.equals(from)) {
            //http response also returns 1 but there's no need to request it
            return BigDecimal.valueOf(1);
        }
        URI uri = generateUri(to, from);
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        return jsonObject.get(RATE_PARAMETER).getAsBigDecimal();
    }

    public static String getPricesRateSync(CurrencyCode from)
            throws IOException, InterruptedException, URISyntaxException {
        if (from == null) {
            throw new IllegalArgumentException();
        }
        URI uri = generateUri(null, from);
        HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

        Gson gson = new Gson();
        JsonArray parsed = gson.fromJson(jsonObject.get(RATES_PARAMETER), JsonArray.class);
        StringBuilder result = new StringBuilder();
        for (JsonElement je : parsed) {
            String current = je.getAsJsonObject().get(ASSET_ID_QUOTE_PARAMETER).getAsString();
            if (CurrencyCode.contains(current)) {
                double rate = 1 / je.getAsJsonObject().get(RATE_PARAMETER).getAsDouble();
                result.append(current)
                        .append(":    ")
                        .append(rate)
                        .append(System.lineSeparator());
            }
        }
        return result.toString();
    }
}
