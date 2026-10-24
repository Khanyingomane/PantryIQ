
package com.example.pantryiq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays recipe information retrieved from the SQLite database.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;

    private LinearLayout layoutRecipeDetail;
    private TextView tvRecipeDetailName;
    private TextView tvRecipeDetailDescription;
    private TextView tvRecipeDetailTime;
    private TextView tvRecipeDetailServings;
    private TextView tvRecipeDetailIngredients;
    private TextView tvRecipeDetailInstructions;
    private Button btnBackToSuggestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        layoutRecipeDetail = findViewById(R.id.layoutRecipeDetail);
        tvRecipeDetailName = findViewById(R.id.tvRecipeDetailName);
        tvRecipeDetailDescription =
                findViewById(R.id.tvRecipeDetailDescription);
        tvRecipeDetailTime = findViewById(R.id.tvRecipeDetailTime);
        tvRecipeDetailServings = findViewById(R.id.tvRecipeDetailServings);
        tvRecipeDetailIngredients =
                findViewById(R.id.tvRecipeDetailIngredients);
        tvRecipeDetailInstructions =
                findViewById(R.id.tvRecipeDetailInstructions);
        btnBackToSuggestions =
                findViewById(R.id.btnBackToSuggestions);

        databaseHelper = new PantryDatabaseHelper(this);

        btnBackToSuggestions.setOnClickListener(view -> finish());

        displayRecipe();
    }

    /**
     * Finds the selected recipe in SQLite using the supplied recipe ID.
     */
    private void displayRecipe() {
        String recipeId = getIntent().getStringExtra("recipe_id");

        if (recipeId == null || recipeId.trim().isEmpty()) {
            showRecipeUnavailable();
            return;
        }

        Recipe selectedRecipe = null;

        for (Recipe recipe : databaseHelper.getAllRecipesFromDatabase()) {
            if (recipe.getId().equals(recipeId)) {
                selectedRecipe = recipe;
                break;
            }
        }

        if (selectedRecipe == null) {
            showRecipeUnavailable();
            return;
        }

        tvRecipeDetailName.setText(selectedRecipe.getName());
        tvRecipeDetailDescription.setText(
                selectedRecipe.getDescription()
        );

        tvRecipeDetailTime.setText(
                "Preparation: "
                        + selectedRecipe.getPreparationMinutes()
                        + " minutes\nCooking: "
                        + selectedRecipe.getCookingMinutes()
                        + " minutes\nTotal time: "
                        + selectedRecipe.getTotalMinutes()
                        + " minutes"
        );

        tvRecipeDetailServings.setText(
                "Servings: " + selectedRecipe.getServings()
        );

        tvRecipeDetailIngredients.setText(
                buildIngredientList(selectedRecipe)
        );

        tvRecipeDetailInstructions.setText(
                buildInstructions(selectedRecipe)
        );
    }

    /**
     * Formats the recipe's required ingredients and quantities.
     */
    private String buildIngredientList(Recipe recipe) {
        StringBuilder result = new StringBuilder();

        for (Recipe.RecipeIngredient ingredient
                : recipe.getIngredients()) {

            result.append("\u2022 ")
                    .append(ingredient.getName())
                    .append(" — ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append("\n");
        }

        return result.toString().trim();
    }

    /**
     * Numbers the recipe's preparation steps in order.
     */
    private String buildInstructions(Recipe recipe) {
        StringBuilder result = new StringBuilder();
        int stepNumber = 1;

        for (String instruction : recipe.getInstructions()) {
            result.append(stepNumber++)
                    .append(". ")
                    .append(instruction)
                    .append("\n\n");
        }

        return result.toString().trim();
    }

    /**
     * Displays whole quantities without unnecessary decimal places.
     */
    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.format(
                    java.util.Locale.getDefault(),
                    "%.0f",
                    quantity
            );
        }

        return String.format(
                java.util.Locale.getDefault(),
                "%.2f",
                quantity
        ).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    /**
     * Informs the user when a requested recipe is unavailable.
     */
    private void showRecipeUnavailable() {
        Toast.makeText(
                this,
                "This recipe could not be found in the database.",
                Toast.LENGTH_LONG
        ).show();

        finish();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
