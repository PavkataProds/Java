package bg.sofia.uni.fmi.mjt.cookingcompass;

import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.HealthLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.MealType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class HttpCaller implements HttpCallerAPI {
    private static final String SCHEME = "https";
    private static final String AUTHORITY = "api.edamam.com";
    private static final String PATH = "/api/recipes/v2";
    private static final String APP_ID = "1f4f807f";
    private static final String APP_KEY = "62aeffd6ef390c3c90d4620bc126801c";
    private static final int MAX_PAGES = 2;

    URI generateUri(String keyWord, MealType mealType, HealthLabel healthLabel) throws URISyntaxException {
        StringBuilder query = new StringBuilder("type=any");
        if (keyWord != null && !keyWord.isBlank()) {
            query.append("&q=").append(keyWord);
        }
        query.append("&app_id=" + APP_ID + "&app_key=" + APP_KEY);
        if (healthLabel != null) {
            query.append("&health=").append(healthLabel.getValue());
        }
        if (mealType != null) {
            query.append("&mealType=").append(mealType.getValue());
        }
        return new URI(SCHEME, AUTHORITY, PATH,
                query.toString(), null);
    }

    public List<Recipe> getRecipesSync(String keyWord, MealType mealType, HealthLabel healthLabel, int pages)
            throws Exception {
        if (mealType == null && healthLabel == null && (keyWord == null || keyWord.isBlank())) {
            throw new IllegalArgumentException();
        }
        pages = Math.min(pages, MAX_PAGES);

        HttpClient client = HttpClient.newBuilder().build();
        URI uri = generateUri(keyWord, mealType, healthLabel);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        String response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        ResponseHandler responseHandler = ResponseHandler.of(JsonParser.parseString(response).getAsJsonObject());
        List<Recipe> result = responseHandler.hits();

        while (pages > 1) {
            request = HttpRequest.newBuilder().uri(URI.create(responseHandler.nextUri())).build();
            response = client.send(request, HttpResponse.BodyHandlers.ofString()).body();
            responseHandler = ResponseHandler.of(JsonParser.parseString(response).getAsJsonObject());
            result.addAll(responseHandler.hits());
            pages--;
        }
        return result;
    }
}