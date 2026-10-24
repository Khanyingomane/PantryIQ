
package com.example.pantryiq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provides PantryIQ's 20 built-in recipes.
 * Ingredient quantities are checked by the matching engine later.
 */
public final class RecipeRepository {

    private RecipeRepository() {
        // Prevent unnecessary repository objects.
    }

    public static List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();

        recipes.add(recipe("R001", "Tomato Scrambled Eggs",
                "A quick, protein-rich breakfast.",
                5, 10, 2,
                ingredients("eggs", 4, "pieces",
                        "tomatoes", 2, "pieces",
                        "onion", 0.5, "pieces",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.25, "tsp"),
                "Chop the tomatoes and onion.",
                "Heat oil and soften the onion and tomatoes.",
                "Beat the eggs with salt, add them to the pan and stir until cooked."));

        recipes.add(recipe("R002", "Egg on Toast",
                "Simple toast topped with cooked eggs.",
                5, 5, 2,
                ingredients("bread", 4, "slices",
                        "eggs", 2, "pieces",
                        "cooking oil", 1, "tsp",
                        "salt", 0.25, "tsp"),
                "Toast the bread.",
                "Heat oil and fry the eggs until cooked.",
                "Season the eggs and serve on the toast."));

        recipes.add(recipe("R003", "Potato and Onion Hash",
                "Golden potatoes with softened onion.",
                10, 20, 2,
                ingredients("potatoes", 3, "pieces",
                        "onion", 1, "pieces",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.5, "tsp"),
                "Dice the potatoes and chop the onion.",
                "Boil the potatoes until just tender, then drain.",
                "Heat oil, fry the onion, add potatoes and cook until golden.",
                "Season and serve."));

        recipes.add(recipe("R004", "Peanut Butter Sandwich",
                "A filling, no-cook sandwich.",
                5, 0, 2,
                ingredients("bread", 4, "slices",
                        "peanut butter", 4, "tbsp"),
                "Spread peanut butter evenly over the bread.",
                "Close the sandwiches, cut and serve."));

        recipes.add(recipe("R005", "Banana Oats",
                "Warm oats with banana.",
                3, 7, 1,
                ingredients("oats", 0.5, "cups",
                        "milk", 1, "cups",
                        "banana", 1, "pieces"),
                "Add oats and milk to a saucepan.",
                "Cook over low heat, stirring until creamy.",
                "Slice the banana and add it before serving."));

        recipes.add(recipe("R006", "Tomato Rice",
                "Rice cooked with tomato and onion.",
                10, 25, 2,
                ingredients("rice", 1, "cups",
                        "tomatoes", 2, "pieces",
                        "onion", 1, "pieces",
                        "cooking oil", 1, "tbsp",
                        "water", 2, "cups",
                        "salt", 0.5, "tsp"),
                "Rinse the rice and chop the vegetables.",
                "Fry onion and tomatoes in oil.",
                "Add rice, water and salt. Cover and simmer until tender."));

        recipes.add(recipe("R007", "Garlic-Free Creamy Pasta",
                "A simple creamy pasta made with milk.",
                5, 15, 2,
                ingredients("pasta", 200, "g",
                        "milk", 1, "cups",
                        "butter", 1, "tbsp",
                        "flour", 1, "tbsp",
                        "salt", 0.25, "tsp"),
                "Cook pasta according to its packet instructions and drain.",
                "Melt butter in a saucepan and stir in flour.",
                "Gradually add milk while stirring until the sauce thickens.",
                "Season, mix in the pasta and serve."));

        recipes.add(recipe("R008", "Cabbage Stir-Fry",
                "A budget-friendly vegetable side.",
                10, 10, 2,
                ingredients("cabbage", 300, "g",
                        "carrots", 1, "pieces",
                        "onion", 1, "pieces",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.25, "tsp"),
                "Slice the cabbage, carrot and onion.",
                "Heat oil in a pan and fry the onion.",
                "Add carrot and cabbage and stir-fry until tender.",
                "Season and serve."));

        recipes.add(recipe("R009", "Bean Tomato Stew",
                "A hearty tomato and bean dish.",
                10, 20, 2,
                ingredients("canned beans", 1, "cans",
                        "tomatoes", 2, "pieces",
                        "onion", 1, "pieces",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.5, "tsp"),
                "Chop the onion and tomatoes.",
                "Fry the onion in oil until soft.",
                "Add tomatoes and cook until they break down.",
                "Stir in drained beans, season and simmer for 10 minutes."));

        recipes.add(recipe("R010", "Cheese Omelette",
                "A soft omelette with melted cheese.",
                5, 8, 1,
                ingredients("eggs", 2, "pieces",
                        "cheese", 30, "g",
                        "butter", 1, "tsp",
                        "salt", 0.25, "tsp"),
                "Beat the eggs with salt and grate the cheese.",
                "Melt butter in a non-stick pan and add the eggs.",
                "When nearly set, add cheese, fold and cook until done."));

        recipes.add(recipe("R011", "Carrot Potato Soup",
                "A comforting blended vegetable soup.",
                10, 25, 2,
                ingredients("potatoes", 2, "pieces",
                        "carrots", 2, "pieces",
                        "onion", 1, "pieces",
                        "water", 3, "cups",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.5, "tsp"),
                "Peel and chop the vegetables.",
                "Fry the onion in oil until soft.",
                "Add potatoes, carrots and water.",
                "Simmer until tender, mash or blend carefully, then season."));

        recipes.add(recipe("R012", "Fried Cabbage and Eggs",
                "A quick meal with vegetables and protein.",
                8, 12, 2,
                ingredients("cabbage", 250, "g",
                        "eggs", 3, "pieces",
                        "onion", 1, "pieces",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.25, "tsp"),
                "Slice the cabbage and chop the onion.",
                "Fry the onion and cabbage in oil until softened.",
                "Add beaten eggs and stir until fully cooked.",
                "Season and serve."));

        recipes.add(recipe("R013", "Sweetcorn Rice",
                "Rice with sweetcorn for an easy side dish.",
                5, 20, 2,
                ingredients("rice", 1, "cups",
                        "sweetcorn", 1, "cups",
                        "water", 2, "cups",
                        "cooking oil", 1, "tsp",
                        "salt", 0.5, "tsp"),
                "Rinse the rice.",
                "Add rice, water, oil and salt to a saucepan.",
                "Cover and simmer until the rice is almost tender.",
                "Stir in sweetcorn and cook until heated through."));

        recipes.add(recipe("R014", "Banana Peanut Oats",
                "Oats with banana and peanut butter.",
                3, 7, 1,
                ingredients("oats", 0.5, "cups",
                        "milk", 1, "cups",
                        "banana", 1, "pieces",
                        "peanut butter", 1, "tbsp"),
                "Combine oats and milk in a saucepan.",
                "Cook over low heat until creamy, stirring regularly.",
                "Slice the banana and stir it in with peanut butter."));

        recipes.add(recipe("R015", "Tomato Cheese Toast",
                "Toasted bread with tomato and melted cheese.",
                5, 7, 2,
                ingredients("bread", 4, "slices",
                        "tomatoes", 1, "pieces",
                        "cheese", 60, "g"),
                "Slice the tomato and grate the cheese.",
                "Place tomato and cheese on the bread.",
                "Toast in a suitable oven or sandwich press until the cheese melts."));

        recipes.add(recipe("R016", "Carrot Cabbage Salad",
                "A crunchy fresh vegetable salad.",
                10, 0, 2,
                ingredients("cabbage", 200, "g",
                        "carrots", 2, "pieces",
                        "lemon", 1, "pieces",
                        "salt", 0.25, "tsp"),
                "Finely shred the cabbage.",
                "Grate the carrots and combine with the cabbage.",
                "Squeeze lemon juice over the salad, season lightly and toss."));

        recipes.add(recipe("R017", "Potato Egg Salad",
                "Boiled potatoes and eggs in a simple salad.",
                10, 15, 2,
                ingredients("potatoes", 3, "pieces",
                        "eggs", 2, "pieces",
                        "mayonnaise", 2, "tbsp",
                        "salt", 0.25, "tsp"),
                "Boil the potatoes until tender and the eggs until hard-boiled.",
                "Allow them to cool, then peel and chop.",
                "Mix gently with mayonnaise and salt."));

        recipes.add(recipe("R018", "Lentil Tomato Curry",
                "A filling lentil dish served with rice.",
                10, 30, 2,
                ingredients("lentils", 1, "cups",
                        "rice", 1, "cups",
                        "tomatoes", 2, "pieces",
                        "onion", 1, "pieces",
                        "curry powder", 1, "tsp",
                        "water", 3, "cups",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.5, "tsp"),
                "Rinse the lentils and chop the vegetables.",
                "Fry the onion in oil and stir in curry powder.",
                "Add tomatoes, lentils and water. Simmer until the lentils are tender.",
                "Cook the rice separately and serve with the lentil curry."));

        recipes.add(recipe("R019", "Spinach Scrambled Eggs",
                "Eggs cooked with leafy spinach.",
                5, 8, 2,
                ingredients("eggs", 3, "pieces",
                        "spinach", 100, "g",
                        "onion", 0.5, "pieces",
                        "cooking oil", 1, "tbsp",
                        "salt", 0.25, "tsp"),
                "Wash and chop the spinach and onion.",
                "Fry the onion in oil until soft.",
                "Add spinach and cook until wilted.",
                "Add beaten eggs and stir until fully cooked. Season and serve."));

        recipes.add(recipe("R020", "Apple Oat Porridge",
                "Warm oats topped with chopped apple.",
                5, 8, 1,
                ingredients("oats", 0.5, "cups",
                        "milk", 1, "cups",
                        "apple", 1, "pieces",
                        "cinnamon", 0.25, "tsp"),
                "Add oats and milk to a saucepan.",
                "Cook over low heat until creamy.",
                "Chop the apple and serve it over the porridge with cinnamon."));

        return Collections.unmodifiableList(recipes);
    }

    private static Recipe recipe(
            String id,
            String name,
            String description,
            int preparationMinutes,
            int cookingMinutes,
            int servings,
            List<Recipe.RecipeIngredient> ingredients,
            String... instructions) {

        return new Recipe(
                id,
                name,
                description,
                ingredients,
                Arrays.asList(instructions),
                preparationMinutes,
                cookingMinutes,
                servings);
    }

    /**
     * Creates ingredient requirements from name, quantity and unit triples.
     */
    private static List<Recipe.RecipeIngredient> ingredients(Object... values) {
        if (values.length % 3 != 0) {
            throw new IllegalArgumentException(
                    "Ingredients must be supplied as name, quantity and unit.");
        }

        List<Recipe.RecipeIngredient> result = new ArrayList<>();

        for (int i = 0; i < values.length; i += 3) {
            String name = (String) values[i];
            double quantity = ((Number) values[i + 1]).doubleValue();
            String unit = (String) values[i + 2];

            result.add(new Recipe.RecipeIngredient(name, quantity, unit));
        }

        return result;
    }
}
