package com.hub.dairy.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.MilkSale;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.ANIMAL_ID;
import static com.hub.dairy.helpers.Constants.ANIMAL_NAME;
import static com.hub.dairy.helpers.Constants.DATE_FORMAT;
import static com.hub.dairy.helpers.Constants.FEMALE;
import static com.hub.dairy.helpers.Constants.GENDER;
import static com.hub.dairy.helpers.Constants.TRANSACTIONS;

public class TransactionDialog extends AppCompatDialogFragment implements
        DatePickerDialog.OnDateSetListener {

    private static final String TAG = "MilkDialog";
    private Spinner transSpinner;
    private TextInputLayout textAnimal, textDate, textCash, txtQuantity;
    private AutoCompleteTextView mAnimalName;
    private EditText inputDate, inputCash, inputQuantity;
    private String time, userId, mType, animalId, animalName;
    private CollectionReference animalRef, transRef;
    private ProgressBar mProgress;
    private Button milkSale, animalSale;
    private TransInterface mTransInterface;
    private LinearLayout milkPurchase;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.transaction_layout, null);

        initViews(view);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        time = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        animalRef = database.collection(ANIMALS);
        transRef = database.collection(TRANSACTIONS);

        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        loadTransactionType();

        String type = transSpinner.getSelectedItem().toString();
        if (type.equals("Animal Sale")) {
            milkPurchase.setVisibility(View.GONE);
            txtQuantity.setVisibility(View.GONE);
        } else {
            milkPurchase.setVisibility(View.VISIBLE);
        }

        mAnimalName.setOnItemClickListener((parent, view1, position, id) -> {
            animalName = (String) parent.getItemAtPosition(position);
            Query query = animalRef.whereEqualTo(ANIMAL_NAME, animalName).limit(1);
            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Animal animal = snapshot.toObject(Animal.class);
                    animalId = animal.getAnimalId();
                }
            });
        });

        milkSale.setOnClickListener(v -> submitSale());

        inputDate.setOnClickListener(v -> openCalendar());

        animalSale.setOnClickListener(v -> saleAnimal());

        return alertDialog;
    }

    private void saleAnimal() {
        String transId = transRef.document().getId();
        String date = inputDate.getText().toString();
        String cash = inputCash.getText().toString().trim();
        if (!animalName.isEmpty()) {
            if (!date.isEmpty()) {
                if (!cash.isEmpty()) {
                    doSaleAnimal(transId, date, cash);
                } else {
                    textCash.setError("Please enter amount");
                }
            } else {
                textDate.setError("Please select a date");
            }
        } else {
            textAnimal.setError("Please enter animal name");
        }
    }

    private void doSaleAnimal(String transId, String date, String cash) {
        String quantity = "1";
        mProgress.setVisibility(View.VISIBLE);
        MilkSale milkSale = new MilkSale(transId, animalId, quantity, cash, mType, date, userId, time);
        transRef.document(transId).set(milkSale).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                setAvailability(animalId);
                mProgress.setVisibility(View.GONE);
                dismiss();
                mTransInterface.notifyInput("Animal transaction added");
            } else {
                mProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAvailability(String animalId) {
        Query query = animalRef.whereEqualTo(ANIMAL_ID, animalId);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {

        });
    }

    private void submitSale() {
        String transId = transRef.document().getId();
        String date = inputDate.getText().toString();
        String quantity = inputQuantity.getText().toString().trim();
        String cash = inputCash.getText().toString().trim();
        if (!animalName.isEmpty()) {
            if (!date.isEmpty()) {
                if (!quantity.isEmpty()) {
                    if (!cash.isEmpty()) {
                        doSubmit(transId, date, quantity, cash);
                    } else {
                        textCash.setError("Please enter amount");
                    }
                } else {
                    txtQuantity.setError("Please enter quantity");
                }
            } else {
                textDate.setError("Please select a date");
            }
        } else {
            textAnimal.setError("Please enter animal name");
        }
    }

    private void doSubmit(String transId, String date, String quantity, String cash) {
        mProgress.setVisibility(View.VISIBLE);
        MilkSale milkSale = new MilkSale(transId, animalId, quantity, cash, mType, date, userId, time);
        transRef.document(transId).set(milkSale).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mProgress.setVisibility(View.GONE);
                dismiss();
                mTransInterface.notifyInput("Milk transaction added");
            } else {
                mProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCalendar() {
        DatePickerDialog dialog = new DatePickerDialog(Objects.requireNonNull(getContext()),
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void initViews(View view) {
        transSpinner = view.findViewById(R.id.transSpinner);
        textAnimal = view.findViewById(R.id.textAnimal);
        mAnimalName = view.findViewById(R.id.ac_animal_name);
        textDate = view.findViewById(R.id.textDate);
        inputDate = view.findViewById(R.id.inputDate);
        textCash = view.findViewById(R.id.textCash);
        inputCash = view.findViewById(R.id.inputCash);
        milkSale = view.findViewById(R.id.btnMilkSale);
        animalSale = view.findViewById(R.id.btnAnimalSale);
        milkPurchase = view.findViewById(R.id.milkPurchase);
        inputQuantity = view.findViewById(R.id.inputQuantity);
        mProgress = view.findViewById(R.id.transProgress);
        txtQuantity = view.findViewById(R.id.txtQuantity);
    }

    private void loadTransactionType() {
        String[] types = getResources().getStringArray(R.array.type);
        ArrayAdapter<String> typesAdapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                android.R.layout.simple_spinner_item, types);
        typesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        transSpinner.setAdapter(typesAdapter);

        transSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mType = transSpinner.getSelectedItem().toString();
                if (mType.equals("Animal Sale")) {
                    clearFields();
                    hideMilkFields();
                    loadAllAnimals();
                } else if (mType.equals("Choose One")) {
                    hideAllFields();
                } else {
                    clearFields();
                    showMilkFields();
                    loadFemaleAnimals();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "onNothingSelected: Nothing selected");
            }
        });
    }

    private void clearFields() {
        inputDate.setText("");
        mAnimalName.setText("");
        inputQuantity.setText("");
        inputCash.setText("");
    }

    private void showMilkFields() {
        txtQuantity.setVisibility(View.VISIBLE);
        transSpinner.setVisibility(View.VISIBLE);
        textAnimal.setVisibility(View.VISIBLE);
        textDate.setVisibility(View.VISIBLE);
        textCash.setVisibility(View.VISIBLE);
        milkSale.setVisibility(View.VISIBLE);
        animalSale.setVisibility(View.GONE);
    }

    private void hideAllFields() {
        txtQuantity.setVisibility(View.GONE);
        textAnimal.setVisibility(View.GONE);
        textDate.setVisibility(View.GONE);
        textCash.setVisibility(View.GONE);
        milkSale.setVisibility(View.GONE);
    }

    private void hideMilkFields() {
        txtQuantity.setVisibility(View.GONE);
        transSpinner.setVisibility(View.VISIBLE);
        textAnimal.setVisibility(View.VISIBLE);
        inputDate.setVisibility(View.VISIBLE);
        inputCash.setVisibility(View.VISIBLE);
        milkSale.setVisibility(View.GONE);
        animalSale.setVisibility(View.VISIBLE);
    }

    private void loadFemaleAnimals() {
        List<Animal> animals = new ArrayList<>();
        Query query = animalRef.whereEqualTo(GENDER, FEMALE);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                animals.clear();
                animals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                allFemaleAnimals(animals);
            } else {
                Log.d(TAG, "getUserAnimals: No user animals currently");
            }
        });
    }

    private void allFemaleAnimals(List<Animal> animals) {
        List<String> names = new ArrayList<>();
        for (Animal animal : animals) {
            String name = animal.getAnimalName();
            names.add(name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                        android.R.layout.simple_spinner_dropdown_item, names);
        mAnimalName.setAdapter(adapter);
    }

    private void loadAllAnimals() {
        List<Animal> animals = new ArrayList<>();
        animalRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                animals.clear();
                animals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                allAnimalSearch(animals);
            } else {
                Log.d(TAG, "getUserAnimals: No user animals currently");
            }
        });
    }

    private void allAnimalSearch(List<Animal> animals) {
        List<String> names = new ArrayList<>();
        for (Animal animal : animals) {
            String name = animal.getAnimalName();
            names.add(name);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                        android.R.layout.simple_spinner_dropdown_item, names);
        mAnimalName.setAdapter(adapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mTransInterface = (TransInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MilkDialog");
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String date = dayOfMonth + "/" + (month + 1) + "/" + year;
        inputDate.setText(date);
    }

    public interface TransInterface {

        void notifyInput(String message);
    }
}
