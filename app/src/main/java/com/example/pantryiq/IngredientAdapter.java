package com.example.pantryiq;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Displays pantry ingredients, expiry reminders, and Edit/Delete controls.
 */
public class IngredientAdapter extends ArrayAdapter<Ingredient> {

    private static final String PREFS_NAME = "PantryIQSettings";
    private static final String KEY_EXPIRY_REMINDERS = "expiry_reminders";
    private static final int REMINDER_DAYS = 7;

    private final LayoutInflater inflater;
    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnEditClickListener {
        void onEditClick(Ingredient ingredient);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Ingredient ingredient);
    }

    public IngredientAdapter(
            Context context,
            List<Ingredient> ingredients,
            OnEditClickListener editClickListener,
            OnDeleteClickListener deleteClickListener) {

        super(context, 0, ingredients);

        inflater = LayoutInflater.from(context);
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        if (row == null) {
            row = inflater.inflate(R.layout.item_ingredient, parent, false);
        }

        Ingredient ingredient = getItem(position);

        if (ingredient == null) {
            return row;
        }

        TextView tvName = row.findViewById(R.id.tvIngredientName);
        TextView tvQuantity = row.findViewById(R.id.tvIngredientQuantity);
        TextView tvExpiry = row.findViewById(R.id.tvIngredientExpiry);

        Button btnEdit = row.findViewById(R.id.btnEditIngredient);
        Button btnDelete = row.findViewById(R.id.btnDeleteIngredient);

        tvName.setText(ingredient.getName());
        tvQuantity.setText(
                ingredient.getQuantity() + " " + ingredient.getUnit()
        );

        displayExpiryStatus(tvExpiry, ingredient.getExpiryDate());

        btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(ingredient);
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(ingredient);
            }
        });

        return row;
    }

    /**
     * Shows expiry warnings only when the user has enabled reminders.
     * Dates are expected in yyyy-MM-dd format.
     */
    private void displayExpiryStatus(TextView tvExpiry, String expiryDate) {

        SharedPreferences preferences = getContext().getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        boolean remindersEnabled = preferences.getBoolean(
                KEY_EXPIRY_REMINDERS,
                true
        );

        if (!remindersEnabled || expiryDate == null
                || expiryDate.trim().isEmpty()) {
            tvExpiry.setVisibility(View.GONE);
            return;
        }

        tvExpiry.setVisibility(View.VISIBLE);

        try {
            LocalDate expiry = LocalDate.parse(expiryDate.trim());
            LocalDate today = LocalDate.now();

            if (expiry.isBefore(today)) {
                tvExpiry.setText("Expired: " + expiryDate);
                tvExpiry.setTextColor(Color.RED);

            } else if (!expiry.isAfter(today.plusDays(REMINDER_DAYS))) {
                tvExpiry.setText("Expiring soon: " + expiryDate);
                tvExpiry.setTextColor(Color.rgb(190, 100, 0));

            } else {
                tvExpiry.setText("Expires: " + expiryDate);
                tvExpiry.setTextColor(Color.DKGRAY);
            }

        } catch (DateTimeParseException exception) {
            // Keep an unrecognised date visible without labelling it incorrectly.
            tvExpiry.setText("Expiry date: " + expiryDate);
            tvExpiry.setTextColor(Color.DKGRAY);
        }
    }
}

