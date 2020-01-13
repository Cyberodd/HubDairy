package com.hub.dairy.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMALS;

public class TransactionActivity extends AppCompatActivity {

    private static final String TAG = "TransactionActivity";
    private Toolbar mToolbar;
    private Button mAddTans;
    private EditText mSource, mDesc, mAmount;
    private TextInputLayout txtSource, txtDesc, txtAmount, txtAnimal;
    private AutoCompleteTextView mSuggestion;
    private List<Animal> mAnimals = new ArrayList<>();
    private CollectionReference animalRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        if (user != null){
            animalRef = database.collection(ANIMALS).document(user.getUid()).collection(ANIMALS);
        } else {
            Log.d(TAG, "onCreate: Can't fetch user animals right now");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add MilkProduce");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getUserAnimals();

        mAddTans.setOnClickListener(v -> saveTransaction());
    }

    private void getUserAnimals() {
        animalRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()){
                mAnimals.clear();
                mAnimals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                populateSearch(mAnimals);
            } else {
                Log.d(TAG, "getUserAnimals: No user animals currently");
            }
        });
    }

    private void populateSearch(List<Animal> animals) {
        List<String> names = new ArrayList<>();
        for (Animal animal : animals){
            String name = animal.getName();
            names.add(name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        mSuggestion.setAdapter(adapter);
    }

    private void saveTransaction() {
        String animalName = mSuggestion.getText().toString();
        String source = mSource.getText().toString().trim();
        String desc = mDesc.getText().toString().trim();
        String amount = mAmount.getText().toString().trim();

        if (!animalName.isEmpty()){
            if (!source.isEmpty()){
                if (!desc.isEmpty()){
                    if (!amount.isEmpty()){
                        doSaveTransaction(source, desc, amount, animalName);
                    } else {
                        txtAmount.setError("Please enter amount");
                    }
                } else {
                    txtDesc.setError("Please enter description");
                }
            } else {
                txtSource.setError("Please enter source");
            }
        } else {
            txtAnimal.setError("Please choose animal name");
        }
    }

    private void doSaveTransaction(String source, String desc, String amount, String animalName) {

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
        txtAnimal = findViewById(R.id.textAnimal);
    }
}
