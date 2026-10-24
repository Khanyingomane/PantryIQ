
package com.example.pantryiq;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "PantryIQSettings";
    private static final String KEY_EXPIRY_REMINDERS =
            "expiry_reminders";
    private static final String KEY_UNIT_PREFERENCE =
            "unit_preference";

    private CheckBox cbExpiryReminders;
    private Spinner spinnerUnitPreference;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Connect the layout controls.
        cbExpiryReminders = findViewById(R.id.cbExpiryReminders);
        spinnerUnitPreference =
                findViewById(R.id.spinnerUnitPreference);

        Button btnSaveSettings =
                findViewById(R.id.btnSaveSettings);
        Button btnBackToPantry =
                findViewById(R.id.btnBackToPantry);

        // Access the app's saved preferences.
        preferences = getSharedPreferences(
                PREFS_NAME,
                MODE_PRIVATE
        );

        // Restore the user's previous settings.
        loadSettings();

        // Save changes when the user taps Save Settings.
        btnSaveSettings.setOnClickListener(view -> saveSettings());

        // Return to the pantry screen.
        btnBackToPantry.setOnClickListener(view -> finish());
    }

    /**
     * Loads saved preferences, using sensible defaults
     * for a first-time visit.
     */
    private void loadSettings() {
        boolean expiryRemindersEnabled =
                preferences.getBoolean(KEY_EXPIRY_REMINDERS, true);

        int savedUnitPosition =
                preferences.getInt(KEY_UNIT_PREFERENCE, 0);

        cbExpiryReminders.setChecked(expiryRemindersEnabled);

        if (savedUnitPosition >= 0
                && savedUnitPosition
                < spinnerUnitPreference.getCount()) {
            spinnerUnitPreference.setSelection(savedUnitPosition);
        } else {
            spinnerUnitPreference.setSelection(0);
        }
    }

    /**
     * Saves the selected settings to the device.
     */
    private void saveSettings() {
        boolean expiryRemindersEnabled =
                cbExpiryReminders.isChecked();

        int selectedUnitPosition =
                spinnerUnitPreference.getSelectedItemPosition();

        boolean saved = preferences.edit()
                .putBoolean(
                        KEY_EXPIRY_REMINDERS,
                        expiryRemindersEnabled
                )
                .putInt(
                        KEY_UNIT_PREFERENCE,
                        selectedUnitPosition
                )
                .commit();

        if (saved) {
            Toast.makeText(
                    this,
                    "Settings saved successfully",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Could not save settings",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
