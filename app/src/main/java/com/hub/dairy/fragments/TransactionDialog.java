package com.hub.dairy.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.MilkProduce;
import com.hub.dairy.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.ANIMAL_ID;
import static com.hub.dairy.helpers.Constants.ANIMAL_NAME;
import static com.hub.dairy.helpers.Constants.AVAILABLE;
import static com.hub.dairy.helpers.Constants.DATE;
import static com.hub.dairy.helpers.Constants.FEMALE;
import static com.hub.dairy.helpers.Constants.GENDER;
import static com.hub.dairy.helpers.Constants.LONG_DATE;
import static com.hub.dairy.helpers.Constants.MILK_PRODUCE;
import static com.hub.dairy.helpers.Constants.PRICE_PER_LITRE;
import static com.hub.dairy.helpers.Constants.REG_DATE;
import static com.hub.dairy.helpers.Constants.SHORT_DATE;
import static com.hub.dairy.helpers.Constants.STATUS;
import static com.hub.dairy.helpers.Constants.TIME;
import static com.hub.dairy.helpers.Constants.TRANSACTIONS;
import static com.hub.dairy.helpers.Constants.TYPE;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class TransactionDialog extends AppCompatDialogFragment implements
        DatePickerDialog.OnDateSetListener {

    private static final String TAG = "MilkDialog";
    private Spinner transSpinner;
    private TextInputLayout textAnimal, textDate, textCash, txtQuantity;
    private AutoCompleteTextView mAnimalName;
    private EditText inputDate, inputCash, inputQuantity;
    private String time, today, userId, mType, animalId, animalName;
    private CollectionReference animalRef, transRef, milkRef;
    private ProgressBar mProgress;
    private Button milkSale, animalSale;
    private TransInterface mTransInterface;
    private Animal mAnimal;
    private List<Animal> mAnimals;
    private List<Animal> allAnimals;
    private List<String> names;
    private List<String> allAnimalNames;
    private List<MilkProduce> mMilkProduces;

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

        today = new SimpleDateFormat(SHORT_DATE, Locale.getDefault()).format(new Date());
        time = new SimpleDateFormat(LONG_DATE, Locale.getDefault()).format(new Date());
        animalRef = database.collection(ANIMALS);
        transRef = database.collection(TRANSACTIONS);
        milkRef = database.collection(MILK_PRODUCE);

        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        loadTransactionType();

        mAnimalName.setOnItemClickListener((parent, view1, position, id) -> {
            animalName = (String) parent.getItemAtPosition(position);
            Query query = animalRef.whereEqualTo(ANIMAL_NAME, animalName).limit(1);
            query.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                    mAnimal = snapshot.toObject(Animal.class);
                    animalId = mAnimal.getAnimalId();
                }
            });
        });

        milkSale.setOnClickListener(v -> submitSale());

        inputDate.setOnClickListener(v -> openCalendar());

        animalSale.setOnClickListener(v -> saleAnimal());

        Objects.requireNonNull(alertDialog.getWindow()).setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        return alertDialog;
    }

    private void saleAnimal() {
        String transId = transRef.document().getId();
        String date = inputDate.getText().toString();
        String cash = inputCash.getText().toString().trim();
        validateAllAnimalNames();
        if (!mAnimalName.getText().toString().isEmpty()) {
            textAnimal.setError("");
            if (allAnimalNames.contains(mAnimalName.getText().toString().trim())) {
                textAnimal.setError("");
                if (!date.isEmpty()) {
                    textDate.setError("");
                    if (date.equals(today)) {
                        if (!cash.isEmpty()) {
                            doSaleAnimal(transId, date, cash);
                        } else {
                            textCash.setError("");
                            textCash.setError("Please enter amount");
                        }
                    } else {
                        textDate.setError("Please enter today's date");
                    }
                } else {
                    textDate.setError("Please select a date");
                }
            } else {
                textAnimal.setError("Invalid animal name");
            }
        } else {
            textAnimal.setError("Animal name required");
        }
    }

    private void validateAllAnimalNames() {
        allAnimalNames = new ArrayList<>();
        for (Animal animal : allAnimals) {
            String name = animal.getAnimalName();
            allAnimalNames.add(name);
        }
    }

    private void doSaleAnimal(String transId, String date, String cash) {
        String quantity = "1";
        mProgress.setVisibility(View.VISIBLE);
        Transaction transaction =
                new Transaction(transId, animalId, quantity, cash, mType, date, userId, time, "");
        transRef.document(transId).set(transaction).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                setAvailability(animalId);
            } else {
                mProgress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAvailability(String animalId) {
        Map<String, Object> map = new HashMap<>();
        map.put("availability", "sold");
        animalRef.document(animalId)
                .set(map, SetOptions.merge()).addOnSuccessListener(aVoid -> {
            mProgress.setVisibility(View.GONE);
            dismiss();
            mTransInterface.notifyInput("Animal transaction added");
        });
    }

    private void validateAnimalName() {
        names = new ArrayList<>();
        for (Animal animal : mAnimals) {
            String name = animal.getAnimalName();
            names.add(name);
        }
    }

    private void submitSale() {
        getMilkProduce();
    }

    private void validateInputs() {
        String transId = transRef.document().getId();
        String date = inputDate.getText().toString();
        String quantity = inputQuantity.getText().toString().trim();
        validateAnimalName();
        if (!mAnimalName.getText().toString().isEmpty()) {
            textAnimal.setError("");
            if (names.contains(mAnimalName.getText().toString().trim())) {
                textAnimal.setError("");
                if (!date.isEmpty()) {
                    textDate.setError("");
                    if (date.equals(today)) {
                        if (!quantity.isEmpty()) {
                            txtQuantity.setError("");
                            checkQuantity(transId, date, quantity);
                        } else {
                            txtQuantity.setError("Please enter quantity");
                        }
                    } else {
                        textDate.setError("Please enter today's date");
                    }
                } else {
                    textDate.setError("Please select a date");
                }
            } else {
                textAnimal.setError("Invalid animal name");
            }
        } else {
            textAnimal.setError("Animal name required");
        }
    }

    private void checkQuantity(String transId, String date, String quantity) {
        List<Float> floatList = new ArrayList<>();
        for (int i = 0; i < mMilkProduces.size(); i++) {
            String qty = mMilkProduces.get(i).getQuantity();
            float f = Float.parseFloat(qty);
            floatList.add(f);
        }
        sumQuantities(floatList, transId, date, quantity);
    }

    private void sumQuantities(List<Float> floatList, String transId, String date, String quantity) {
        float totalQty = 0;
        for (int i = 0; i < floatList.size(); i++) {
            totalQty += floatList.get(i);
        }
        performCheck(totalQty, transId, date, quantity);
    }

    private void performCheck(float totalQty, String transId, String date, String quantity) {
        float qty = Float.parseFloat(quantity);
        if (qty <= totalQty) {
            float totalCash = qty * PRICE_PER_LITRE;
            txtQuantity.setError("");
            String finalPrice = String.format(Locale.ENGLISH, "%.2f", totalCash);
            fetchPrevTransactions(transId, date, quantity, finalPrice);
        } else {
            txtQuantity.setError("Total quantity produced today was " + totalQty + " litres");
            Toast.makeText(getContext(), R.string.error_qty_info, Toast.LENGTH_SHORT).show();
        }
    }

    private void getMilkProduce() {
        String date = new SimpleDateFormat(SHORT_DATE, Locale.getDefault()).format(new Date());
        Query query = milkRef.whereEqualTo(ANIMAL_ID, animalId).whereEqualTo(DATE, date);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots != null) {
                mMilkProduces = new ArrayList<>();
                mMilkProduces.addAll(queryDocumentSnapshots.toObjects(MilkProduce.class));
                checkProducesSize(mMilkProduces);
            } else {
                textAnimal.setError("Milk produce not added for the selected animal");
            }
        });
    }

    private void checkProducesSize(List<MilkProduce> milkProduces) {
        if (!milkProduces.isEmpty()) {
            validateInputs();
        } else {
            textAnimal.setError("Milk produce not added for the selected animal");
        }
    }

    private void fetchPrevTransactions(String transId, String date, String quantity, String cash) {
        Query query = transRef.whereEqualTo(ANIMAL_ID, animalId)
                .whereEqualTo(TYPE, mType)
                .whereEqualTo(USER_ID, userId)
                .orderBy(TIME, Query.Direction.DESCENDING);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Transaction> transactions = new ArrayList<>();
            if (!queryDocumentSnapshots.isEmpty()) {
                transactions = queryDocumentSnapshots.toObjects(Transaction.class);
                getPrevTransId(transactions, transId, date, quantity, cash);
            } else {
                getPrevTransId(transactions, transId, date, quantity, cash);
                Log.d(TAG, "fetchPrevTransactions: No transactions yet");
            }
        });
    }

    private void getPrevTransId(List<Transaction> transactions, String transId, String date,
                                String quantity, String cash) {
        String prevTransId;
        if (!transactions.isEmpty()) {
            prevTransId = transactions.get(0).getTransId();
            Log.d(TAG, "getPrevTransId: transId " + prevTransId);
            doSubmit(transId, date, quantity, cash, prevTransId);
        } else {
            prevTransId = "";
            doSubmit(transId, date, quantity, cash, prevTransId);
        }
    }

    private void doSubmit(String transId, String date, String quantity, String cash, String prevTransId) {
        mProgress.setVisibility(View.VISIBLE);
        Transaction transaction = new Transaction(
                transId, animalId, quantity, cash, mType, date, userId, time, prevTransId);
        transRef.document(transId).set(transaction).addOnCompleteListener(task -> {
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
                    clearErrors();
                    hideMilkFields();
                    loadAllAnimals();
                } else if (mType.equals("Choose One")) {
                    hideAllFields();
                } else {
                    clearErrors();
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

    private void clearErrors() {
        inputDate.setText("");
        mAnimalName.setText("");
        inputQuantity.setText("");
        inputCash.setText("");
        textDate.setError("");
        textCash.setError("");
        textAnimal.setError("");
        txtQuantity.setError("");
    }

    private void showMilkFields() {
        transSpinner.setVisibility(View.VISIBLE);
        textAnimal.setVisibility(View.VISIBLE);
        textDate.setVisibility(View.VISIBLE);
        txtQuantity.setVisibility(View.VISIBLE);
        textCash.setVisibility(View.GONE);
        milkSale.setVisibility(View.VISIBLE);
        animalSale.setVisibility(View.GONE);
    }

    private void hideAllFields() {
        txtQuantity.setVisibility(View.GONE);
        textAnimal.setVisibility(View.GONE);
        textDate.setVisibility(View.GONE);
        textCash.setVisibility(View.GONE);
        milkSale.setVisibility(View.GONE);
        animalSale.setVisibility(View.GONE);
    }

    private void hideMilkFields() {
        txtQuantity.setVisibility(View.GONE);
        transSpinner.setVisibility(View.VISIBLE);
        textAnimal.setVisibility(View.VISIBLE);
        textDate.setVisibility(View.VISIBLE);
        textCash.setVisibility(View.VISIBLE);
        milkSale.setVisibility(View.GONE);
        animalSale.setVisibility(View.VISIBLE);
    }

    private void loadFemaleAnimals() {
        mAnimals = new ArrayList<>();
        Query query = animalRef.whereEqualTo(GENDER, FEMALE).whereEqualTo(USER_ID, userId);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                mAnimals.clear();
                mAnimals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                allFemaleAnimals(mAnimals);
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
        allAnimals = new ArrayList<>();
        Query query = animalRef.whereEqualTo(USER_ID, userId)
                .whereEqualTo(STATUS, AVAILABLE)
                .orderBy(REG_DATE, Query.Direction.DESCENDING);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                allAnimals.clear();
                allAnimals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                allAnimalSearch(allAnimals);
            } else {
                Log.d(TAG, "getUserAnimals: No user animals currently");
            }
        });
    }

    private void allAnimalSearch(List<Animal> allAnimals) {
        List<String> names = new ArrayList<>();
        for (Animal animal : allAnimals) {
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
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat(SHORT_DATE, Locale.ENGLISH);

        String newDate = dateFormat.format(calendar.getTime());
        inputDate.setText(newDate);
    }

    public interface TransInterface {

        void notifyInput(String message);
    }
}
