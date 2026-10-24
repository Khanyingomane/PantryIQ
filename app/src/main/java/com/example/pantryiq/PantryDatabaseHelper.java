package com.example.pantryiq;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages Pantry IQ's SQLite database.
 *
 * Stores pantry ingredients, built-in recipes, recipe ingredients,
 * and recipe preparation instructions.
 */
public class PantryDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pantryiq.db";
    private static final int DATABASE_VERSION = 2;

    // Pantry ingredient table.
    public static final String TABLE_INGREDIENTS = "ingredients";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_EXPIRY_DATE = "expiry_date";

    // Recipe tables.
    public static final String TABLE_RECIPES = "recipes";
    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";
    public static final String TABLE_RECIPE_INSTRUCTIONS =
            "recipe_instructions";

    private static final String COL_RECIPE_ID = "recipe_id";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_PREP_MINUTES = "preparation_minutes";
    private static final String COL_COOK_MINUTES = "cooking_minutes";
    private static final String COL_SERVINGS = "servings";

    private static final String COL_RECIPE_INGREDIENT_ID =
            "recipe_ingredient_id";
    private static final String COL_RECIPE_NAME = "ingredient_name";
    private static final String COL_RECIPE_QUANTITY = "required_quantity";
    private static final String COL_RECIPE_UNIT = "required_unit";

    private static final String COL_INSTRUCTION_ID = "instruction_id";
    private static final String COL_INSTRUCTION_TEXT = "instruction_text";
    private static final String COL_STEP_NUMBER = "step_number";

    public PantryDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Enables foreign-key enforcement for recipe relationships.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Creates all tables and inserts the built-in recipes
     * when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        createIngredientTable(db);
        createRecipeTables(db);
        seedRecipes(db);
    }

    /**
     * Upgrades the database without deleting existing pantry data.
     *
     * Version 2 introduces recipe storage.
     */
    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        if (oldVersion < 2 && newVersion >= 2) {
            createIngredientTable(db);
            createRecipeTables(db);
            seedRecipes(db);
        }
    }

    /**
     * Creates the pantry ingredient table if it does not exist.
     */
    private void createIngredientTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS "
                + TABLE_INGREDIENTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COLUMN_QUANTITY + " REAL NOT NULL CHECK ("
                + COLUMN_QUANTITY + " > 0), "
                + COLUMN_UNIT + " TEXT NOT NULL, "
                + COLUMN_EXPIRY_DATE + " TEXT"
                + ")";

        db.execSQL(sql);
    }

    /**
     * Creates the recipe, recipe ingredient and instruction tables.
     */
    private void createRecipeTables(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + TABLE_RECIPES + " ("
                + COL_RECIPE_ID + " TEXT PRIMARY KEY, "
                + COLUMN_NAME + " TEXT NOT NULL, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_PREP_MINUTES + " INTEGER NOT NULL, "
                + COL_COOK_MINUTES + " INTEGER NOT NULL, "
                + COL_SERVINGS + " INTEGER NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_RECIPE_INGREDIENT_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " TEXT NOT NULL, "
                + COL_RECIPE_NAME + " TEXT NOT NULL, "
                + COL_RECIPE_QUANTITY + " REAL NOT NULL "
                + "CHECK (" + COL_RECIPE_QUANTITY + " > 0), "
                + COL_RECIPE_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPES + "(" + COL_RECIPE_ID + ") "
                + "ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE IF NOT EXISTS "
                + TABLE_RECIPE_INSTRUCTIONS + " ("
                + COL_INSTRUCTION_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " TEXT NOT NULL, "
                + COL_STEP_NUMBER + " INTEGER NOT NULL, "
                + COL_INSTRUCTION_TEXT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPES + "(" + COL_RECIPE_ID + ") "
                + "ON DELETE CASCADE"
                + ")");
    }

    /**
     * Seeds the database with the recipes supplied by RecipeRepository.
     *
     * A transaction prevents partially inserted recipe data.
     * Existing recipe IDs are not duplicated.
     */
    private void seedRecipes(SQLiteDatabase db) {
        db.beginTransaction();

        try {
            List<Recipe> recipes = RecipeRepository.getAllRecipes();

            for (Recipe recipe : recipes) {
                if (recipe == null) {
                    continue;
                }

                ContentValues recipeValues = new ContentValues();
                recipeValues.put(COL_RECIPE_ID, recipe.getId());
                recipeValues.put(COLUMN_NAME, recipe.getName());
                recipeValues.put(
                        COL_DESCRIPTION, recipe.getDescription());
                recipeValues.put(
                        COL_PREP_MINUTES,
                        recipe.getPreparationMinutes());
                recipeValues.put(
                        COL_COOK_MINUTES,
                        recipe.getCookingMinutes());
                recipeValues.put(COL_SERVINGS, recipe.getServings());

                long inserted = db.insertWithOnConflict(
                        TABLE_RECIPES,
                        null,
                        recipeValues,
                        SQLiteDatabase.CONFLICT_IGNORE
                );

                // A recipe already stored in the database is left intact.
                if (inserted == -1) {
                    continue;
                }

                for (Recipe.RecipeIngredient ingredient
                        : recipe.getIngredients()) {

                    ContentValues ingredientValues =
                            new ContentValues();

                    ingredientValues.put(
                            COL_RECIPE_ID, recipe.getId());
                    ingredientValues.put(
                            COL_RECIPE_NAME, ingredient.getName());
                    ingredientValues.put(
                            COL_RECIPE_QUANTITY,
                            ingredient.getQuantity());
                    ingredientValues.put(
                            COL_RECIPE_UNIT, ingredient.getUnit());

                    db.insertOrThrow(
                            TABLE_RECIPE_INGREDIENTS,
                            null,
                            ingredientValues
                    );
                }

                List<String> instructions = recipe.getInstructions();

                for (int i = 0; i < instructions.size(); i++) {
                    ContentValues instructionValues =
                            new ContentValues();

                    instructionValues.put(
                            COL_RECIPE_ID, recipe.getId());
                    instructionValues.put(COL_STEP_NUMBER, i + 1);
                    instructionValues.put(
                            COL_INSTRUCTION_TEXT, instructions.get(i));

                    db.insertOrThrow(
                            TABLE_RECIPE_INSTRUCTIONS,
                            null,
                            instructionValues
                    );
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Validates ingredient fields and creates database values.
     */
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

    /**
     * CREATE: Inserts a new pantry ingredient.
     *
     * @return inserted row ID, or -1 if a database error occurs.
     */
    public long addIngredient(
            String name,
            double quantity,
            String unit,
            String expiryDate) {

        ContentValues values = createIngredientValues(
                name, quantity, unit, expiryDate);

        try {
            return getWritableDatabase().insert(
                    TABLE_INGREDIENTS,
                    null,
                    values
            );
        } catch (SQLException exception) {
            return -1;
        }
    }

    /**
     * READ: Retrieves pantry ingredients alphabetically.
     *
     * The caller is responsible for closing the returned Cursor.
     */
    public Cursor getAllIngredients() {
        return getReadableDatabase().query(
                TABLE_INGREDIENTS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_NAME + " COLLATE NOCASE ASC"
        );
    }

    /**
     * UPDATE: Updates a pantry ingredient using its database ID.
     *
     * @return number of rows updated, or -1 on a database error.
     */
    public int updateIngredient(
            long id,
            String name,
            double quantity,
            String unit,
            String expiryDate) {

        if (id <= 0) {
            return 0;
        }

        ContentValues values = createIngredientValues(
                name, quantity, unit, expiryDate);

        try {
            return getWritableDatabase().update(
                    TABLE_INGREDIENTS,
                    values,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (SQLException exception) {
            return -1;
        }
    }

    /**
     * DELETE: Removes a pantry ingredient using its database ID.
     *
     * @return number of rows deleted, or -1 on a database error.
     */
    public int deleteIngredient(long id) {
        if (id <= 0) {
            return 0;
        }

        try {
            return getWritableDatabase().delete(
                    TABLE_INGREDIENTS,
                    COLUMN_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (SQLException exception) {
            return -1;
        }
    }

    /**
     * Reads recipes from SQLite, including their ingredients
     * and ordered preparation instructions.
     */
    public List<Recipe> getAllRecipesFromDatabase() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor recipeCursor = db.query(
                TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                COLUMN_NAME + " COLLATE NOCASE ASC")) {

            while (recipeCursor.moveToNext()) {
                String id = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow(
                                COL_RECIPE_ID));

                String name = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow(
                                COLUMN_NAME));

                String description = recipeCursor.getString(
                        recipeCursor.getColumnIndexOrThrow(
                                COL_DESCRIPTION));

                int prepMinutes = recipeCursor.getInt(
                        recipeCursor.getColumnIndexOrThrow(
                                COL_PREP_MINUTES));

                int cookMinutes = recipeCursor.getInt(
                        recipeCursor.getColumnIndexOrThrow(
                                COL_COOK_MINUTES));

                int servings = recipeCursor.getInt(
                        recipeCursor.getColumnIndexOrThrow(
                                COL_SERVINGS));

                List<Recipe.RecipeIngredient> ingredients =
                        getRecipeIngredients(db, id);

                List<String> instructions =
                        getRecipeInstructions(db, id);

                // Ignore incomplete records rather than crashing the screen.
                if (ingredients.isEmpty() || instructions.isEmpty()) {
                    continue;
                }

                recipes.add(new Recipe(
                        id,
                        name,
                        description,
                        ingredients,
                        instructions,
                        prepMinutes,
                        cookMinutes,
                        servings
                ));
            }
        }

        return recipes;
    }

    /**
     * Reads all required ingredients belonging to a recipe.
     */
    private List<Recipe.RecipeIngredient> getRecipeIngredients(
            SQLiteDatabase db,
            String recipeId) {

        List<Recipe.RecipeIngredient> ingredients = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLE_RECIPE_INGREDIENTS,
                null,
                COL_RECIPE_ID + " = ?",
                new String[]{recipeId},
                null,
                null,
                COL_RECIPE_INGREDIENT_ID + " ASC")) {

            while (cursor.moveToNext()) {
                ingredients.add(new Recipe.RecipeIngredient(
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COL_RECIPE_NAME)),
                        cursor.getDouble(
                                cursor.getColumnIndexOrThrow(
                                        COL_RECIPE_QUANTITY)),
                        cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        COL_RECIPE_UNIT))
                ));
            }
        }

        return ingredients;
    }

    /**
     * Reads a recipe's instructions in step order.
     */
    private List<String> getRecipeInstructions(
            SQLiteDatabase db,
            String recipeId) {

        List<String> instructions = new ArrayList<>();

        try (Cursor cursor = db.query(
                TABLE_RECIPE_INSTRUCTIONS,
                null,
                COL_RECIPE_ID + " = ?",
                new String[]{recipeId},
                null,
                null,
                COL_STEP_NUMBER + " ASC")) {

            while (cursor.moveToNext()) {
                instructions.add(cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                COL_INSTRUCTION_TEXT)));
            }
        }

        return instructions;
    }
}