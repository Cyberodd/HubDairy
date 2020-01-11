package com.hub.dairy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.hub.dairy.models.Animal;

import java.util.Objects;

public class AnimalDetailActivity extends AppCompatActivity {

    private static final String TAG = "AnimalDetailActivity";
    private Toolbar mToolbar;
    private Animal mAnimal;
    private String animalName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animal_detail);

        Intent intent = getIntent();
        mAnimal = intent.getParcelableExtra("animal");
        if (mAnimal != null){
            animalName = mAnimal.getName();
        } else {
            Log.d(TAG, "onCreate: No animal passed");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(animalName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.animalToolbar);
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
