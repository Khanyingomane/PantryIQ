
package com.example.pantryiq;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeSuggestionsActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;

    private LinearLayout layoutRecipeSuggestions;
    private TextView tvRecipeCount;
    private TextView tvEmptyRecipes;
    private Button btnBackToPantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_suggestions);

        // Connect the screen components.
        layoutRecipeSuggestions =
                findViewById(R.id.layoutRecipeSuggestions);
        tvRecipeCount = findViewById(R.id.tvRecipeCount);
        tvEmptyRecipes = findViewById(R.id.tvEmptyRecipes);
        btnBackToPantry = findViewById(R.id.btnBackToPantry);

        // Initialise the database.
        databaseHelper = new PantryDatabaseHelper(this);

        // Return to the previous screen.
        btnBackToPantry.setOnClickListener(view -> finish());

        loadRecipeSuggestions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh suggestions whenever the screen becomes active.
        if (databaseHelper != null && layoutRecipeSuggestions != null) {
            loadRecipeSuggestions();
        }
    }

    /**
     * Loads recipes and displays only those that can be made
     * with all required pantry ingredients and quantities.
     */
    private void loadRecipeSuggestions() {
        List<Ingredient> pantryIngredients = readPantryIngredients();

        // Retrieve the recipes stored in SQLite.
        List<Recipe> savedRecipes =
                databaseHelper.getAllRecipesFromDatabase();

        // Apply strict ingredient and quantity matching.
        List<Recipe> matchingRecipes =
                RecipeMatcher.findMatchingRecipes(
                        savedRecipes,
                        pantryIngredients
                );

        // TEMPORARY diagnostic to identify the source of the problem.
        Toast.makeText(
                this,
                "Saved recipes: " + savedRecipes.size()
                        + " | Matches: " + matchingRecipes.size(),
                Toast.LENGTH_LONG
        ).show();

        // Clear old cards before refreshing the list.
        layoutRecipeSuggestions.removeAllViews();

        // Update the number of available recipes.
        tvRecipeCount.setText(
                getString(R.string.recipe_count, matchingRecipes.size())
        );

        // Display the empty state when no recipe qualifies.
        if (matchingRecipes.isEmpty()) {
            tvEmptyRecipes.setVisibility(View.VISIBLE);
            layoutRecipeSuggestions.setVisibility(View.GONE);
            return;
        }

        tvEmptyRecipes.setVisibility(View.GONE);
        layoutRecipeSuggestions.setVisibility(View.VISIBLE);

        for (Recipe recipe : matchingRecipes) {
            addRecipeCard(recipe);
        }
    }

    /**
     * Reads pantry ingredients from SQLite.
     */
    private List<Ingredient> readPantryIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllIngredients();

        if (cursor == null) {
            return ingredients;
        }

        try {
            int idColumn = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_ID
            );
            int nameColumn = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_NAME
            );
            int quantityColumn = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_QUANTITY
            );
            int unitColumn = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_UNIT
            );
            int expiryColumn = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_EXPIRY_DATE
            );

            while (cursor.moveToNext()) {
                ingredients.add(
                        new Ingredient(
                                cursor.getLong(idColumn),
                                cursor.getString(nameColumn),
                                cursor.getDouble(quantityColumn),
                                cursor.getString(unitColumn),
                                cursor.getString(expiryColumn)
                        )
                );
            }
        } finally {
            cursor.close();
        }

        return ingredients;
    }

    /**
     * Creates a recipe card with its details and navigation button.
     */
    private void addRecipeCard(Recipe recipe) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_recipe_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(cardParams);

        // Recipe name.
        TextView nameView = new TextView(this);
        nameView.setText(recipe.getName());
        nameView.setTextSize(19);
        nameView.setTextColor(getColor(R.color.electric_plum));
        nameView.setTypeface(
                null,
                android.graphics.Typeface.BOLD
        );
        card.addView(nameView);

        // Short description.
        TextView descriptionView = new TextView(this);
        descriptionView.setText(recipe.getDescription());
        descriptionView.setTextSize(14);
        descriptionView.setTextColor(
                getColor(android.R.color.darker_gray)
        );

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.setMargins(0, dp(5), 0, dp(8));
        descriptionView.setLayoutParams(descriptionParams);
        card.addView(descriptionView);

        // Preparation time and serving information.
        TextView timeView = new TextView(this);
        timeView.setText(
                String.format(
                        Locale.getDefault(),
                        "%d min total  |  Serves %d",
                        recipe.getTotalMinutes(),
                        recipe.getServings()
                )
        );
        timeView.setTextSize(13);
        timeView.setTextColor(
                getColor(android.R.color.darker_gray)
        );
        card.addView(timeView);

        // Required ingredients and quantities.
        TextView ingredientsView = new TextView(this);
        ingredientsView.setText(
                buildIngredientSummary(recipe.getIngredients())
        );
        ingredientsView.setTextSize(14);
        ingredientsView.setTextColor(
                getColor(R.color.electric_plum)
        );

        LinearLayout.LayoutParams ingredientsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        ingredientsParams.setMargins(0, dp(10), 0, dp(12));
        ingredientsView.setLayoutParams(ingredientsParams);
        card.addView(ingredientsView);

        // Open the full recipe instructions.
        Button viewRecipeButton = new Button(this);
        viewRecipeButton.setText("View Recipe");
        viewRecipeButton.setAllCaps(false);
        viewRecipeButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        getColor(R.color.vivid_pink)
                )
        );
        viewRecipeButton.setTextColor(
                getColor(android.R.color.white)
        );

        viewRecipeButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    RecipeSuggestionsActivity.this,
                    RecipeDetailActivity.class
            );

            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        });

        card.addView(viewRecipeButton);
        layoutRecipeSuggestions.addView(card);
    }

    /**
     * Formats recipe ingredient names, quantities and units.
     */
    private String buildIngredientSummary(
            List<Recipe.RecipeIngredient> ingredients
    ) {
        StringBuilder summary = new StringBuilder("Ingredients: ");

        for (int i = 0; i < ingredients.size(); i++) {
            Recipe.RecipeIngredient ingredient = ingredients.get(i);

            if (i > 0) {
                summary.append(", ");
            }

            summary.append(ingredient.getName())
                    .append(" (")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(")");
        }

        return summary.toString();
    }

    /**
     * Avoids unnecessary decimal zeros in displayed quantities.
     */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.format(
                    Locale.getDefault(),
                    "%.0f",
                    quantity
            );
        }

        return String.format(
                Locale.getDefault(),
                "%.2f",
                quantity
        ).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /**
     * Converts density-independent pixels to screen pixels.
     */
    private int dp(int value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + 0.5f
        );
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
