package bg.sofia.uni.fmi.mjt.cookingcompass;

import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.HealthLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.MealType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HttpCallerTest {

    @Test
    void getRecipesSync_withValidInput_shouldReturnListOfRecipes() {
        HttpCaller httpCaller = new HttpCaller();
        try {
            List<Recipe> recipes = httpCaller.getRecipesSync("pasta", MealType.LUNCH, HealthLabel.VEGETARIAN, 1);
            assertNotNull(recipes);
            assertFalse(recipes.isEmpty());
        } catch (Exception e) {
            fail("Exception thrown while fetching recipes");
        }
    }

    @Test
    void getRecipesSync_withInvalidInput_shouldThrowException() {
        HttpCaller httpCaller = new HttpCaller();
        assertThrows(IllegalArgumentException.class, () -> httpCaller.getRecipesSync(null, null, null, 1));
    }
}
