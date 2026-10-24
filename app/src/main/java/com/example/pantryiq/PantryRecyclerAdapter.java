package com.example.pantryiq;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PantryRecyclerAdapter
        extends RecyclerView.Adapter<PantryRecyclerAdapter.IngredientViewHolder> {

    public interface OnIngredientActionListener {
        void onEdit(Ingredient ingredient);
        void onDelete(Ingredient ingredient);
    }

    private final Context context;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final OnIngredientActionListener actionListener;

    public PantryRecyclerAdapter(
            Context context,
            OnIngredientActionListener actionListener
    ) {
        this.context = context;
        this.actionListener = actionListener;
    }

    // Creates each ingredient card from the existing pantry item layout.
    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_pantry_ingredient,
                parent,
                false
        );

        return new IngredientViewHolder(view);
    }

    // Connects the ingredient data and its edit/delete actions to a card.
    @Override
    public void onBindViewHolder(
            @NonNull IngredientViewHolder holder,
            int position
    ) {
        Ingredient ingredient = ingredients.get(position);

        holder.nameText.setText(ingredient.getName());
        holder.quantityText.setText(formatQuantity(
                ingredient.getQuantity(),
                ingredient.getUnit()
        ));

        displayExpiry(holder.expiryText, ingredient.getExpiryDate());

        if (holder.ingredientImage != null) {
            holder.ingredientImage.setIngredientName(ingredient.getName());
            holder.ingredientImage.setContentDescription(
                    ingredient.getName() + " illustration"
            );
        }

        holder.editButton.setOnClickListener(view -> {
            if (actionListener != null) {
                actionListener.onEdit(ingredient);
            }
        });

        holder.deleteButton.setOnClickListener(view -> {
            if (actionListener != null) {
                actionListener.onDelete(ingredient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    // Replaces the displayed data after the database is loaded or searched.
    public void setIngredients(List<Ingredient> newIngredients) {
        ingredients.clear();
        ingredients.addAll(newIngredients);
        notifyDataSetChanged();
    }

    private String formatQuantity(double quantity, String unit) {
        String amount = quantity == Math.floor(quantity)
                ? String.format(Locale.getDefault(), "%.0f", quantity)
                : String.format(Locale.getDefault(), "%s", quantity);

        return unit == null || unit.trim().isEmpty()
                ? amount
                : amount + " " + unit;
    }

    private void displayExpiry(TextView view, String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            view.setText("Expiry date not set");
            view.setTextColor(Color.parseColor("#6D6273"));
            return;
        }

        try {
            LocalDate expiry = LocalDate.parse(expiryDate);
            long daysRemaining = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    expiry
            );

            if (daysRemaining < 0) {
                view.setText("Expired");
                view.setTextColor(Color.parseColor("#D32F2F"));
            } else if (daysRemaining == 0) {
                view.setText("Expires today");
                view.setTextColor(Color.parseColor("#E65100"));
            } else if (daysRemaining <= 7) {
                view.setText("Expires in " + daysRemaining + " day(s)");
                view.setTextColor(Color.parseColor("#E65100"));
            } else {
                view.setText("Expiry: " + expiryDate);
                view.setTextColor(Color.parseColor("#6D6273"));
            }
        } catch (Exception exception) {
            view.setText("Expiry: " + expiryDate);
            view.setTextColor(Color.parseColor("#6D6273"));
        }
    }

    // Holds references to the views in one ingredient card.
    static class IngredientViewHolder extends RecyclerView.ViewHolder {

        final TextView nameText;
        final TextView quantityText;
        final TextView expiryText;
        final View editButton;
        final View deleteButton;
        final IngredientImageView ingredientImage;

        IngredientViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.tvIngredientName);
            quantityText = itemView.findViewById(R.id.tvIngredientQuantity);
            expiryText = itemView.findViewById(R.id.tvIngredientExpiry);
            editButton = itemView.findViewById(R.id.btnEditIngredient);
            deleteButton = itemView.findViewById(R.id.btnDeleteIngredient);
            ingredientImage = itemView.findViewById(R.id.ivIngredientImage);
        }
    }
}
