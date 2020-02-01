package com.hub.dairy.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.CATEGORY;
import static com.hub.dairy.helpers.Constants.FEMALE;
import static com.hub.dairy.helpers.Constants.GENDER;
import static com.hub.dairy.helpers.Constants.MALE;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class ParentDialog extends AppCompatDialogFragment {

    private static final String TAG = "ParentDialog";
    private Spinner fatherSpinner, motherSpinner;
    private CollectionReference animalRef;
    private String mUserId, category;
    private ParentSet mParentSet;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.parents_dialog, null);

        fatherSpinner = view.findViewById(R.id.fatherSpinner);
        motherSpinner = view.findViewById(R.id.motherSpinner);
        TextView noParents = view.findViewById(R.id.noParents);
        TextView proceed = view.findViewById(R.id.proceed);

        Bundle bundle = getArguments();
        if (bundle != null) {
            category = bundle.getString("category");
        } else {
            Log.d(TAG, "onCreateDialog: no category passed");
        }

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        animalRef = database.collection(ANIMALS);
        if (user != null) {
            mUserId = user.getUid();
        } else {
            Log.d(TAG, "onCreateDialog: User not logged in");
        }

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        loadMales(fatherSpinner, category);
        loadFemales(motherSpinner, category);

        proceed.setOnClickListener(v -> proceed());

        noParents.setOnClickListener(v -> proceedNoParents());

        return alertDialog;
    }

    private void proceedNoParents() {
        String father = "";
        String mother = "";
        mParentSet.proceed(father, mother);
    }

    private void proceed() {
        String father = fatherSpinner.getSelectedItem().toString();
        String mother = motherSpinner.getSelectedItem().toString();
        mParentSet.proceed(father, mother);
    }

    private void loadFemales(Spinner motherSpinner, String category) {
        Query motherQuery = animalRef.whereEqualTo(GENDER, FEMALE)
                .whereEqualTo(CATEGORY, category)
                .whereEqualTo(USER_ID, mUserId);
        motherQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                populateMotherSpinner(queryDocumentSnapshots.toObjects(Animal.class), motherSpinner);
            } else {
                Log.d(TAG, "loadParents: No Animals available currently");
            }
        });
    }

    private void populateMotherSpinner(List<Animal> toObjects, Spinner motherSpinner) {
        if (!toObjects.isEmpty()) {
            List<Animal> mothers = new ArrayList<>(toObjects);
            loadMotherSpinner(mothers, motherSpinner);
        } else {
            Log.d(TAG, "populateMotherSpinner: No female animals found for this user");
        }
    }

    private void loadMotherSpinner(List<Animal> mothers, Spinner motherSpinner) {
        List<String> motherNames = new ArrayList<>();
        for (Animal animal : mothers) {
            String motherName = animal.getAnimalName();
            motherNames.add(motherName);
        }
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                        android.R.layout.simple_spinner_item, motherNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        motherSpinner.setPrompt(getString(R.string.select_a_category));
        motherSpinner.setAdapter(adapter);
    }

    private void loadMales(Spinner fatherSpinner, String category) {
        Query fatherQuery = animalRef.whereEqualTo(GENDER, MALE)
                .whereEqualTo(CATEGORY, category)
                .whereEqualTo(USER_ID, mUserId);
        fatherQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                populateFatherSpinner(queryDocumentSnapshots.toObjects(Animal.class), fatherSpinner);
            } else {
                Log.d(TAG, "loadParents: No Animals available currently");
            }
        });
    }

    private void populateFatherSpinner(List<Animal> toObjects, Spinner fatherSpinner) {
        if (!toObjects.isEmpty()) {
            List<Animal> fathers = new ArrayList<>(toObjects);
            loadFatherSpinner(fathers, fatherSpinner);
        } else {
            Log.d(TAG, "populateFatherSpinner: No male animals found for this user");
        }
    }

    private void loadFatherSpinner(List<Animal> fathers, Spinner fatherSpinner) {
        List<String> fatherNames = new ArrayList<>();
        for (Animal animal : fathers) {
            String fatherName = animal.getAnimalName();
            fatherNames.add(fatherName);
        }
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Objects.requireNonNull(getContext()),
                        android.R.layout.simple_spinner_item, fatherNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fatherSpinner.setPrompt(getString(R.string.select_a_category));
        fatherSpinner.setAdapter(adapter);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mParentSet = (ParentSet) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ParentDialog");
        }
    }

    public interface ParentSet {
        void proceed(String father, String mother);
    }
}
