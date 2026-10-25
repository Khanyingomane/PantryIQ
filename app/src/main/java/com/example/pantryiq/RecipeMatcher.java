
package com.example.pantryiq;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Matches recipes against pantry ingredients.
 *
 * A recipe is suggested only when every required ingredient
 * is available in sufficient quantity and a compatible unit.
 */
public final class RecipeMatcher {

    private static final String TAG = "PantryRecipeMatcher";
    private static final double EPSILON = 0.000001;

    private RecipeMatcher() {
        // Utility class; no instances needed.
    }

    public static List<Recipe> findMatchingRecipes(
            List<Recipe> recipes,
            List<Ingredient> pantryIngredients) {

        List<Recipe> matches = new ArrayList<>();

        if (recipes == null || pantryIngredients == null) {
            Log.e(TAG, "Recipes or pantry ingredients list is null.");
            return matches;
        }

        Map<String, List<Ingredient>> pantry = new HashMap<>();

        for (Ingredient ingredient : pantryIngredients) {
            if (ingredient == null || ingredient.getName() == null) {
                continue;
            }

            String key = normalizeName(ingredient.getName());

            if (key.isEmpty()) {
                continue;
            }

            pantry.computeIfAbsent(key, unused -> new ArrayList<>())
                    .add(ingredient);

            Log.d(TAG, "PANTRY: " + key
                    + ", quantity=" + ingredient.getQuantity()
                    + ", unit=" + ingredient.getUnit());
        }

        for (Recipe recipe : recipes) {
            if (recipe == null) {
                continue;
            }

            boolean matchesRecipe = hasAllIngredients(recipe, pantry);

            Log.d(TAG, "RECIPE: " + recipe.getName()
                    + " | MATCH=" + matchesRecipe);

            if (matchesRecipe) {
                matches.add(recipe);
            }
        }

        Log.d(TAG, "TOTAL MATCHES: " + matches.size());

        return matches;
    }

    /**
     * Checks every required ingredient and logs the first failure.
     */
    private static boolean hasAllIngredients(
            Recipe recipe,
            Map<String, List<Ingredient>> pantry) {

        for (Recipe.RecipeIngredient required : recipe.getIngredients()) {
            if (required == null || required.getName() == null) {
                Log.d(TAG, recipe.getName()
                        + " rejected: invalid recipe ingredient.");
                return false;
            }

            String key = normalizeName(required.getName());
            List<Ingredient> available = findIngredientEntries(pantry, key);

            if (available == null) {
                Log.d(TAG, recipe.getName()
                        + " rejected: missing ingredient " + key);
                return false;
            }

            if (!hasSufficientQuantity(required, available)) {
                Log.d(TAG, recipe.getName()
                        + " rejected: insufficient quantity or incompatible unit for "
                        + key + "; required " + required.getQuantity()
                        + " " + required.getUnit());
                return false;
            }
        }

        return true;
    }

    private static List<Ingredient> findIngredientEntries(
            Map<String, List<Ingredient>> pantry,
            String requiredName) {

        String singularRequired = singularize(requiredName);

        for (Map.Entry<String, List<Ingredient>> entry
                : pantry.entrySet()) {

            if (singularize(entry.getKey()).equals(singularRequired)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Handles common English ingredient plurals.
     */
    private static String singularize(String name) {
        if (name == null || name.isEmpty()) {
            return "";
        }

        switch (name) {
            case "tomatoes":
                return "tomato";
            case "potatoes":
                return "potato";
            case "leaves":
                return "leaf";
            case "loaves":
                return "loaf";
            default:
                break;
        }

        if (name.endsWith("ies") && name.length() > 3) {
            return name.substring(0, name.length() - 3) + "y";
        }

        if (name.endsWith("oes") && name.length() > 3) {
            return name.substring(0, name.length() - 2);
        }

        if (name.endsWith("s")
                && !name.endsWith("ss")
                && !name.endsWith("us")
                && name.length() > 2) {
            return name.substring(0, name.length() - 1);
        }

        return name;
    }

    /**
     * Sums pantry quantities that can safely be converted
     * into the recipe's required unit.
     */
    private static boolean hasSufficientQuantity(
            Recipe.RecipeIngredient required,
            List<Ingredient> availableIngredients) {

        if (!Double.isFinite(required.getQuantity())
                || required.getQuantity() < 0
                || required.getUnit() == null) {
            return false;
        }

        double availableQuantity = 0;

        for (Ingredient available : availableIngredients) {
            if (available.getUnit() == null
                    || !Double.isFinite(available.getQuantity())
                    || available.getQuantity() < 0) {
                continue;
            }

            Double converted = convertQuantity(
                    available.getQuantity(),
                    available.getUnit(),
                    required.getUnit()
            );

            if (converted != null) {
                availableQuantity += converted;
            }
        }

        return availableQuantity + EPSILON >= required.getQuantity();
    }

    /**
     * Converts only compatible units.
     * Count units such as slices, pieces and cans remain distinct.
     */
    private static Double convertQuantity(
            double quantity,
            String sourceUnit,
            String targetUnit) {

        if (sourceUnit == null || targetUnit == null
                || !Double.isFinite(quantity) || quantity < 0) {
            return null;
        }

        String source = normalizeUnit(sourceUnit);
        String target = normalizeUnit(targetUnit);

        if (source.equals(target)) {
            return quantity;
        }

        if (isMassUnit(source) && isMassUnit(target)) {
            return fromGrams(toGrams(quantity, source), target);
        }

        if (isVolumeUnit(source) && isVolumeUnit(target)) {
            return fromMillilitres(
                    toMillilitres(quantity, source), target);
        }

        if (isKitchenVolumeUnit(source)
                && isKitchenVolumeUnit(target)) {
            return fromMillilitres(
                    toMillilitres(quantity, source), target);
        }

        return null;
    }

    private static boolean isMassUnit(String unit) {
        return unit.equals("g") || unit.equals("kg");
    }

    private static boolean isVolumeUnit(String unit) {
        return unit.equals("ml") || unit.equals("l");
    }

    private static boolean isKitchenVolumeUnit(String unit) {
        return unit.equals("tsp")
                || unit.equals("tbsp")
                || unit.equals("cup");
    }

    private static double toGrams(double quantity, String unit) {
        return unit.equals("kg") ? quantity * 1000 : quantity;
    }

    private static double fromGrams(double grams, String unit) {
        return unit.equals("kg") ? grams / 1000 : grams;
    }

    private static double toMillilitres(double quantity, String unit) {
        switch (unit) {
            case "l":
                return quantity * 1000;
            case "cup":
                return quantity * 250;
            case "tbsp":
                return quantity * 15;
            case "tsp":
                return quantity * 5;
            default:
                return quantity;
        }
    }

    private static double fromMillilitres(
            double millilitres,
            String unit) {

        switch (unit) {
            case "l":
                return millilitres / 1000;
            case "cup":
                return millilitres / 250;
            case "tbsp":
                return millilitres / 15;
            case "tsp":
                return millilitres / 5;
            default:
                return millilitres;
        }
    }

    private static String normalizeUnit(String unit) {
        String normalized = unit.trim().toLowerCase(Locale.ROOT);

        switch (normalized) {
            case "gram":
            case "grams":
                return "g";
            case "kilogram":
            case "kilograms":
                return "kg";
            case "millilitre":
            case "millilitres":
            case "milliliter":
            case "milliliters":
                return "ml";
            case "litre":
            case "litres":
            case "liter":
            case "liters":
                return "l";
            case "piece":
            case "pieces":
            case "pc":
            case "pcs":
            case "each":
                return "piece";
            case "slice":
            case "slices":
                return "slice";
            case "item":
            case "items":
                return "item";
            case "egg":
            case "eggs":
                return "egg";
            case "banana":
            case "bananas":
                return "banana";
            case "tomato":
            case "tomatoes":
                return "tomato";
            case "potato":
            case "potatoes":
                return "potato";
            case "carrot":
            case "carrots":
                return "carrot";
            case "onion":
            case "onions":
                return "onion";
            case "lemon":
            case "lemons":
                return "lemon";
            case "apple":
            case "apples":
                return "apple";
            case "cup":
            case "cups":
                return "cup";
            case "tablespoon":
            case "tablespoons":
            case "tbsp":
            case "tbs":
                return "tbsp";
            case "teaspoon":
            case "teaspoons":
            case "tsp":
                return "tsp";
            default:
                return normalized;
        }
    }

    private static String normalizeName(String name) {
        return name.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }
}
