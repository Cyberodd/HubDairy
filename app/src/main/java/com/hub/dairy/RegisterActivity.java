package com.hub.dairy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hub.dairy.models.User;

import static com.hub.dairy.helpers.Constants.USERS;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private TextView mLogin;
    private EditText mName, mEmail, mPassword;
    private Button mRegister;
    private TextInputLayout txtName, txtEmail, txtPassword;
    private FirebaseAuth mAuth;
    private CollectionReference userRef;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        userRef = database.collection(USERS);

        initViews();

        mLogin.setOnClickListener(v -> toLogin());

        mRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (!name.isEmpty()) {
            if (!email.isEmpty()) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!password.isEmpty()) {
                        if (password.length() >= 6) {
                            doRegister(name, email, password);
                        } else {
                            txtPassword.setError("Password too short");
                        }
                    } else {
                        txtPassword.setError("Please enter your preferred password");
                    }
                } else {
                    txtEmail.setError("Enter a valid email");
                }
            } else {
                txtEmail.setError("Please enter your email");
            }
        } else {
            txtName.setError("Please enter your name");
        }
    }

    private void doRegister(String name, String email, String password) {
        mProgress.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                saveInfo(name, email);
            } else {
                mProgress.setVisibility(View.GONE);
                Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            mProgress.setVisibility(View.GONE);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveInfo(String name, String email) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null){
            updateUserInfo(name, email, user.getUid());
        } else {
            Log.d(TAG, "saveInfo: Unable to fetch user");
        }
    }

    private void updateUserInfo(String name, String email, String uid) {
        User user = new User(uid, name, email, "", "");
        userRef.document(uid).set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mProgress.setVisibility(View.GONE);
                toMainActivity();
            } else {
                mProgress.setVisibility(View.GONE);
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            mProgress.setVisibility(View.GONE);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void toLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        mLogin = findViewById(R.id.txtLogin);
        mRegister = findViewById(R.id.btnRegister);
        mName = findViewById(R.id.regName);
        mEmail = findViewById(R.id.regEmail);
        mPassword = findViewById(R.id.regPassword);
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        mProgress = findViewById(R.id.regProgress);
    }
}
