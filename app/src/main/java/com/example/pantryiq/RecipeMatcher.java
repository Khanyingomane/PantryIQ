package com.example.pantryiq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Matches recipes against pantry ingredients.
 *
 * A recipe matches only when every required ingredient is available
 * in sufficient quantity and in a compatible unit.
 */
public final class RecipeMatcher {

    private RecipeMatcher() {
        // Utility class; no instances needed.
    }

    /**
     * Returns only recipes for which every ingredient requirement is met.
     */
    public static List<Recipe> findMatchingRecipes(
            List<Recipe> recipes,
            List<Ingredient> pantryIngredients) {

        List<Recipe> matches = new ArrayList<>();

        if (recipes == null || pantryIngredients == null) {
            return matches;
        }

        Map<String, List<Ingredient>> pantry = new HashMap<>();

        for (Ingredient ingredient : pantryIngredients) {
            if (ingredient == null || ingredient.getName() == null) {
                continue;
            }

            String key = normalizeName(ingredient.getName());

            pantry.computeIfAbsent(key, unused -> new ArrayList<>())
                    .add(ingredient);
        }

        for (Recipe recipe : recipes) {
            if (recipe != null && hasAllIngredients(recipe, pantry)) {
                matches.add(recipe);
            }
        }

        return matches;
    }

    /**
     * Checks every required ingredient against the pantry.
     */
    private static boolean hasAllIngredients(
            Recipe recipe,
            Map<String, List<Ingredient>> pantry) {

        for (Recipe.RecipeIngredient required : recipe.getIngredients()) {
            String key = normalizeName(required.getName());

            List<Ingredient> availableIngredients = pantry.get(key);

            if (availableIngredients == null
                    || !hasSufficientQuantity(required, availableIngredients)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether matching pantry entries provide enough quantity.
     * Compatible units are converted before quantities are compared.
     */
    private static boolean hasSufficientQuantity(
            Recipe.RecipeIngredient required,
            List<Ingredient> availableIngredients) {

        double availableQuantity = 0;

        for (Ingredient available : availableIngredients) {
            Double converted = convertQuantity(
                    available.getQuantity(),
                    available.getUnit(),
                    required.getUnit());

            if (converted != null) {
                availableQuantity += converted;
            }
        }

        return availableQuantity + 0.000001 >= required.getQuantity();
    }

    /**
     * Converts between common compatible metric units.
     * Returns null when the units cannot safely be compared.
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

        // Mass conversions.
        if (isMassUnit(source) && isMassUnit(target)) {
            double grams = toGrams(quantity, source);
            return fromGrams(grams, target);
        }

        // Volume conversions.
        if (isVolumeUnit(source) && isVolumeUnit(target)) {
            double millilitres = toMillilitres(quantity, source);
            return fromMillilitres(millilitres, target);
        }

        // Count units are only compatible with other count units.
        if (isCountUnit(source) && isCountUnit(target)) {
            return quantity;
        }

        return null;
    }

    private static boolean isMassUnit(String unit) {
        return unit.equals("g") || unit.equals("kg");
    }

    private static boolean isVolumeUnit(String unit) {
        return unit.equals("ml") || unit.equals("l");
    }

    private static boolean isCountUnit(String unit) {
        return unit.equals("pieces") || unit.equals("piece")
                || unit.equals("pcs") || unit.equals("each");
    }

    private static double toGrams(double quantity, String unit) {
        return unit.equals("kg") ? quantity * 1000 : quantity;
    }

    private static double fromGrams(double grams, String unit) {
        return unit.equals("kg") ? grams / 1000 : grams;
    }

    private static double toMillilitres(double quantity, String unit) {
        return unit.equals("l") ? quantity * 1000 : quantity;
    }

    private static double fromMillilitres(double ml, String unit) {
        return unit.equals("l") ? ml / 1000 : ml;
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
            case "pc":
            case "pcs":
            case "each":
                return "pieces";
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


