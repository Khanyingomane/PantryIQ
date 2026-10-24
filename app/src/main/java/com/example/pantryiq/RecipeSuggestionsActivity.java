
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

/**
 * Displays recipes that can be prepared using the current pantry.
 */
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
     * Reads the pantry, finds matching recipes and displays the results.
     */
    private void loadRecipeSuggestions() {
        List<Ingredient> pantryIngredients = readPantryIngredients();

        List<Recipe> matchingRecipes = RecipeMatcher.findMatchingRecipes(
                RecipeRepository.getAllRecipes(),
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
     * Converts saved database rows into Ingredient objects.
     */
    private List<Ingredient> readPantryIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllIngredients();

        if (cursor == null) {
            return ingredients;
        }

        try {
            int idIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_NAME);
            int quantityIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_QUANTITY);
            int unitIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_UNIT);
            int expiryIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_EXPIRY_DATE);

            while (cursor.moveToNext()) {
                ingredients.add(new Ingredient(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getDouble(quantityIndex),
                        cursor.getString(unitIndex),
                        cursor.getString(expiryIndex)
                ));
            }
        } finally {
            cursor.close();
        }

        return ingredients;
    }

    /**
     * Creates a simple recipe card programmatically.
     */
    private void addRecipeCard(Recipe recipe) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_recipe_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView name = new TextView(this);
        name.setText(recipe.getName());
        name.setTextSize(18);
        name.setTextColor(getColor(R.color.electric_plum));
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(name);

        TextView description = new TextView(this);
        description.setText(recipe.getDescription());
        description.setTextSize(14);
        description.setTextColor(getColor(R.color.electric_plum));
        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        descriptionParams.setMargins(0, dp(6), 0, dp(8));
        description.setLayoutParams(descriptionParams);
        card.addView(description);

        TextView details = new TextView(this);
        details.setText(
                recipe.getTotalMinutes() + " min  •  "
                        + recipe.getServings() + " servings"
        );
        details.setTextSize(13);
        details.setTextColor(getColor(R.color.electric_plum));
        card.addView(details);

        TextView ingredients = new TextView(this);
        ingredients.setText(buildIngredientSummary(recipe));
        ingredients.setTextSize(13);
        ingredients.setTextColor(getColor(R.color.electric_plum));
        LinearLayout.LayoutParams ingredientsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        ingredientsParams.setMargins(0, dp(8), 0, dp(10));
        ingredients.setLayoutParams(ingredientsParams);
        card.addView(ingredients);

        Button viewRecipe = new Button(this);
        viewRecipe.setText("View Recipe");
        viewRecipe.setOnClickListener(view -> {
            Intent intent = new Intent(
                    RecipeSuggestionsActivity.this,
                    RecipeDetailActivity.class
            );
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
        });
        card.addView(viewRecipe);

        layoutRecipeSuggestions.addView(card);
    }

    private String buildIngredientSummary(Recipe recipe) {
        StringBuilder summary = new StringBuilder("Ingredients:");

        for (Recipe.RecipeIngredient ingredient : recipe.getIngredients()) {
            summary.append("\n• ")
                    .append(ingredient.getName())
                    .append(" — ")
                    .append(ingredient.getQuantity())
                    .append(" ")
                    .append(ingredient.getUnit());
        }

        return summary.toString();
    }

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
