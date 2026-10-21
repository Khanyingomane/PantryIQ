package com.example.pantryiq;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddIngredientActivity extends AppCompatActivity {

    private EditText etIngredientName;
    private EditText etQuantity;
    private EditText etUnit;
    private EditText etExpiryDate;

    private PantryDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);

        initialiseViews();

        databaseHelper = new PantryDatabaseHelper(this);

        setupButtonListeners();
    }

    // Connect Java variables to the XML views.
    private void initialiseViews() {
        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiryDate = findViewById(R.id.etExpiryDate);
    }

    // Assign behaviour to the form buttons.
    private void setupButtonListeners() {
        Button btnSaveIngredient = findViewById(R.id.btnSaveIngredient);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnSaveIngredient.setOnClickListener(view -> saveIngredient());

        btnCancel.setOnClickListener(view -> finish());
    }

    // Validate and save the ingredient.
    private void saveIngredient() {
        String name = etIngredientName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        if (name.isEmpty()) {
            etIngredientName.setError("Enter an ingredient name.");
            etIngredientName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            etQuantity.setError("Enter a quantity.");
            etQuantity.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException exception) {
            etQuantity.setError("Enter a valid number.");
            etQuantity.requestFocus();
            return;
        }

        if (!Double.isFinite(quantity) || quantity <= 0) {
            etQuantity.setError("Quantity must be greater than zero.");
            etQuantity.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            etUnit.setError("Enter a unit, such as kg, g or pieces.");
            etUnit.requestFocus();
            return;
        }

        if (!expiryDate.isEmpty() && !isValidExpiryDate(expiryDate)) {
            etExpiryDate.setError("Use a valid date in YYYY-MM-DD format.");
            etExpiryDate.requestFocus();
            return;
        }

        try {
            long result = databaseHelper.addIngredient(
                    name, quantity, unit,
                    expiryDate.isEmpty() ? null : expiryDate
            );

            if (result == -1) {
                showMessage("Could not save the ingredient. Please try again.");
                return;
            }

            Toast.makeText(
                    this,
                    "Ingredient saved successfully.",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();

        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        } catch (RuntimeException exception) {
            showMessage("A database error occurred. Please try again.");
        }
    }

    // Check that the optional expiry date is a real calendar date.
    private boolean isValidExpiryDate(String date) {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        dateFormat.setLenient(false);

        ParsePosition position = new ParsePosition(0);
        java.util.Date parsedDate = dateFormat.parse(date, position);

        return parsedDate != null
                && position.getIndex() == date.length();
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    protected void onDestroy() {
        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}