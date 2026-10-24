package com.example.pantryiq;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;
    private ArrayList<Ingredient> ingredients;
    private IngredientAdapter ingredientAdapter;

    private TextView tvIngredientCount;
    private LinearLayout layoutEmptyPantry;
    private ExpandedGridView listPantryIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Connect the layout views.
        tvIngredientCount = findViewById(R.id.tvIngredientCount);
        layoutEmptyPantry = findViewById(R.id.layoutEmptyPantry);
        listPantryIngredients = findViewById(R.id.listPantryIngredients);

        Button btnAddIngredient = findViewById(R.id.btnAddIngredient);
        Button btnDiscoverRecipes = findViewById(R.id.btnDiscoverRecipes);
        Button btnOpenSettings = findViewById(R.id.btnOpenSettings);

        // Prepare the database and ingredient collection.
        databaseHelper = new PantryDatabaseHelper(this);
        ingredients = new ArrayList<>();

        // Keep the existing edit and delete actions on each ingredient.
        ingredientAdapter = new IngredientAdapter(
                this,
                ingredients,
                this::editIngredient,
                this::confirmDeleteIngredient
        );

        listPantryIngredients.setAdapter(ingredientAdapter);

        // Open the form to add an ingredient.
        btnAddIngredient.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );
            startActivity(intent);
        });

        // Open recipe suggestions.
        btnDiscoverRecipes.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    RecipeSuggestionsActivity.class
            );
            startActivity(intent);
        });

        // Open app settings.
        btnOpenSettings.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    SettingsActivity.class
            );
            startActivity(intent);
        });

        // Keep content clear of the system status and navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, windowInsets) -> {
                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return windowInsets;
                }
        );

        loadIngredients();
    }

    /**
     * Opens the ingredient form for the selected ingredient.
     */
    private void editIngredient(Ingredient ingredient) {
        Intent intent = new Intent(
                MainActivity.this,
                AddIngredientActivity.class
        );

        intent.putExtra("ingredient_id", ingredient.getId());
        startActivity(intent);
    }

    /**
     * Asks for confirmation before deleting an ingredient.
     */
    private void confirmDeleteIngredient(Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage(
                        "Are you sure you want to delete "
                                + ingredient.getName() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteIngredient(ingredient)
                )
                .show();
    }

    /**
     * Deletes an ingredient and refreshes the pantry display.
     */
    private void deleteIngredient(Ingredient ingredient) {
        try {
            int deleted = databaseHelper.deleteIngredient(
                    ingredient.getId()
            );

            if (deleted > 0) {
                Toast.makeText(
                        this,
                        "Ingredient deleted",
                        Toast.LENGTH_SHORT
                ).show();

                loadIngredients();
            } else {
                Toast.makeText(
                        this,
                        "Could not delete ingredient",
                        Toast.LENGTH_SHORT
                ).show();
            }
        } catch (Exception exception) {
            Toast.makeText(
                    this,
                    "An error occurred while deleting the ingredient",
                    Toast.LENGTH_SHORT
            ).show();

            exception.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh data after returning from another screen.
        if (databaseHelper != null && ingredients != null) {
            loadIngredients();
        }
    }

    /**
     * Reads saved ingredients from SQLite and refreshes the grid,
     * item count, and empty-pantry message.
     */
    private void loadIngredients() {
        ingredients.clear();

        try (Cursor cursor = databaseHelper.getAllIngredients()) {

            int idIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_ID
            );
            int nameIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_NAME
            );
            int quantityIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_QUANTITY
            );
            int unitIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_UNIT
            );
            int expiryIndex = cursor.getColumnIndexOrThrow(
                    PantryDatabaseHelper.COLUMN_EXPIRY_DATE
            );

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idIndex);
                String name = cursor.getString(nameIndex);
                double quantity = cursor.getDouble(quantityIndex);
                String unit = cursor.getString(unitIndex);

                String expiryDate = cursor.isNull(expiryIndex)
                        ? null
                        : cursor.getString(expiryIndex);

                ingredients.add(new Ingredient(
                        id,
                        name,
                        quantity,
                        unit,
                        expiryDate
                ));
            }

        } catch (Exception exception) {
            exception.printStackTrace();

            Toast.makeText(
                    this,
                    "Could not load pantry ingredients",
                    Toast.LENGTH_SHORT
            ).show();
        }

        // Refresh the existing adapter and its expiry information.
        ingredientAdapter.notifyDataSetChanged();

        // Update the pantry item count.
        int count = ingredients.size();

        String countText = String.format(
                Locale.getDefault(),
                "%d %s",
                count,
                count == 1 ? "ITEM" : "ITEMS"
        );

        tvIngredientCount.setText(countText);

        // Show the empty state only when the pantry has no ingredients.
        boolean isEmpty = ingredients.isEmpty();

        layoutEmptyPantry.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        listPantryIngredients.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE
        );
    }

    @Override
    protected void onDestroy() {
        // Release the database helper when this activity is destroyed.
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}

