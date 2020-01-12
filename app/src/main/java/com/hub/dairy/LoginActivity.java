package com.hub.dairy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextView mReg;
    private EditText mEmail, mPassword;
    private TextInputLayout txtEmail, txtPassword;
    private Button mLogin;
    private ProgressBar mProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        initViews();

        mReg.setOnClickListener(v -> toRegister());

        mLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (!email.isEmpty()) {
            if (!password.isEmpty()) {
                doLogin(email, password);
            } else {
                txtPassword.setError("Please enter your password");
            }
        } else {
            txtEmail.setError("Please enter your valid email");
        }
    }

    private void doLogin(String email, String password) {
        mProgress.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
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

    private void toRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private void initViews() {
        mReg = findViewById(R.id.txtRegister);
        mLogin = findViewById(R.id.btnLogin);
        mEmail = findViewById(R.id.loginEmail);
        mPassword = findViewById(R.id.loginPassword);
        mProgress = findViewById(R.id.loginProgress);
        txtEmail = findViewById(R.id.textEmail);
        txtPassword = findViewById(R.id.textPassword);
    }
}
