package com.hub.dairy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.hub.dairy.models.Animal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransactionActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private Button mAddTans;
    private EditText mSource, mDesc, mAmount;
    private TextInputLayout txtSource, txtDesc, txtAmount;
    private AutoCompleteTextView mSuggestion;
    private List<String> mAnimals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Transaction");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getUserAnimals();

        mAddTans.setOnClickListener(v -> saveTransaction());
    }

    private void getUserAnimals() {
        mAnimals.add("One");
        mAnimals.add("Two");
        mAnimals.add("Three");
        mAnimals.add("Four");
        mAnimals.add("Five");
        mAnimals.add("Six");
        mAnimals.add("Seven");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, mAnimals);
        mSuggestion.setAdapter(adapter);
    }

    private void saveTransaction() {
        String source = mSource.getText().toString().trim();
        String desc = mDesc.getText().toString().trim();
        String amount = mAmount.getText().toString().trim();

        if (!source.isEmpty()){
            if (!desc.isEmpty()){
                if (!amount.isEmpty()){
                    doSaveTransaction(source, desc, amount);
                } else {
                    txtAmount.setError("Please enter amount");
                }
            } else {
                txtDesc.setError("Please enter description");
            }
        } else {
            txtSource.setError("Please enter source");
        }
    }

    private void doSaveTransaction(String source, String desc, String amount) {
        // TODO: 1/12/2020 Handle saving transactions
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    private void initViews() {
        mToolbar = findViewById(R.id.transactionToolbar);
        mAddTans = findViewById(R.id.btnAddTrans);
        mSource = findViewById(R.id.transSource);
        mDesc = findViewById(R.id.transDesc);
        mAmount = findViewById(R.id.transAmount);
        txtSource = findViewById(R.id.sourceText);
        txtDesc = findViewById(R.id.descText);
        txtAmount = findViewById(R.id.amountText);
        mSuggestion = findViewById(R.id.animalSuggestion);
    }
}
