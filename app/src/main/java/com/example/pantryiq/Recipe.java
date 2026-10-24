package com.example.pantryiq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a recipe and the ingredients required to prepare it.
 */
public class Recipe {

    private final String id;
    private final String name;
    private final String description;
    private final List<RecipeIngredient> ingredients;
    private final List<String> instructions;
    private final int preparationMinutes;
    private final int cookingMinutes;
    private final int servings;

    public Recipe(
            String id,
            String name,
            String description,
            List<RecipeIngredient> ingredients,
            List<String> instructions,
            int preparationMinutes,
            int cookingMinutes,
            int servings) {

        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe ID is required.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipe name is required.");
        }

        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException(
                    "A recipe must have at least one ingredient.");
        }

        if (instructions == null || instructions.isEmpty()) {
            throw new IllegalArgumentException(
                    "A recipe must have instructions.");
        }

        if (preparationMinutes < 0 || cookingMinutes < 0) {
            throw new IllegalArgumentException(
                    "Preparation and cooking times cannot be negative.");
        }

        if (servings <= 0) {
            throw new IllegalArgumentException(
                    "Servings must be greater than zero.");
        }

        this.id = id;
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();

        this.ingredients = Collections.unmodifiableList(
                new ArrayList<>(ingredients));

        this.instructions = Collections.unmodifiableList(
                new ArrayList<>(instructions));

        this.preparationMinutes = preparationMinutes;
        this.cookingMinutes = cookingMinutes;
        this.servings = servings;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public int getPreparationMinutes() {
        return preparationMinutes;
    }

    public int getCookingMinutes() {
        return cookingMinutes;
    }

    public int getTotalMinutes() {
        return preparationMinutes + cookingMinutes;
    }

    public int getServings() {
        return servings;
    }

    /**
     * Represents one ingredient and the quantity a recipe requires.
     */
    public static class RecipeIngredient {

        private final String name;
        private final double quantity;
        private final String unit;

        public RecipeIngredient(
                String name,
                double quantity,
                String unit) {

            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Ingredient name is required.");
            }

            if (!Double.isFinite(quantity) || quantity <= 0) {
                throw new IllegalArgumentException(
                        "Ingredient quantity must be positive.");
            }

            if (unit == null || unit.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Ingredient unit is required.");
            }

            this.name = name.trim();
            this.quantity = quantity;
            this.unit = unit.trim();
        }

        public String getName() {
            return name;
        }

        public double getQuantity() {
            return quantity;
        }

        public String getUnit() {
            return unit;
        }
    }
}


