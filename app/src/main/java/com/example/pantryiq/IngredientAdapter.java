package com.example.pantryiq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

/**
 * Displays pantry ingredients and provides Edit and Delete controls.
 */
public class IngredientAdapter extends ArrayAdapter<Ingredient> {

    private final LayoutInflater inflater;
    private final OnEditClickListener editClickListener;
    private final OnDeleteClickListener deleteClickListener;

    /**
     * Callback for editing an ingredient.
     */
    public interface OnEditClickListener {
        void onEditClick(Ingredient ingredient);
    }

    /**
     * Callback for deleting an ingredient.
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(Ingredient ingredient);
    }

    /**
     * Connects the adapter to the ingredient list and button callbacks.
     */
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

    /**
     * Builds each ingredient row and connects its controls.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;

        if (row == null) {
            row = inflater.inflate(
                    R.layout.item_ingredient, parent, false);
        }

        Ingredient ingredient = getItem(position);

        if (ingredient == null) {
            return row;
        }

        TextView tvName =
                row.findViewById(R.id.tvIngredientName);
        TextView tvQuantity =
                row.findViewById(R.id.tvIngredientQuantity);
        TextView tvExpiry =
                row.findViewById(R.id.tvIngredientExpiry);

        Button btnEdit =
                row.findViewById(R.id.btnEditIngredient);
        Button btnDelete =
                row.findViewById(R.id.btnDeleteIngredient);

        tvName.setText(ingredient.getName());

        String quantityText = ingredient.getQuantity()
                + " " + ingredient.getUnit();
        tvQuantity.setText(quantityText);

        String expiryDate = ingredient.getExpiryDate();

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            tvExpiry.setVisibility(View.GONE);
        } else {
            tvExpiry.setVisibility(View.VISIBLE);
            tvExpiry.setText("Expires: " + expiryDate);
        }

        // Pass the selected ingredient to MainActivity.
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
}