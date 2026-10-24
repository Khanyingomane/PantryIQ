package com.example.pantryiq;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Displays the ingredients and cooking instructions for one recipe.
 */
public class RecipeDetailActivity extends AppCompatActivity {

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
        btnBackToSuggestions = findViewById(R.id.btnBackToSuggestions);

        btnBackToSuggestions.setOnClickListener(view -> finish());

        displayRecipe();
    }

    /**
     * Finds the selected recipe using the ID passed from the suggestions screen.
     */
    private void displayRecipe() {
        String recipeId = getIntent().getStringExtra("recipe_id");

        if (recipeId == null || recipeId.trim().isEmpty()) {
            showRecipeUnavailable();
            return;
        }

        Recipe selectedRecipe = null;

        for (Recipe recipe : RecipeRepository.getAllRecipes()) {
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
        tvRecipeDetailDescription.setText(selectedRecipe.getDescription());
        tvRecipeDetailTime.setText(
                "Total time: " + selectedRecipe.getTotalMinutes() + " minutes"
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

    private String buildIngredientList(Recipe recipe) {
        StringBuilder result = new StringBuilder();

        for (Recipe.RecipeIngredient ingredient : recipe.getIngredients()) {
            result.append("\u2022 ")
                    .append(ingredient.getName())
                    .append(" — ")
                    .append(ingredient.getQuantity())
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append("\n");
        }

        return result.toString().trim();
    }

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

    private void showRecipeUnavailable() {
        Toast.makeText(
                this,
                "This recipe could not be found.",
                Toast.LENGTH_LONG
        ).show();

        finish();
    }
}
