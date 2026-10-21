package com.example.pantryiq;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;
    private List<Ingredient> ingredients;
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

        // Connect Java variables to the views in activity_main.xml.
        tvIngredientCount = findViewById(R.id.tvIngredientCount);
        layoutEmptyPantry = findViewById(R.id.layoutEmptyPantry);
        listPantryIngredients = findViewById(R.id.listPantryIngredients);
        Button btnAddIngredient = findViewById(R.id.btnAddIngredient);

        // Prepare the database and the list that holds pantry ingredients.
        databaseHelper = new PantryDatabaseHelper(this);
        ingredients = new ArrayList<>();

        // Attach the custom adapter to the pantry ListView.
        ingredientAdapter = new IngredientAdapter(this, ingredients);
        listPantryIngredients.setAdapter(ingredientAdapter);

        // Open the Add Ingredient screen when the button is tapped.
        btnAddIngredient.setOnClickListener(view -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddIngredientActivity.class
            );
            startActivity(intent);
        });

        // Keep the system bars from covering the screen content.
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

        // Display the current pantry contents.
        loadIngredients();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the list when returning from Add Ingredient.
        // This makes newly saved ingredients appear automatically.
        if (databaseHelper != null && ingredients != null) {
            loadIngredients();
        }
    }

    private void loadIngredients() {
        ingredients.clear();

        // Read all saved ingredients from SQLite.
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

                // Convert each database row into an Ingredient object.
                Ingredient ingredient = new Ingredient(
                        id,
                        name,
                        quantity,
                        unit,
                        expiryDate
                );

                ingredients.add(ingredient);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }

        // Tell the adapter to redraw the ingredient list.
        ingredientAdapter.notifyDataSetChanged();

        // Update the item count.
        int count = ingredients.size();
        String countText = String.format(
                Locale.getDefault(),
                "%d %s",
                count,
                count == 1 ? "ITEM" : "ITEMS"
        );
        tvIngredientCount.setText(countText);

        // Show the empty message only when the pantry has no ingredients.
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

        // Release the database helper when this screen is destroyed.
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}