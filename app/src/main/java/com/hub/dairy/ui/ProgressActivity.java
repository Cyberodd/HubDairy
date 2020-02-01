package com.hub.dairy.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hub.dairy.R;
import com.hub.dairy.adapters.ReportAdapter;
import com.hub.dairy.models.Animal;
import com.hub.dairy.models.MilkProduce;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMAL_ID;
import static com.hub.dairy.helpers.Constants.DATE;
import static com.hub.dairy.helpers.Constants.MILK_PRODUCE;
import static com.hub.dairy.helpers.Constants.SHORT_DATE;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class ProgressActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "ProgressActivity";
    private Toolbar mToolbar;
    private List<MilkProduce> mMilkProduces;
    private RecyclerView progressRecycler;
    private EditText searchDate;
    private String userId, animalId;
    private ProgressBar reportProgress;
    private CollectionReference milkRef;
    private TextView noReportInfo;
    private TextInputLayout textReportDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        milkRef = database.collection(MILK_PRODUCE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        Intent intent = getIntent();
        Animal animal = intent.getParcelableExtra("animal");
        if (animal != null) {
            animalId = animal.getAnimalId();
        } else {
            Log.d(TAG, "onCreate: Animal not passed");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Animal Progress");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchDate.setOnClickListener(v -> openCalender());

        loadAllReports();
    }

    private void loadAllReports() {
        Query query = milkRef.whereEqualTo(ANIMAL_ID, animalId)
                .orderBy(DATE, Query.Direction.DESCENDING);
        getResponse(query);
    }

    private void getResponse(Query query) {
        mMilkProduces = new ArrayList<>();
        reportProgress.setVisibility(View.VISIBLE);
        query.get().addOnSuccessListener(queryDocumentSnapshots ->
                loadView(
                        queryDocumentSnapshots.toObjects(MilkProduce.class))
        ).addOnFailureListener(e -> {
            reportProgress.setVisibility(View.GONE);
            Log.d(TAG, "generateProgressReport: Error " + e);
        });
    }

    private void loadView(List<MilkProduce> toObjects) {
        reportProgress.setVisibility(View.GONE);
        progressRecycler.setHasFixedSize(true);
        progressRecycler.setLayoutManager(new LinearLayoutManager(this));

        mMilkProduces = new ArrayList<>();
        mMilkProduces.addAll(toObjects);

        if (!mMilkProduces.isEmpty()) {
            progressRecycler.setVisibility(View.VISIBLE);
            noReportInfo.setVisibility(View.GONE);
            textReportDate.setError("");
            reportProgress.setVisibility(View.GONE);
            ReportAdapter reportAdapter = new ReportAdapter(mMilkProduces);
            progressRecycler.setAdapter(reportAdapter);
            reportAdapter.notifyDataSetChanged();
        } else {
            progressRecycler.setVisibility(View.GONE);
            reportProgress.setVisibility(View.GONE);
            textReportDate.setError("No record found");
            noReportInfo.setVisibility(View.VISIBLE);
        }
    }

    private void openCalender() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void initViews() {
        mToolbar = findViewById(R.id.reportToolbar);
        searchDate = findViewById(R.id.progressReportDate);
        progressRecycler = findViewById(R.id.progressReportRv);
        reportProgress = findViewById(R.id.reportProgress);
        textReportDate = findViewById(R.id.textReportDate);
        noReportInfo = findViewById(R.id.noReportInfo);
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
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        SimpleDateFormat dateFormat = new SimpleDateFormat(SHORT_DATE, Locale.ENGLISH);

        String newDate = dateFormat.format(calendar.getTime());
        searchDate.setText(newDate);
        String dateParam = searchDate.getText().toString();

        Query query = milkRef.whereEqualTo(DATE, dateParam).whereEqualTo(ANIMAL_ID, animalId)
                .whereEqualTo(USER_ID, userId);
        getResponse(query);
    }
}
