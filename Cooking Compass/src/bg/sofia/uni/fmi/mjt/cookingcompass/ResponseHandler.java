package bg.sofia.uni.fmi.mjt.cookingcompass;

import bg.sofia.uni.fmi.mjt.cookingcompass.exceptions.MissingJsonException;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record ResponseHandler(List<Recipe> hits, String nextUri) {
    public static ResponseHandler of(JsonObject jsonObject) throws MissingJsonException {
        if (jsonObject == null || jsonObject.get("hits") == null) {
            throw new MissingJsonException("Response exception");
        }
        List<JsonElement> hitsJ = jsonObject.get("hits").getAsJsonArray().asList();
        List<Recipe> hits = new ArrayList<>();
        for (JsonElement je : hitsJ) {
            hits.add(Recipe.of(je.getAsJsonObject().get("recipe").getAsJsonObject()));
        }
        JsonElement j = jsonObject.get("_links").getAsJsonObject().get("next").getAsJsonObject().get("href");
        if (j == null) {
            throw new MissingJsonException("Next URI not found.");
        }
        String nextUri = j.getAsString();

        return new ResponseHandler(hits, nextUri);
    }
}