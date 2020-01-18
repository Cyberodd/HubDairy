package com.hub.dairy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hub.dairy.R;
import com.hub.dairy.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.USERS;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private Toolbar mToolbar;
    private Button btnLogout, btnSaveInfo, btnEditInfo;
    private String userId;
    private FirebaseAuth mAuth;
    private CollectionReference userRef;
    private EditText name, phone, email, farmName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        userRef = database.collection(USERS);

        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnLogout.setOnClickListener(v -> logOut());

        btnEditInfo.setOnClickListener(v -> enableViews());

        btnSaveInfo.setOnClickListener(v -> updateInfo());

        getUserInfo();
    }

    private void enableViews() {
        name.setEnabled(true);
        phone.setEnabled(true);
        farmName.setEnabled(true);
        btnSaveInfo.setVisibility(View.VISIBLE);
    }

    private void updateInfo() {
        String newName = name.getText().toString().trim();
        String newPhone = phone.getText().toString().trim();
        String newFarmName = farmName.getText().toString().trim();

        doUpdateInfo(newName, newPhone, newFarmName);
    }

    private void doUpdateInfo(String newName, String newPhone, String newFarmName) {
        Map<String, Object> updateUser = new HashMap<>();
        updateUser.put("name", newName);
        updateUser.put("phone", newPhone);
        updateUser.put("farmName", newFarmName);
        userRef.document(userId).set(updateUser, SetOptions.merge()).addOnSuccessListener(aVoid -> {
            name.setEnabled(false);
            phone.setEnabled(false);
            farmName.setEnabled(false);
            btnSaveInfo.setVisibility(View.GONE);
            Toast.makeText(this, "User info updated", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Log.d(TAG, "doUpdateInfo: Error " + e));
    }

    private void getUserInfo() {
        userRef.document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                loadUserDetails(user);
            } else {
                Toast.makeText(this, R.string.failed_user, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Log.d(TAG, "getUserInfo: Error " + e));
    }

    private void loadUserDetails(User user) {
        if (user != null) {
            name.setText(user.getName());
            phone.setText(user.getPhone());
            email.setText(user.getEmail());
            farmName.setText(user.getFarmName());
        } else {
            Log.d(TAG, "loadUserDetails: No user found");
        }
    }

    private void logOut() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.profileToolbar);
        btnLogout = findViewById(R.id.btnLogout);
        name = findViewById(R.id.userName);
        phone = findViewById(R.id.userPhone);
        email = findViewById(R.id.userEmail);
        farmName = findViewById(R.id.userFarmName);
        btnSaveInfo = findViewById(R.id.btnSaveInfo);
        btnEditInfo = findViewById(R.id.btnEditInfo);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }
}
