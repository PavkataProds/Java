package bg.sofia.uni.fmi.mjt.cookingcompass.recipe;

import bg.sofia.uni.fmi.mjt.cookingcompass.exceptions.MissingJsonException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public record Recipe(
        //If changing value names, use '@SerializedName("oldValueName")'
        String label,
        List<DietLabel> dietLabels,
        List<HealthLabel> healthLabels,
        List<String> ingredientLines,
        double totalWeight,
        List<Cuisine> cuisineType,
        List<MealType> mealType,
        List<DishType> dishType) {
    private static List<HealthLabel> getHealthLabels(List<JsonElement> jsonElements) {
        List<HealthLabel> healthLabels = new ArrayList<>();
        for (JsonElement je : jsonElements) {
            healthLabels.add(HealthLabel.valueOf(je.getAsString().toUpperCase().replace(' ', '_')
                    .replace('/', '_').replace('-', '_')));
        }
        return healthLabels;
    }

    private static List<String> getIngredientLines(List<JsonElement> jsonElements) {
        List<String> ingredientLines = new ArrayList<>();
        for (JsonElement je : jsonElements) {
            ingredientLines.add(je.getAsString());
        }
        return ingredientLines;
    }

    private static List<DietLabel> getDietLabels(List<JsonElement> jsonElements) {
        List<DietLabel> dietLabels = new ArrayList<>();
        for (JsonElement je : jsonElements) {
            dietLabels.add(DietLabel.valueOf(je.getAsString().toUpperCase().replace(' ', '_')
                    .replace('/', '_').replace('-', '_')));
        }
        return dietLabels;
    }

    private static List<Cuisine> getCuisineType(List<JsonElement> jsonElements) {
        List<Cuisine> cuisineType = new ArrayList<>();
        for (JsonElement je : jsonElements) {
            cuisineType.add(Cuisine.valueOf(je.getAsString().toUpperCase().replace(' ', '_')
                    .replace('/', '_').replace('-', '_')));
        }
        return cuisineType;
    }

    public static Recipe of(JsonObject jsonObject) throws MissingJsonException {
        if (jsonObject == null || jsonObject.get("label") == null) {
            throw new MissingJsonException("Response exception");
        }
        String label = jsonObject.get("label").getAsString();
        double totalWeight = jsonObject.get("totalWeight").getAsDouble();
        List<JsonElement> mealTypesJ = jsonObject.get("mealType").getAsJsonArray().asList();
        List<MealType> mealTypes = new ArrayList<>();
        for (JsonElement je : mealTypesJ) {
            mealTypes.add(MealType.valueOf(je.getAsString().toUpperCase().replace(' ', '_')
                    .replace('/', '_').replace('-', '_')));
        }
        List<JsonElement> dishTypesJ = jsonObject.get("dishType").getAsJsonArray().asList();
        List<DishType> dishTypes = new ArrayList<>();
        for (JsonElement je : dishTypesJ) {
            dishTypes.add(DishType.valueOf(je.getAsString().toUpperCase().replace(' ', '_')
                    .replace('/', '_').replace('-', '_')));
        }
        List<JsonElement> cuisineTypeJ = jsonObject.get("cuisineType").getAsJsonArray().asList();
        List<Cuisine> cuisineType = getCuisineType(cuisineTypeJ);
        List<JsonElement> dietLabelsJ = jsonObject.get("dietLabels").getAsJsonArray().asList();
        List<DietLabel> dietLabels = getDietLabels(dietLabelsJ);
        List<JsonElement> healthLabelsJ = jsonObject.get("healthLabels").getAsJsonArray().asList();
        List<HealthLabel> healthLabels = getHealthLabels(healthLabelsJ);
        List<JsonElement> ingredientLinesJ = jsonObject.get("ingredientLines").getAsJsonArray().asList();
        List<String> ingredientLines = getIngredientLines(ingredientLinesJ);
        return new Recipe(label, dietLabels, healthLabels, ingredientLines,
                totalWeight, cuisineType, mealTypes, dishTypes);
    }
}