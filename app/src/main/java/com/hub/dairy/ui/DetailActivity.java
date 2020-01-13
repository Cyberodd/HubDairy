package com.hub.dairy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hub.dairy.R;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.MilkProduce;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hub.dairy.helpers.Constants.DATE_FORMAT;
import static com.hub.dairy.helpers.Constants.MILK_PRODUCE;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private Toolbar mToolbar;
    private Animal mAnimal;
    private String animalId, animalName, userId, date;
    private TextInputLayout txtQuantity;
    private TextView mQuantity, name, gender, location, regDate, breed, category, status;
    private CircleImageView image;
    private CardView mIsFemale;
    private Button mSubmit;
    private CollectionReference milkRef;
    private ProgressBar mProgress;
    private int produceCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        date = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(new Date());
        if (user != null) {
            userId = user.getUid();
            milkRef = database.collection(MILK_PRODUCE).document(userId).collection(MILK_PRODUCE);
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        Intent intent = getIntent();
        mAnimal = intent.getParcelableExtra("animal");
        if (mAnimal != null) {
            animalId = mAnimal.getId();
            animalName = mAnimal.getName();
        } else {
            Log.d(TAG, "onCreate: No animal passed");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(animalName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayAnimalInfo();

        getMilkProduceEntries();

        mSubmit.setOnClickListener(v -> submitQuantity());
    }

    private void getMilkProduceEntries() {
        milkRef.document(animalId).collection(date)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                produceCount = queryDocumentSnapshots.size();
            } else {
                produceCount = 0;
            }
        });
    }

    private void submitQuantity() {
        String quantity = mQuantity.getText().toString().trim();
        if (!quantity.isEmpty()) {
            diSubmit(quantity);
        } else {
            txtQuantity.setError("Please input quantity of milk produced first");
        }
    }

    private void diSubmit(String quantity) {
        mProgress.setVisibility(View.VISIBLE);
        String milkProdId = milkRef.document().getId();
        MilkProduce milkProduce = new MilkProduce(userId, animalId, animalName, quantity, date);
        if (produceCount < 2){
            milkRef.document(animalId)
                    .collection(date)
                    .document(milkProdId).set(milkProduce).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mProgress.setVisibility(View.GONE);
                    mQuantity.setText("");
                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                } else {
                    mProgress.setVisibility(View.GONE);
                    mQuantity.setText("");
                    Toast.makeText(this, "An error occurred",
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                mProgress.setVisibility(View.GONE);
                mQuantity.setText("");
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            mProgress.setVisibility(View.GONE);
            mQuantity.setText("");
            Toast.makeText(this, "Can't enter more than 2 entries on the same day",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void displayAnimalInfo() {
        name.setText(animalName);
        gender.setText(mAnimal.getGender());
        location.setText(mAnimal.getLocation());
        regDate.setText(mAnimal.getRegDate());
        breed.setText(mAnimal.getBreed());
        category.setText(mAnimal.getCategory());
        status.setText(mAnimal.getStatus());

        Glide.with(this).load(mAnimal.getImageUrl())
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error_photo)
                .into(image);
        if (mAnimal.getGender().equals("Female")) {
            mIsFemale.setVisibility(View.VISIBLE);
        } else {
            mIsFemale.setVisibility(View.GONE);
        }
    }

    private void initViews() {
        mToolbar = findViewById(R.id.animalToolbar);
        name = findViewById(R.id.animal_name);
        gender = findViewById(R.id.animal_gender);
        location = findViewById(R.id.animal_location);
        regDate = findViewById(R.id.animal_reg_date);
        image = findViewById(R.id.animal_image);
        breed = findViewById(R.id.animal_breed);
        category = findViewById(R.id.animal_category);
        status = findViewById(R.id.animal_status);
        mIsFemale = findViewById(R.id.isFemale);
        mSubmit = findViewById(R.id.btnSubmit);
        txtQuantity = findViewById(R.id.textQuantity);
        mQuantity = findViewById(R.id.quantity);
        mProgress = findViewById(R.id.produceProgress);
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
