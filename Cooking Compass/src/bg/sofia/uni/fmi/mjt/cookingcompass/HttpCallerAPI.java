package bg.sofia.uni.fmi.mjt.cookingcompass;

import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.HealthLabel;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.MealType;
import bg.sofia.uni.fmi.mjt.cookingcompass.recipe.Recipe;

import java.util.List;

public interface HttpCallerAPI {
    List<Recipe> getRecipesSync(String keyWord, MealType mealType, HealthLabel healthLabel, int pages)
            throws Exception;
}