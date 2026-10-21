package com.example.pantryiq;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Displays pantry ingredients as individual rows in a ListView.
 * Each row shows the ingredient name, quantity, unit, and
 * optional expiry date.
 */
public class IngredientAdapter extends ArrayAdapter<Ingredient> {

    private final LayoutInflater inflater;

    /**
     * Connects the adapter to the context and ingredient list.
     *
     * @param context the screen displaying the pantry
     * @param ingredients ingredients to display
     */
    public IngredientAdapter(Context context,
                             List<Ingredient> ingredients) {
        super(context, 0, ingredients);

        // Used to turn the row XML layout into a View.
        inflater = LayoutInflater.from(context);
    }

    /**
     * Builds the row displayed for each ingredient.
     */
    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {

        View row = convertView;

        // Reuse an existing row when possible to improve scrolling.
        if (row == null) {
            row = inflater.inflate(
                    R.layout.item_ingredient, parent, false);
        }

        // Get the ingredient for this row.
        Ingredient ingredient = getItem(position);

        if (ingredient == null) {
            return row;
        }

        // Connect the row's TextViews to their XML IDs.
        TextView tvName = row.findViewById(R.id.tvIngredientName);
        TextView tvQuantity = row.findViewById(R.id.tvIngredientQuantity);
        TextView tvExpiry = row.findViewById(R.id.tvIngredientExpiry);

        // Display the ingredient name.
        tvName.setText(ingredient.getName());

        // Display the available quantity and its unit.
        String quantityText = ingredient.getQuantity()
                + " " + ingredient.getUnit();
        tvQuantity.setText(quantityText);

        // Show the expiry date only when one was provided.
        String expiryDate = ingredient.getExpiryDate();

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            tvExpiry.setVisibility(View.GONE);
        } else {
            tvExpiry.setVisibility(View.VISIBLE);
            tvExpiry.setText("Expires: " + expiryDate);
        }

        return row;
    }
}