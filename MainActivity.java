package com.example.zakatcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    // Declare UI elements
    private TextInputLayout tilWeight, tilGoldValue;
    private TextInputEditText etWeight, etGoldValue;
    private RadioGroup rgGoldType;
    private RadioButton rbKeep, rbWear;
    private Button btnCalculate, btnReset;
    private TextView tvTotalValue, tvZakatPayable, tvTotalZakat, tvNotice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components from XML layout
        tilWeight = findViewById(R.id.tilWeight);
        tilGoldValue = findViewById(R.id.tilGoldValue);
        etWeight = findViewById(R.id.etWeight);
        etGoldValue = findViewById(R.id.etGoldValue);
        rgGoldType = findViewById(R.id.rgGoldType);
        rbKeep = findViewById(R.id.rbKeep);
        rbWear = findViewById(R.id.rbWear);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnReset = findViewById(R.id.btnReset);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvZakatPayable = findViewById(R.id.tvZakatPayable);
        tvTotalZakat = findViewById(R.id.tvTotalZakat);
        tvNotice = findViewById(R.id.tvNotice);

        // Handle the Calculate button click event
        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateZakat();
            }
        });

        // Handle the Reset button click event
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetForm();
            }
        });
    }

    // Creates the menu items in the Top Action Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // Listens for menu choices
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_share) {
            // Share the app URL to other users
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
            return true;
        } else if (id == R.id.action_about) {
            // Navigate to the About Page
            Intent intent = new Intent(MainActivity.this, AboutPage.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Validates user input and calculates zakat for gold.
     *
     * Formula:
     *   1. Total Value = weight * goldValue
     *   2. Uruf (X) = 85g for Keep, 200g for Wear
     *   3. Payable Weight = weight - X (if > 0)
     *   4. Zakat Payable Value = payableWeight * goldValue
     *   5. Total Zakat = 2.5% * zakatPayableValue
     */
    private void calculateZakat() {
        // Clear previous errors
        tilWeight.setError(null);
        tilGoldValue.setError(null);

        // Get input strings
        String weightStr = etWeight.getText().toString().trim();
        String valueStr = etGoldValue.getText().toString().trim();

        // Validate: check if fields are empty
        if (weightStr.isEmpty()) {
            tilWeight.setError(getString(R.string.error_weight_empty));
            Toast.makeText(this, getString(R.string.error_weight_empty), Toast.LENGTH_SHORT).show();
            etWeight.requestFocus();
            return;
        }

        if (valueStr.isEmpty()) {
            tilGoldValue.setError(getString(R.string.error_value_empty));
            Toast.makeText(this, getString(R.string.error_value_empty), Toast.LENGTH_SHORT).show();
            etGoldValue.requestFocus();
            return;
        }

        // Validate: check if input is a valid number
        double weight;
        double goldValue;

        try {
            weight = Double.parseDouble(weightStr);
        } catch (NumberFormatException e) {
            tilWeight.setError(getString(R.string.error_invalid_number));
            Toast.makeText(this, getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
            etWeight.requestFocus();
            return;
        }

        try {
            goldValue = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            tilGoldValue.setError(getString(R.string.error_invalid_number));
            Toast.makeText(this, getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
            etGoldValue.requestFocus();
            return;
        }

        // Validate: check for negative values
        if (weight <= 0) {
            tilWeight.setError(getString(R.string.error_weight_negative));
            Toast.makeText(this, getString(R.string.error_weight_negative), Toast.LENGTH_SHORT).show();
            etWeight.requestFocus();
            return;
        }

        if (goldValue <= 0) {
            tilGoldValue.setError(getString(R.string.error_value_negative));
            Toast.makeText(this, getString(R.string.error_value_negative), Toast.LENGTH_SHORT).show();
            etGoldValue.requestFocus();
            return;
        }

        // 1. Calculate Total Value of Gold
        double totalValue = weight * goldValue;

        // 2. Set the Uruf (X) value based on category selection
        double urufX = 85.0; // Default is "Keep"
        if (rbWear.isChecked()) {
            urufX = 200.0; // Switch to "Wear"
        }

        // 3. Calculate Zakat Payable Weight and Value
        double payableWeight = weight - urufX;
        double zakatPayableValue = 0.0;

        // If weight is less than or equal to Uruf threshold, no zakat is due
        if (payableWeight > 0) {
            zakatPayableValue = payableWeight * goldValue;
        }

        // 4. Calculate Final Zakat Amount (2.5%)
        double totalZakat = zakatPayableValue * 0.025;

        // Display results formatted to 2 decimal places
        tvTotalValue.setText(String.format("Total Gold Value: RM %.2f", totalValue));
        tvZakatPayable.setText(String.format("Zakat Payable Value: RM %.2f", zakatPayableValue));
        tvTotalZakat.setText(String.format("Total Zakat: RM %.2f", totalZakat));

        // Show helpful notice based on result
        tvNotice.setVisibility(View.VISIBLE);
        if (payableWeight <= 0) {
            tvNotice.setText(getString(R.string.notice_no_zakat));
        } else if (rbKeep.isChecked()) {
            tvNotice.setText(getString(R.string.notice_keep));
        } else {
            tvNotice.setText(getString(R.string.notice_wear));
        }

        // Success feedback
        Toast.makeText(this, "Zakat calculated successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Resets all input fields and output displays to default values.
     */
    private void resetForm() {
        // Clear input fields
        etWeight.setText("");
        etGoldValue.setText("");

        // Reset radio to default (Keep)
        rbKeep.setChecked(true);

        // Clear errors
        tilWeight.setError(null);
        tilGoldValue.setError(null);

        // Reset output displays
        tvTotalValue.setText(getString(R.string.result_total_value));
        tvZakatPayable.setText(getString(R.string.result_zakat_payable));
        tvTotalZakat.setText(getString(R.string.result_total_zakat));

        // Hide notice
        tvNotice.setVisibility(View.GONE);
        tvNotice.setText("");

        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }
}