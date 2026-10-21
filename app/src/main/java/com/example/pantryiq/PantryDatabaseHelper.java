package com.example.pantryiq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PantryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pantryiq.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";

    public PantryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_INGREDIENTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_QUANTITY + " REAL NOT NULL CHECK ("
                + COLUMN_QUANTITY + " > 0), "
                + COLUMN_UNIT + " TEXT NOT NULL, "
                + COLUMN_EXPIRY_DATE + " TEXT"
                + ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        // Add database migrations here when the schema changes.
        // Preserve existing pantry data during upgrades.
    }

    // Prepare and validate ingredient data for database operations.
    private ContentValues createIngredientValues(
            String name,
            double quantity,
            String unit,
            String expiryDate) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingredient name is required.");
        }

        if (!Double.isFinite(quantity) || quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be a positive number.");
        }

        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Ingredient unit is required.");
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name.trim());
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_UNIT, unit.trim());

        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            values.putNull(COLUMN_EXPIRY_DATE);
        } else {
            values.put(COLUMN_EXPIRY_DATE, expiryDate.trim());
        }

        return values;
    }

    // CREATE: Add a new ingredient.
    public long addIngredient(String name, double quantity,
                              String unit, String expiryDate) {

        ContentValues values = createIngredientValues(
                name, quantity, unit, expiryDate);

        try {
            SQLiteDatabase db = getWritableDatabase();
            return db.insert(TABLE_INGREDIENTS, null, values);
        } catch (SQLException exception) {
            return -1;
        }
    }

    // READ: Retrieve ingredients in alphabetical order.
    // The caller must close the returned Cursor.
    public Cursor getAllIngredients() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                TABLE_INGREDIENTS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_NAME + " COLLATE NOCASE ASC"
        );
    }

    // UPDATE: Edit an existing ingredient by ID.
    public int updateIngredient(long id, String name,
                                double quantity, String unit,
                                String expiryDate) {

        if (id <= 0) {
            return 0;
        }

        ContentValues values = createIngredientValues(
                name, quantity, unit, expiryDate);

        try {
            SQLiteDatabase db = getWritableDatabase();

            return db.update(
                    TABLE_INGREDIENTS,
                    values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (SQLException exception) {
            return -1;
        }
    }

    // DELETE: Remove an ingredient by ID.
    public int deleteIngredient(long id) {
        if (id <= 0) {
            return 0;
        }

        try {
            SQLiteDatabase db = getWritableDatabase();

            return db.delete(
                    TABLE_INGREDIENTS,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (SQLException exception) {
            return -1;
        }
    }
}