package com.example.pantryiq;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        layoutRecipeSuggestions = findViewById(R.id.layoutRecipeSuggestions);
        tvRecipeCount = findViewById(R.id.tvRecipeCount);
        tvEmptyRecipes = findViewById(R.id.tvEmptyRecipes);
        btnBackToPantry = findViewById(R.id.btnBackToPantry);

        databaseHelper = new PantryDatabaseHelper(this);

        btnBackToPantry.setOnClickListener(view -> finish());

        loadRecipeSuggestions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && layoutRecipeSuggestions != null) {
            loadRecipeSuggestions();
        }
    }

    /**
     * Loads recipes from SQLite and displays only recipes
     * for which every required ingredient is available.
     */
    private void loadRecipeSuggestions() {
        List<Ingredient> pantryIngredients = readPantryIngredients();

        // Recipes are now retrieved from the SQLite database.
        List<Recipe> savedRecipes =
                databaseHelper.getAllRecipesFromDatabase();

        List<Recipe> matchingRecipes = RecipeMatcher.findMatchingRecipes(
                savedRecipes,
                pantryIngredients
        );

        layoutRecipeSuggestions.removeAllViews();

        tvRecipeCount.setText(
                getString(R.string.recipe_count, matchingRecipes.size())
        );

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
     * Reads pantry ingredients from SQLite and converts each row
     * into an Ingredient object.
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
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                double quantity = cursor.getDouble(quantityColumn);
                String unit = cursor.getString(unitColumn);
                String expiryDate = cursor.getString(expiryColumn);

                ingredients.add(
                        new Ingredient(
                                id,
                                name,
                                quantity,
                                unit,
                                expiryDate
                        )
                );
            }
        } finally {
            cursor.close();
        }

        return ingredients;
    }

    /**
     * Creates a recipe card and opens the corresponding detail screen.
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

        TextView nameView = new TextView(this);
        nameView.setText(recipe.getName());
        nameView.setTextSize(19);
        nameView.setTextColor(getColor(R.color.electric_plum));
        nameView.setTypeface(null, android.graphics.Typeface.BOLD);

        card.addView(nameView);

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
     * Formats the required ingredients for a compact recipe preview.
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
     * Avoids displaying unnecessary decimal zeros in quantities.
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


