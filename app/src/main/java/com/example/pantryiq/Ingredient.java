package com.example.pantryiq;

/**
 * Represents one ingredient stored in the PantryIQ pantry.
 * This class holds ingredient data so it can be passed
 * between the database, adapter, and app screens.
 */
public class Ingredient {

    // Unique database ID for this ingredient.
    private long id;

    // Ingredient details entered by the user.
    private String name;
    private double quantity;
    private String unit;

    // Expiry date is optional and uses YYYY-MM-DD format.
    private String expiryDate;

    /**
     * Creates an ingredient with all its details.
     *
     * @param id         unique database ID
     * @param name       ingredient name
     * @param quantity   amount available
     * @param unit       measurement unit
     * @param expiryDate optional expiry date
     */
    public Ingredient(long id, String name, double quantity,
                      String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    // Returns the database ID.
    public long getId() {
        return id;
    }

    // Updates the database ID when needed.
    public void setId(long id) {
        this.id = id;
    }

    // Returns the ingredient name.
    public String getName() {
        return name;
    }

    // Updates the ingredient name.
    public void setName(String name) {
        this.name = name;
    }

    // Returns the available quantity.
    public double getQuantity() {
        return quantity;
    }

    // Updates the available quantity.
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    // Returns the measurement unit, such as kg or pieces.
    public String getUnit() {
        return unit;
    }

    // Updates the measurement unit.
    public void setUnit(String unit) {
        this.unit = unit;
    }

    // Returns the expiry date, or null if it is unknown.
    public String getExpiryDate() {
        return expiryDate;
    }

    // Updates or clears the optional expiry date.
    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}
