package com.hub.dairy.fragments;

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
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.MilkProduce;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
    private int produceCount;
    private String mMilkProdId;

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

        submit.setOnClickListener(v -> saveInfo(alertDialog));
        return alertDialog;
    }

    private void getMilkProduceEntries() {
        Query prodQuery = milkRef.whereEqualTo(DATE, date)
                .whereEqualTo(ANIMAL_ID, animalId);
        prodQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                produceCount = queryDocumentSnapshots.size();
            } else {
                produceCount = 0;
            }
        });
    }

    private void saveInfo(AlertDialog alertDialog) {
        String quantity = mQuantity.getText().toString();
        if (!quantity.isEmpty()) {
            txtQuantity.setError("");
            doSubmitInfo(quantity, alertDialog);
        } else {
            txtQuantity.setError("Please input quantity of milk produced first");
        }
    }

    private void doSubmitInfo(String quantity, AlertDialog alertDialog) {
        mProgress.setVisibility(View.VISIBLE);
        MilkProduce milkProd =
                new MilkProduce(mMilkProdId, userId, animalId, animalName, quantity, date, time);
        if (produceCount < 2) {
            milkRef.document(mMilkProdId).set(milkProd).addOnCompleteListener(task -> {
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
        } else {
            mProgress.setVisibility(View.GONE);
            mQuantity.setText("");
            txtQuantity.setError("Can't enter more than 2 entries on the same day");
        }
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
