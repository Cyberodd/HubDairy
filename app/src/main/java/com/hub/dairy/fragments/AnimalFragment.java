package com.hub.dairy.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hub.dairy.R;
import com.hub.dairy.adapters.AnimalAdapter;
import com.hub.dairy.dialogs.AnimalDialog;
import com.hub.dairy.dialogs.MeatDialog;
import com.hub.dairy.dialogs.MilkDialog;
import com.hub.dairy.models.Animal;
import com.hub.dairy.ui.ProgressActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.MALE;
import static com.hub.dairy.helpers.Constants.REG_DATE;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class AnimalFragment extends Fragment implements AnimalAdapter.AnimalClick {

    private static final String TAG = "AnimalFragment";
    private String userId;
    private RecyclerView animalRv;
    private List<Animal> mAnimals = new ArrayList<>();
    private CollectionReference animalRef;
    private AnimalAdapter mAnimalAdapter;
    private ProgressBar progress;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animal, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        animalRef = database.collection(ANIMALS);
        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onActivityCreated: User not logged in");
        }

        mAnimalAdapter = new AnimalAdapter(mAnimals, this);

        loadAnimals();
    }

    private void loadAnimals() {
        progress.setVisibility(View.VISIBLE);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        animalRv.setHasFixedSize(true);
        animalRv.setLayoutManager(manager);

        Query animalQuery = animalRef.whereEqualTo(USER_ID, userId);
        animalQuery
                .orderBy(REG_DATE, Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                mAnimals.clear();
                mAnimals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                animalRv.setAdapter(mAnimalAdapter);
                mAnimalAdapter.notifyDataSetChanged();
                progress.setVisibility(View.GONE);
            } else {
                progress.setVisibility(View.GONE);
                Toast.makeText(getContext(), "No animals yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        animalRv = view.findViewById(R.id.animalRecycler);
        progress = view.findViewById(R.id.progress);
    }

    @Override
    public void onAnimalClick(Animal animal, View view) {
        switch (view.getId()) {
            case R.id.animalClick:
                openDialog(animal);
                break;
            case R.id.addProduce:
                openProduceDialog(animal);
                break;
        }
    }

    private void openProduceDialog(Animal animal) {
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.produce_layout);
        initDialog(dialog, animal);
        dialog.show();
    }

    private void initDialog(Dialog dialog, Animal animal) {
        Button btnMeat = dialog.findViewById(R.id.btnMeat);
        Button btnMilk = dialog.findViewById(R.id.btnMilk);

        if (animal.getGender().equals(MALE)) {
            btnMilk.setVisibility(View.GONE);
        } else {
            btnMilk.setVisibility(View.VISIBLE);
        }

        btnMeat.setOnClickListener(v -> openMeatDialog(dialog, animal));

        btnMilk.setOnClickListener(v -> openMilkDialog(dialog, animal));
    }

    private void openMeatDialog(Dialog dialog, Animal animal) {
        MeatDialog meatDialog = new MeatDialog();
        Bundle args = new Bundle();
        args.putParcelable("animal", animal);
        meatDialog.setArguments(args);
        dialog.dismiss();
        meatDialog.show(getChildFragmentManager(), "meatDialog");
    }


    private void openMilkDialog(Dialog dialog, Animal animal) {
        MilkDialog milkDialog = new MilkDialog();
        Bundle args = new Bundle();
        args.putParcelable("animal", animal);
        milkDialog.setArguments(args);
        dialog.dismiss();
        milkDialog.show(getChildFragmentManager(), "MilkDialog");
    }

    private void openDialog(Animal animal) {
        Dialog dialog = new Dialog(Objects.requireNonNull(getContext()));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.options_dialog);
        initDialogViews(dialog, animal);
        dialog.show();
    }

    private void initDialogViews(Dialog dialog, Animal animal) {
        Button btnInfo = dialog.findViewById(R.id.btnAnimalInfo);
        Button btnReport = dialog.findViewById(R.id.btnReport);

        btnInfo.setOnClickListener(v -> openAnimalDialog(animal, dialog));

        btnReport.setOnClickListener(v -> toAnimalReports(animal, dialog));
    }

    private void toAnimalReports(Animal animal, Dialog dialog) {
        Intent intent = new Intent(getContext(), ProgressActivity.class);
        intent.putExtra("animal", animal);
        dialog.dismiss();
        startActivity(intent);
    }

    private void openAnimalDialog(Animal animal, Dialog dialog) {
        AnimalDialog animalDialog = new AnimalDialog();
        Bundle args = new Bundle();
        args.putParcelable("animal", animal);
        animalDialog.setArguments(args);
        dialog.dismiss();
        animalDialog.show(getChildFragmentManager(), "animalDialog");
    }
}
