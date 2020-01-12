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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hub.dairy.R;
import com.hub.dairy.models.Category;

import java.util.Objects;

import static com.hub.dairy.helpers.Constants.CATEGORIES;

public class CategoryDialog extends AppCompatDialogFragment {

    private static final String TAG = "CategoryDialog";
    private EditText mCategoryName;
    private String categoryId;
    private CategoryInterface listener;
    private DocumentReference docRef;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = getActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.category_layout, null);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        CollectionReference colRef = database.collection(CATEGORIES);
        categoryId = colRef.document().getId();
        if (user != null){
            String userId = user.getUid();
            docRef = colRef.document(userId);
        } else {
            Log.d(TAG, "onCreateDialog: User not logged in");
        }

        Button save = view.findViewById(R.id.btnSave);
        Button cancel = view.findViewById(R.id.btnCancel);
        mCategoryName = view.findViewById(R.id.categoryName);

        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        setCancelable(false);

        cancel.setOnClickListener(v -> alertDialog.dismiss());

        save.setOnClickListener(v -> saveCategory(alertDialog));

        return alertDialog;
    }

    private void saveCategory(AlertDialog alertDialog) {
        String categoryName = mCategoryName.getText().toString().trim();
        if (!categoryName.isEmpty()) {
            Category category = new Category(categoryId, categoryName);
            docRef.collection(CATEGORIES).document(categoryId).set(category)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.notifyUpdate();
                            alertDialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Something went wrong",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(),
                    Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(getActivity(), "Please enter a category", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (CategoryInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement CategoryDialog");
        }
    }

    public interface CategoryInterface{
        void notifyUpdate();
    }
}
