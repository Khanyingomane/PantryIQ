package com.example.pantryiq;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

public class IngredientAdapter extends ArrayAdapter<Ingredient> {

    public interface OnEditClickListener {
        void onEditClick(Ingredient ingredient);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Ingredient ingredient);
    }

    private static final String PREFS_NAME = "PantryIQSettings";
    private static final String KEY_EXPIRY_REMINDERS = "expiry_reminders";
    private static final int REMINDER_DAYS = 7;

    private final Context context;
    private final List<Ingredient> ingredients;
    private final LayoutInflater inflater;

    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;

    /**
     * Matches MainActivity's existing four-argument constructor call.
     */
    public IngredientAdapter(
            Context context,
            List<Ingredient> ingredients,
            OnEditClickListener editClickListener,
            OnDeleteClickListener deleteClickListener
    ) {
        super(context, 0, ingredients);

        this.context = context;
        this.ingredients = ingredients;
        this.inflater = LayoutInflater.from(context);
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;

        if (itemView == null) {
            itemView = inflater.inflate(
                    R.layout.item_pantry_ingredient,
                    parent,
                    false
            );
        }

        Ingredient ingredient = ingredients.get(position);

        // Draw the matching illustration for the current ingredient.
        IngredientImageView ingredientImage =
                itemView.findViewById(R.id.ivIngredientImage);

        if (ingredientImage != null) {
            ingredientImage.setIngredientName(ingredient.getName());
            ingredientImage.setContentDescription(
                    ingredient.getName() + " illustration"
            );
        }

        TextView nameText =
                itemView.findViewById(R.id.tvIngredientName);

        TextView quantityText =
                itemView.findViewById(R.id.tvIngredientQuantity);

        TextView expiryText =
                itemView.findViewById(R.id.tvIngredientExpiry);

        if (nameText != null) {
            nameText.setText(ingredient.getName());
        }

        if (quantityText != null) {
            quantityText.setText(
                    formatQuantity(
                            ingredient.getQuantity(),
                            ingredient.getUnit()
                    )
            );
        }

        if (expiryText != null) {
            displayExpiryStatus(expiryText, ingredient);
        }

        // Connect Edit to MainActivity's existing edit callback.
        View editButton = itemView.findViewById(
                R.id.btnEditIngredient
        );

        if (editButton != null) {
            editButton.setOnClickListener(view -> {
                if (editClickListener != null) {
                    editClickListener.onEditClick(ingredient);
                }
            });
        }

        // Connect Delete to MainActivity's confirmation dialog.
        View deleteButton = itemView.findViewById(
                R.id.btnDeleteIngredient
        );

        if (deleteButton != null) {
            deleteButton.setOnClickListener(view -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(ingredient);
                }
            });
        }

        return itemView;
    }

    private String formatQuantity(double quantity, String unit) {
        String formattedQuantity;

        if (quantity == Math.floor(quantity)) {
            formattedQuantity = String.format(
                    Locale.getDefault(),
                    "%.0f",
                    quantity
            );
        } else {
            formattedQuantity = String.format(
                    Locale.getDefault(),
                    "%s",
                    quantity
            );
        }

        if (unit == null || unit.trim().isEmpty()) {
            return formattedQuantity;
        }

        return formattedQuantity + " " + unit;
    }

    /**
     * Displays expiry information and highlights urgent dates.
     */
    private void displayExpiryStatus(
            TextView expiryText,
            Ingredient ingredient
    ) {
        String expiryDate = ingredient.getExpiryDate();

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            expiryText.setText("Expiry date not set");
            expiryText.setTextColor(
                    android.graphics.Color.parseColor("#6D6273")
            );
            return;
        }

        try {
            LocalDate expiry = LocalDate.parse(expiryDate);
            LocalDate today = LocalDate.now();

            long daysRemaining = ChronoUnit.DAYS.between(
                    today,
                    expiry
            );

            if (daysRemaining < 0) {
                expiryText.setText("Expired");
                expiryText.setTextColor(
                        android.graphics.Color.parseColor("#D32F2F")
                );
            } else if (daysRemaining == 0) {
                expiryText.setText("Expires today");
                expiryText.setTextColor(
                        android.graphics.Color.parseColor("#E65100")
                );
            } else if (
                    isExpiryReminderEnabled()
                            && daysRemaining <= REMINDER_DAYS
            ) {
                expiryText.setText(
                        "Expires in " + daysRemaining + " day(s)"
                );
                expiryText.setTextColor(
                        android.graphics.Color.parseColor("#E65100")
                );
            } else {
                expiryText.setText("Expiry: " + expiryDate);
                expiryText.setTextColor(
                        android.graphics.Color.parseColor("#6D6273")
                );
            }

        } catch (Exception exception) {
            expiryText.setText("Expiry: " + expiryDate);
            expiryText.setTextColor(
                    android.graphics.Color.parseColor("#6D6273")
            );
        }
    }

    private boolean isExpiryReminderEnabled() {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        return preferences.getBoolean(
                KEY_EXPIRY_REMINDERS,
                true
        );
    }
}
