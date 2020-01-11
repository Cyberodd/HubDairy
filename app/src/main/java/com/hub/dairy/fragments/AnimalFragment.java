package com.hub.dairy.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hub.dairy.AnimalActivity;
import com.hub.dairy.AnimalDetailActivity;
import com.hub.dairy.R;
import com.hub.dairy.adapters.AnimalAdapter;
import com.hub.dairy.models.Animal;

import java.util.ArrayList;
import java.util.List;

import static com.hub.dairy.helpers.Constants.ANIMALS;

public class AnimalFragment extends Fragment implements AnimalAdapter.AnimalClick {

    private RecyclerView animalRv;
    private List<Animal> mAnimals = new ArrayList<>();
    private FloatingActionButton mFab, mAddAnimal;
    private boolean isFabOpen = true;
    private TextView txtAddAnimal;
    private CollectionReference animalRef;
    private AnimalAdapter mAnimalAdapter;

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
        animalRef = database.collection(ANIMALS);

        mAnimalAdapter = new AnimalAdapter(mAnimals, this);

        loadAnimals();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFab.setOnClickListener(v -> displayAnimations());

        mAddAnimal.setOnClickListener(v -> navigateToAddAnimal());
    }

    private void navigateToAddAnimal() {
        Intent intent = new Intent(getContext(), AnimalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void displayAnimations() {
        if (!isFabOpen)
            closeFab();
        else
            openFab();
    }

    private void openFab() {
        isFabOpen = false;
        mAddAnimal.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        txtAddAnimal.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        txtAddAnimal.setVisibility(View.VISIBLE);
    }

    private void closeFab() {
        isFabOpen = true;
        mAddAnimal.animate().translationY(0.0F);
        txtAddAnimal.animate().translationY(0.0F);
        txtAddAnimal.setVisibility(View.GONE);
    }

    private void loadAnimals() {
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        animalRv.setHasFixedSize(true);
        animalRv.setLayoutManager(manager);

        animalRef.orderBy("name", Query.Direction.DESCENDING)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                mAnimals.clear();
                mAnimals.addAll(queryDocumentSnapshots.toObjects(Animal.class));
                animalRv.setAdapter(mAnimalAdapter);
                mAnimalAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initViews(View view) {
        animalRv = view.findViewById(R.id.animalRecycler);
        mFab = view.findViewById(R.id.btnFab);
        mAddAnimal = view.findViewById(R.id.btnAddAnimal);
        txtAddAnimal = view.findViewById(R.id.txtAddAnimal);
    }

    @Override
    public void onAnimalClick(Animal animal) {
        Intent intent = new Intent(getContext(), AnimalDetailActivity.class);
        intent.putExtra("animal", animal);
        startActivity(intent);
    }
}
