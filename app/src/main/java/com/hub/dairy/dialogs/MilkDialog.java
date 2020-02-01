package com.hub.dairy.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.MilkProduce;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMAL_ID;
import static com.hub.dairy.helpers.Constants.DATE;
import static com.hub.dairy.helpers.Constants.LONG_DATE;
import static com.hub.dairy.helpers.Constants.MILK_PRODUCE;
import static com.hub.dairy.helpers.Constants.SHORT_DATE;

public class MilkDialog extends AppCompatDialogFragment {

    private static final String TAG = "MilkDialog";
    private MilkInterface listener;
    private TextInputLayout txtQuantity;
    private EditText mQuantity;
    private String date, time, userId, animalId, animalName;
    private CollectionReference milkRef;
    private ProgressBar mProgress;
    private boolean canAdd = false;
    private String mMilkProdId, timeOfDay;
    private RadioGroup mRadioGroup;
    private RadioButton rbMorning, rbAfternoon;
    private String mMrgQty;
    private String mEvgQty;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.milk_produce, null);

        mQuantity = view.findViewById(R.id.quantity);
        txtQuantity = view.findViewById(R.id.textQuantity);
        mProgress = view.findViewById(R.id.produceProgress);
        mRadioGroup = view.findViewById(R.id.rgSelection);
        rbMorning = view.findViewById(R.id.rbMorning);
        rbAfternoon = view.findViewById(R.id.rbAfternoon);
        Button submit = view.findViewById(R.id.btnSubmit);

        Bundle bundle = getArguments();
        if (bundle != null) {
            Animal animal = bundle.getParcelable("animal");
            if (animal != null) {
                animalId = animal.getAnimalId();
                animalName = animal.getAnimalName();
            } else {
                Log.d(TAG, "onCreateDialog: Something went wrong");
            }
        } else {
            Log.d(TAG, "onCreateDialog: No Animal passed");
        }

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        date = new SimpleDateFormat(SHORT_DATE, Locale.getDefault()).format(new Date());
        time = new SimpleDateFormat(LONG_DATE, Locale.getDefault()).format(new Date());

        milkRef = database.collection(MILK_PRODUCE);
        mMilkProdId = milkRef.document().getId();

        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        getMilkProduceEntries();

        listenToRadioButton();

        submit.setOnClickListener(v -> saveInfo(alertDialog));
        return alertDialog;
    }

    private void listenToRadioButton() {
        mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rbMorning:
                    timeOfDay = rbMorning.getText().toString();
                    break;
                case R.id.rbAfternoon:
                    timeOfDay = rbAfternoon.getText().toString();
                    break;
            }
        });
    }

    private void getMilkProduceEntries() {
        Query prodQuery = milkRef.whereEqualTo(DATE, date)
                .whereEqualTo(ANIMAL_ID, animalId);
        prodQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                MilkProduce produce = snapshot.toObject(MilkProduce.class);
                if (produce != null){
                    checkFields(produce);
                } else {
                    canAdd = true;
                }
            }
        });
    }

    private void checkFields(MilkProduce produce) {
        mMrgQty = produce.getMrgQty();
        mEvgQty = produce.getEvnQty();
        canAdd = mMrgQty.equals("") || mEvgQty.equals("");
    }

    private void saveInfo(AlertDialog alertDialog) {
        if (canAdd) {
            String quantity = mQuantity.getText().toString();
            if (timeOfDay != null) {
                if (!quantity.isEmpty()) {
                    if (timeOfDay.equals("Morning")) {
                        submitMorningProduce(quantity, alertDialog, quantity);
                    } else {
                        submitAfternoonProduce(quantity, alertDialog);
                    }
                } else {
                    txtQuantity.setError("Please input quantity of milk produced first");
                }
            } else {
                Toast.makeText(getContext(), "Time of day is required", Toast.LENGTH_SHORT).show();
            }
        } else {
            mProgress.setVisibility(View.GONE);
            mQuantity.setText("");
            txtQuantity.setError("Can't enter more than 2 entries on the same day");
        }
    }

    private void submitAfternoonProduce(String quantity, AlertDialog alertDialog) {
        Query query = milkRef.whereEqualTo(ANIMAL_ID, animalId).whereEqualTo(DATE, date);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                MilkProduce produce = snapshot.toObject(MilkProduce.class);
                getMorningQty(produce, quantity, alertDialog, quantity);
            }
        });
    }

    private void getMorningQty(MilkProduce prod, String mQty, AlertDialog dialog, String totalQty) {
        if (prod != null && !prod.getMrgQty().equals("")) {
            String mrgQty = prod.getMrgQty();
            float qty = Float.parseFloat(mrgQty);
            sumAndSaveQty(qty, mQty, dialog, prod);
        } else {
            saveNewEntry(mQty, dialog, totalQty);
        }
    }

    private void saveNewEntry(String quantity, AlertDialog alertDialog, String totalQty) {
        if (mEvgQty.equals("")){
            saveProduce("", quantity, alertDialog, totalQty);
        } else {
            txtQuantity.setError("Produce for selected time already added");
        }
    }

    private void sumAndSaveQty(float qty, String quantity, AlertDialog dialog, MilkProduce produce) {
        String produceId = produce.getProduceId();

        mProgress.setVisibility(View.VISIBLE);
        float evQty = Float.parseFloat(quantity);
        float totalQty = qty + evQty;
        String finalQty = String.format(Locale.ENGLISH, "%.2f", totalQty);

        Map<String, Object> map = new HashMap<>();
        map.put("evnQty", quantity);
        map.put("totalQty", finalQty);
        milkRef.document(produceId).set(map, SetOptions.merge()).addOnSuccessListener(aVoid -> {
            mProgress.setVisibility(View.GONE);
            dialog.dismiss();
        });
    }

    private void submitMorningProduce(String quantity, AlertDialog alertDialog, String totalQty) {
        if (mMrgQty.equals("")){
            saveProduce(quantity, "", alertDialog, totalQty);
        } else {
            txtQuantity.setError("Produce for selected time already added");
        }
    }

    private void saveProduce(String mrgQty, String evgQty, AlertDialog alertDialog, String totalQty) {
        mProgress.setVisibility(View.VISIBLE);
        MilkProduce produce = new MilkProduce(mMilkProdId, userId, animalId, animalName, mrgQty,
                date, time, evgQty, totalQty);
        milkRef.document(mMilkProdId).set(produce).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mProgress.setVisibility(View.GONE);
                mQuantity.setText("");
                listener.notifyInput();
                alertDialog.dismiss();
            } else {
                mProgress.setVisibility(View.GONE);
                mQuantity.setText("");
                Toast.makeText(getContext(), "An error occurred",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            mProgress.setVisibility(View.GONE);
            mQuantity.setText("");
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (MilkInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MilkDialog");
        }
    }

    public interface MilkInterface {
        void notifyInput();
    }
}
