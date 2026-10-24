package com.example.pantryiq;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;
    private ArrayList<Ingredient> ingredients;
    private IngredientAdapter ingredientAdapter;

    private TextView tvIngredientCount;
    private View layoutEmptyPantry;
    private ExpandedGridView listPantryIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display and load the dashboard layout.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Find the dashboard views.
        tvIngredientCount = findViewById(R.id.tvIngredientCount);
        layoutEmptyPantry = findViewById(R.id.layoutEmptyPantry);
        listPantryIngredients = findViewById(R.id.listPantryIngredients);

        Button btnOpenPantry = findViewById(R.id.btnOpenPantry);
        Button btnAddIngredient = findViewById(R.id.btnAddIngredient);
        Button btnDiscoverRecipes = findViewById(R.id.btnDiscoverRecipes);
        Button btnOpenSettings = findViewById(R.id.btnOpenSettings);

        // Initialise the database and ingredient list.
        databaseHelper = new PantryDatabaseHelper(this);
        ingredients = new ArrayList<>();

        // Connect the ingredient preview to its adapter.
        ingredientAdapter = new IngredientAdapter(
                this,
                ingredients,
                this::editIngredient,
                this::confirmDeleteIngredient
        );
        listPantryIngredients.setAdapter(ingredientAdapter);

        // Open the full pantry inventory screen.
        btnOpenPantry.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PantryActivity.class);
            startActivity(intent);
        });

        // Open the form for adding an ingredient.
        btnAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddIngredientActivity.class);
            startActivity(intent);
        });

        // Open recipe discovery.
        btnDiscoverRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RecipeSuggestionsActivity.class);
            startActivity(intent);
        });

        // Open app settings.
        btnOpenSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Apply system-bar insets so content is not hidden behind system UI.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, windowInsets) -> {
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
        });

        // Load the saved ingredients when the dashboard opens.
        loadIngredients();
    }

    /**
     * Opens the ingredient form in edit mode for the selected ingredient.
     */
    private void editIngredient(Ingredient ingredient) {
        Intent intent = new Intent(this, AddIngredientActivity.class);
        intent.putExtra("ingredient_id", ingredient.getId());
        startActivity(intent);
    }

    /**
     * Asks for confirmation before deleting an ingredient.
     */
    private void confirmDeleteIngredient(Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage("Are you sure you want to delete "
                        + ingredient.getName() + " from your pantry?")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteIngredient(ingredient.getId());
                })
                .show();
    }

    /**
     * Deletes the selected ingredient and refreshes the dashboard.
     */
    private void deleteIngredient(long ingredientId) {
        int rowsDeleted = databaseHelper.deleteIngredient(ingredientId);

        if (rowsDeleted > 0) {
            Toast.makeText(
                    this,
                    "Ingredient deleted.",
                    Toast.LENGTH_SHORT
            ).show();

            loadIngredients();
        } else {
            Toast.makeText(
                    this,
                    "Could not delete the ingredient.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Reads the latest ingredients from SQLite and updates the dashboard.
     */
    private void loadIngredients() {
        ingredients.clear();

        Cursor cursor = databaseHelper.getAllIngredients();

        if (cursor != null) {
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
                    Ingredient ingredient = new Ingredient(
                            cursor.getLong(idColumn),
                            cursor.getString(nameColumn),
                            cursor.getDouble(quantityColumn),
                            cursor.getString(unitColumn),
                            cursor.getString(expiryColumn)
                    );

                    ingredients.add(ingredient);
                }
            } finally {
                // Always close the cursor after reading database results.
                cursor.close();
            }
        }

        // Refresh the two-column ingredient preview.
        ingredientAdapter.notifyDataSetChanged();

        // Update the pantry item count.
        int count = ingredients.size();
        String countLabel = count == 1 ? "ITEM" : "ITEMS";
        tvIngredientCount.setText(String.format("%d %s", count, countLabel));

        // Show the empty state only when there are no saved ingredients.
        if (count == 0) {
            layoutEmptyPantry.setVisibility(View.VISIBLE);
            listPantryIngredients.setVisibility(View.GONE);
        } else {
            layoutEmptyPantry.setVisibility(View.GONE);
            listPantryIngredients.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the dashboard when returning from another screen.
        if (databaseHelper != null && ingredientAdapter != null) {
            loadIngredients();
        }
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
