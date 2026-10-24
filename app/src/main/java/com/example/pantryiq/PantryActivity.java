package com.example.pantryiq;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PantryActivity extends AppCompatActivity {

    private PantryDatabaseHelper databaseHelper;
    private PantryRecyclerAdapter pantryAdapter;

    private final List<Ingredient> allIngredients = new ArrayList<>();

    private EditText searchIngredients;
    private RecyclerView pantryRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_pantry_list);

        // Open the existing database; do not create a second pantry database.
        databaseHelper = new PantryDatabaseHelper(this);

        searchIngredients = findViewById(R.id.etSearchIngredients);
        pantryRecyclerView = findViewById(R.id.rvPantryIngredients);

        pantryRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        // Connect the inventory cards to the existing edit/delete workflows.
        pantryAdapter = new PantryRecyclerAdapter(
                this,
                new PantryRecyclerAdapter.OnIngredientActionListener() {
                    @Override
                    public void onEdit(Ingredient ingredient) {
                        openEditScreen(ingredient);
                    }

                    @Override
                    public void onDelete(Ingredient ingredient) {
                        confirmDelete(ingredient);
                    }
                }
        );

        pantryRecyclerView.setAdapter(pantryAdapter);

        // The floating action button opens the existing add-ingredient screen.
        FloatingActionButton addButton =
                findViewById(R.id.fabAddIngredient);

        addButton.setOnClickListener(view -> {
            Intent intent = new Intent(
                    PantryActivity.this,
                    AddIngredientActivity.class
            );
            startActivity(intent);
        });

        setupSearch();
        setupBottomNavigation();

        loadIngredients();
    }

    /**
     * Reads pantry items from the existing SQLite database.
     */
    private void loadIngredients() {
        allIngredients.clear();

        Cursor cursor = databaseHelper.getAllIngredients();

        try {
            int idColumn = cursor.getColumnIndexOrThrow("id");
            int nameColumn = cursor.getColumnIndexOrThrow("name");
            int quantityColumn = cursor.getColumnIndexOrThrow("quantity");
            int unitColumn = cursor.getColumnIndexOrThrow("unit");
            int expiryColumn = cursor.getColumnIndexOrThrow("expiry_date");

            while (cursor.moveToNext()) {
                Ingredient ingredient = new Ingredient(
                        cursor.getLong(idColumn),
                        cursor.getString(nameColumn),
                        cursor.getDouble(quantityColumn),
                        cursor.getString(unitColumn),
                        cursor.getString(expiryColumn)
                );

                allIngredients.add(ingredient);
            }
        } finally {
            cursor.close();
        }

        // Apply the current search text after refreshing the database data.
        filterIngredients(searchIngredients.getText().toString());
    }

    /**
     * Filters the visible ingredient cards by ingredient name.
     */
    private void setupSearch() {
        searchIngredients.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
                // No action needed before the text changes.
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {
                filterIngredients(text.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Filtering is already handled by onTextChanged().
            }
        });
    }

    private void filterIngredients(String query) {
        String searchTerm = query.trim().toLowerCase(Locale.ROOT);
        List<Ingredient> filteredIngredients = new ArrayList<>();

        for (Ingredient ingredient : allIngredients) {
            String name = ingredient.getName();

            if (name != null
                    && name.toLowerCase(Locale.ROOT).contains(searchTerm)) {
                filteredIngredients.add(ingredient);
            }
        }

        pantryAdapter.setIngredients(filteredIngredients);

        TextView subtitle = findViewById(R.id.tvPantrySubtitle);

        if (allIngredients.isEmpty()) {
            subtitle.setText("Your pantry is empty. Add your first ingredient!");
        } else if (filteredIngredients.isEmpty()) {
            subtitle.setText("No ingredients match your search.");
        } else {
            subtitle.setText("Everything you have, all in one place.");
        }
    }

    /**
     * Opens the existing add screen in edit mode using the database ID.
     */
    private void openEditScreen(Ingredient ingredient) {
        Intent intent = new Intent(
                PantryActivity.this,
                AddIngredientActivity.class
        );

        intent.putExtra("ingredient_id", ingredient.getId());
        startActivity(intent);
    }

    /**
     * Requests confirmation before permanently deleting an ingredient.
     */
    private void confirmDelete(Ingredient ingredient) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient?")
                .setMessage(
                        "Remove " + ingredient.getName()
                                + " from your pantry?"
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deletedRows =
                            databaseHelper.deleteIngredient(ingredient.getId());

                    if (deletedRows > 0) {
                        Toast.makeText(
                                this,
                                "Ingredient deleted",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadIngredients();
                    } else {
                        Toast.makeText(
                                this,
                                "Couldn't delete the ingredient",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })
                .show();
    }

    /**
     * Connects the inventory screen's bottom navigation.
     */
    private void setupBottomNavigation() {
        TextView pantryTab = findViewById(R.id.navPantry);
        TextView recipesTab = findViewById(R.id.navRecipes);
        TextView settingsTab = findViewById(R.id.navSettings);

        // Pantry is the screen currently being displayed.
        pantryTab.setOnClickListener(view -> {
            // No navigation needed.
        });

        recipesTab.setOnClickListener(view -> {
            startActivity(new Intent(
                    PantryActivity.this,
                    RecipeSuggestionsActivity.class
            ));
        });

        settingsTab.setOnClickListener(view -> {
            startActivity(new Intent(
                    PantryActivity.this,
                    SettingsActivity.class
            ));
        });
    }

    // Refreshes the list when returning from adding or editing an ingredient.
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null && pantryAdapter != null) {
            loadIngredients();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
