package bg.sofia.uni.fmi.mjt.cookingcompass.recipe;

import bg.sofia.uni.fmi.mjt.cookingcompass.exceptions.MissingJsonException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RecipeTest {

    @Test
    void testOfValidJsonObject() throws MissingJsonException {
        JsonObject jsonObject = createValidJsonObject();

        Recipe recipe = Recipe.of(jsonObject);
        assertNotNull(recipe);
        assertEquals("Chicken Vesuvio", recipe.label());
    }

    @Test
    void testOfNullJsonObject() {
        JsonObject jsonObject = null;

        assertThrows(MissingJsonException.class, () -> Recipe.of(jsonObject));
    }

    @Test
    void testOfMissingLabelInJsonObject() {
        JsonObject jsonObject = new JsonObject();

        assertThrows(MissingJsonException.class, () -> Recipe.of(jsonObject));
    }

    private JsonObject createValidJsonObject() {
        Gson gson = new Gson();
        return gson.fromJson("{\"label\":\"Chicken Vesuvio\",\"dietLabels\":[\"Low-Carb\"]," +
                "\"healthLabels\":[\"Mollusk-Free\",\"Kosher\"],\"ingredientLines\":[\"1/2 cup olive oil\"," +
                "\"1 cup frozen peas, thawed\"],\"totalWeight\":2976.850615011728,\"cuisineType\":[\"italian\"]," +
                "\"mealType\":[\"lunch/dinner\"],\"dishType\":[\"main course\"]}", JsonObject.class);
    }
}
