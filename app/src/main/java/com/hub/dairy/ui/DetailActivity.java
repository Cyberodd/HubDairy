package com.hub.dairy.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.hub.dairy.R;
import com.hub.dairy.fragments.MeatDialog;
import com.hub.dairy.fragments.MilkDialog;
import com.hub.dairy.models.Animal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.MALE;

public class DetailActivity extends AppCompatActivity implements MilkDialog.MilkInterface,
        MeatDialog.MeatInterface {

    private static final String TAG = "DetailActivity";
    private Toolbar mToolbar;
    private Animal mAnimal;
    private String animalName, animalId;
    private TextView availability, update;
    private EditText name, gender, location, regDate, breed, category, status;
    private CircleImageView image;
    private Button mShowDialog;
    private FirebaseFirestore mDatabase;
    private FirebaseUser mUser;
    private RelativeLayout addProduce;
    private CollectionReference animalRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDatabase = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();
        animalRef = mDatabase.collection(ANIMALS);

        Intent intent = getIntent();
        mAnimal = intent.getParcelableExtra("animal");
        if (mAnimal != null) {
            animalName = mAnimal.getAnimalName();
            animalId = mAnimal.getAnimalId();
        } else {
            Log.d(TAG, "onCreate: No animal passed");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(animalName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mShowDialog.setOnClickListener(v -> openDialog());

        update.setOnClickListener(v -> enableFields());

        displayAnimalInfo();
    }

    private void enableFields() {
        name.setEnabled(true);
        location.setEnabled(true);
        breed.setEnabled(true);
    }

    private void displayAnimalInfo() {
        name.setText(animalName);
        gender.setText(mAnimal.getGender());
        location.setText(mAnimal.getLocation());
        regDate.setText(mAnimal.getRegDate());
        breed.setText(mAnimal.getAnimalBreed());
        category.setText(mAnimal.getCategory());
        status.setText(mAnimal.getStatus());
        availability.setText(mAnimal.getAvailability());

        if (mAnimal.getAvailability().equals("sold")) {
            addProduce.setVisibility(View.GONE);
        } else {
            addProduce.setVisibility(View.VISIBLE);
        }

        Glide.with(this).load(mAnimal.getImageUrl())
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error_photo)
                .into(image);
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
        mShowDialog = findViewById(R.id.btnShowDialog);
        availability = findViewById(R.id.availability);
        addProduce = findViewById(R.id.addProduce);
        update = findViewById(R.id.updateAnimalInfo);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            updateInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateInfo() {
        if (!name.isEnabled() || !location.isEnabled() || !breed.isEnabled()) {
            Toast.makeText(this, R.string.unable_to_update, Toast.LENGTH_SHORT).show();
        } else {
            String updateName = name.getText().toString().trim();
            String updateLoc = location.getText().toString().trim();
            String updateBreed = breed.getText().toString().trim();

            Map<String, Object> updateInfo = new HashMap<>();
            updateInfo.put("animalName", updateName);
            updateInfo.put("location", updateLoc);
            updateInfo.put("animalBreed", updateBreed);

            animalRef.document(animalId)
                    .set(updateInfo, SetOptions.merge()).addOnSuccessListener(aVoid -> {
                name.setEnabled(false);
                location.setEnabled(false);
                breed.setEnabled(false);
                Toast.makeText(this, R.string.success_msg, Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void openDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.produce_layout);
        initDialogViews(dialog);
        dialog.show();
    }

    private void initDialogViews(Dialog dialog) {
        Button btnMeat = dialog.findViewById(R.id.btnMeat);
        Button btnMilk = dialog.findViewById(R.id.btnMilk);

        if (mAnimal.getGender().equals(MALE)) {
            btnMilk.setVisibility(View.GONE);
        } else {
            btnMilk.setVisibility(View.VISIBLE);
        }

        btnMeat.setOnClickListener(v -> openMeatDialog(dialog));

        btnMilk.setOnClickListener(v -> openMilkDialog(dialog));
    }

    private void openMilkDialog(Dialog dialog) {
        MilkDialog milkDialog = new MilkDialog();
        Bundle args = new Bundle();
        args.putParcelable("animal", mAnimal);
        milkDialog.setArguments(args);
        dialog.dismiss();
        milkDialog.show(getSupportFragmentManager(), "MilkDialog");
    }

    private void openMeatDialog(Dialog dialog) {
        MeatDialog meatDialog = new MeatDialog();
        Bundle args = new Bundle();
        args.putParcelable("animal", mAnimal);
        meatDialog.setArguments(args);
        dialog.dismiss();
        meatDialog.show(getSupportFragmentManager(), "MeatDialog");
    }

    @Override
    public void notifyInput() {
        Toast.makeText(this, "Produce added", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void isSuccess() {
        removeAnimalFromDb();
    }

    private void removeAnimalFromDb() {
        if (mUser != null) {
            CollectionReference animalRef = mDatabase.collection(ANIMALS)
                    .document(mUser.getUid()).collection(ANIMALS);
            animalRef.document(animalId).delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    toMainActivity();
                } else {
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d(TAG, "removeAnimalFromDb: User not logged in");
        }
    }

    private void toMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
