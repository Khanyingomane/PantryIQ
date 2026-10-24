package com.example.pantryiq;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
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
    private ListView listPantryIngredients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge screen display.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Connect Java variables to the layout views.
        tvIngredientCount = findViewById(R.id.tvIngredientCount);
        layoutEmptyPantry = findViewById(R.id.layoutEmptyPantry);
        listPantryIngredients = findViewById(R.id.listPantryIngredients);
        Button btnAddIngredient = findViewById(R.id.btnAddIngredient);

        // Prepare the database and ingredient collection.
        databaseHelper = new PantryDatabaseHelper(this);
        ingredients = new ArrayList<>();

        // Connect the adapter to the list and its action callbacks.
        ingredientAdapter = new IngredientAdapter(
                this,
                ingredients,
                this::editIngredient,
                this::confirmDeleteIngredient
        );

        listPantryIngredients.setAdapter(ingredientAdapter);

        // Open the form to add a new ingredient.
        btnAddIngredient.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );
            startActivity(intent);
        });

        // Keep system bars from covering the screen content.
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
     * Opens the ingredient form with the selected ingredient's ID.
     * AddIngredientActivity must load that record for editing.
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
     * Requests confirmation before permanently deleting an ingredient.
     */
    private void confirmDeleteIngredient(Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient")
                .setMessage(
                        "Are you sure you want to delete "
                                + ingredient.getName() + "?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) ->
                        deleteIngredient(ingredient))
                .show();
    }

    /**
     * Deletes the selected ingredient and refreshes the pantry.
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

        // Refresh the pantry when returning from another screen.
        if (databaseHelper != null && ingredients != null) {
            loadIngredients();
        }
    }

    /**
     * Loads saved ingredients from SQLite and refreshes the interface.
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

        // Refresh the displayed ingredient rows.
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

        // Toggle between the empty message and ingredient list.
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
        super.onDestroy();

        // Release the database helper when the activity is destroyed.
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}