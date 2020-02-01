package com.hub.dairy.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hub.dairy.helpers.Constants.ANIMALS;

public class AnimalDialog extends AppCompatDialogFragment {

    private static final String TAG = "AnimalDialog";
    private EditText name, gender, location, breed, category, status, regDate, availability;
    private CircleImageView animalImg;
    private TextView txtUpdate;
    private Button btnUpdate;
    private Animal mAnimal;
    private CollectionReference animalRef;
    private boolean isClicked = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.animal_dialog, null);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        animalRef = database.collection(ANIMALS);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mAnimal = bundle.getParcelable("animal");
        } else {
            Log.d(TAG, "onCreateDialog: No Animal passed");
        }

        initViews(view);

        showInfoInViews(view);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();

        btnUpdate.setOnClickListener(v -> updateInfo());

        txtUpdate.setOnClickListener(v -> enableViews());

        animalImg.setOnClickListener(v -> openGallery());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        return alertDialog;
    }

    private void openGallery() {
        if (isClicked) {
            Toast.makeText(getContext(), "Opening gallery", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Update not clicked", Toast.LENGTH_SHORT).show();
        }
    }

    private void enableViews() {
        isClicked = true;
        btnUpdate.setVisibility(View.VISIBLE);
        name.setEnabled(true);
        location.setEnabled(true);
        breed.setEnabled(true);
        status.setEnabled(true);
    }

    private void showInfoInViews(View view) {
        name.setText(mAnimal.getAnimalName());
        gender.setText(mAnimal.getGender());
        location.setText(mAnimal.getLocation());
        breed.setText(mAnimal.getAnimalBreed());
        category.setText(mAnimal.getCategory());
        status.setText(mAnimal.getStatus());
        regDate.setText(mAnimal.getRegDate());
        availability.setText(mAnimal.getAvailability());

        Glide.with(view.getContext()).load(mAnimal.getImageUrl()).into(animalImg);
    }

    private void updateInfo() {
        if (!name.isEnabled() || !location.isEnabled() || !breed.isEnabled()) {
            Toast.makeText(getContext(), R.string.unable_to_update, Toast.LENGTH_SHORT).show();
        } else {
            String updateName = name.getText().toString().trim();
            String updateLoc = location.getText().toString().trim();
            String updateBreed = breed.getText().toString().trim();

            Map<String, Object> updateInfo = new HashMap<>();
            updateInfo.put("animalName", updateName);
            updateInfo.put("location", updateLoc);
            updateInfo.put("animalBreed", updateBreed);

            animalRef.document(mAnimal.getAnimalId())
                    .set(updateInfo, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                name.setEnabled(false);
                location.setEnabled(false);
                breed.setEnabled(false);
                status.setEnabled(false);
                btnUpdate.setVisibility(View.GONE);
                Toast.makeText(getContext(), R.string.success_msg, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void initViews(View view) {
        animalImg = view.findViewById(R.id.animalImg);
        name = view.findViewById(R.id.animalName);
        gender = view.findViewById(R.id.animalGender);
        location = view.findViewById(R.id.animalLocation);
        breed = view.findViewById(R.id.animalBreed);
        category = view.findViewById(R.id.animalCategory);
        status = view.findViewById(R.id.animalStatus);
        regDate = view.findViewById(R.id.animalRegDate);
        availability = view.findViewById(R.id.animalAvailability);
        btnUpdate = view.findViewById(R.id.btnUpdateInfo);
        txtUpdate = view.findViewById(R.id.txtUpdate);
    }

}
