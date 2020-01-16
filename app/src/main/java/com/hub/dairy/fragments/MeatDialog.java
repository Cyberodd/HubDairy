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
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.MeatProduce;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.DATE_FORMAT;
import static com.hub.dairy.helpers.Constants.MEAT_PRODUCE;

public class MeatDialog extends AppCompatDialogFragment {

    private static final String TAG = "MilkDialog";
    private MeatInterface listener;
    private TextInputLayout txtQuantity;
    private EditText mQuantity;
    private String date, userId, animalId, animalName;
    private CollectionReference meatRef;
    private ProgressBar mProgress;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.meat_produce, null);
        mQuantity = view.findViewById(R.id.meat_quantity);
        txtQuantity = view.findViewById(R.id.textMeatQuantity);
        mProgress = view.findViewById(R.id.meatProgress);
        Button submit = view.findViewById(R.id.buttonSubmit);

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
        date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        meatRef = database.collection(MEAT_PRODUCE);

        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        submit.setOnClickListener(v -> saveInfo(alertDialog));
        return alertDialog;
    }

    private void saveInfo(AlertDialog alertDialog) {
        String quantity = mQuantity.getText().toString().trim();
        if (!quantity.isEmpty()) {
            float qty = Float.parseFloat(quantity);
            doSubmitInfo(qty, alertDialog);
        } else {
            txtQuantity.setError("Please input quantity of meat produced first");
        }
    }

    private void doSubmitInfo(float quantity, AlertDialog alertDialog) {
        mProgress.setVisibility(View.VISIBLE);
        String meatProdId = meatRef.document().getId();
        MeatProduce milkProduce =
                new MeatProduce(meatProdId, userId, animalId, animalName, quantity, date);
        meatRef.document(meatProdId).set(milkProduce).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mProgress.setVisibility(View.GONE);
                mQuantity.setText("");
                listener.isSuccess();
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
            listener = (MeatInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement MilkDialog");
        }
    }

    public interface MeatInterface {
        void isSuccess();
    }
}
