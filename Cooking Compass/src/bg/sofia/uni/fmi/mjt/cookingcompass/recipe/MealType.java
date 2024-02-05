package bg.sofia.uni.fmi.mjt.cookingcompass.recipe;

public enum MealType {
    BREAKFAST("breakfast"),
    BRUNCH("brunch"),
    LUNCH("lunch"),
    DINNER("dinner"),
    LUNCH_DINNER("lunch-dinner"),
    SNACK("snack"),
    TEATIME("teatime");

    final String value;

    MealType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
