package com.hub.dairy.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.hub.dairy.R;
import com.hub.dairy.models.Report;
import com.hub.dairy.models.Transaction;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.hub.dairy.helpers.Constants.ANIMAL_TYPE;
import static com.hub.dairy.helpers.Constants.DATE;
import static com.hub.dairy.helpers.Constants.MILK_TYPE;
import static com.hub.dairy.helpers.Constants.REPORTS;
import static com.hub.dairy.helpers.Constants.SHORT_DATE;
import static com.hub.dairy.helpers.Constants.TRANSACTIONS;
import static com.hub.dairy.helpers.Constants.TYPE;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class ReportsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "ReportsActivity";
    private Toolbar mToolbar;
    private String userId, reportId;
    private TextInputLayout txtSearchDate;
    private AutoCompleteTextView inputDate;
    private CollectionReference transRef, reportRef;
    private int milkSize, totalTransactions, animalCount;
    private float totalSales;
    private List<Report> mReports;
    private TextView reportDate, milkSales, animalSales, total, totalAmount, noContent;
    private LinearLayout showReport;
    private Query mQuery;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        transRef = database.collection(TRANSACTIONS);
        reportRef = database.collection(REPORTS);
        reportId = reportRef.document().getId();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Log.d(TAG, "onCreate: User not logged in");
        }

        initViews();

        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Report");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        inputDate.setOnClickListener(v -> openCalender());
    }

    private void openCalender() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        dialog.setCancelable(false);
        dialog.show();
    }

    private void initViews() {
        mToolbar = findViewById(R.id.reportsToolbar);
        txtSearchDate = findViewById(R.id.textSearchDate);
        inputDate = findViewById(R.id.search_date);
        totalAmount = findViewById(R.id.totalAmount);
        total = findViewById(R.id.totalTransactions);
        animalSales = findViewById(R.id.animalSales);
        milkSales = findViewById(R.id.milkSales);
        reportDate = findViewById(R.id.reportDate);
        showReport = findViewById(R.id.showReport);
        noContent = findViewById(R.id.no_content);
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
        inputDate.setText(newDate);
        String dateParam = inputDate.getText().toString();

        mQuery = reportRef.whereEqualTo(DATE, dateParam).whereEqualTo(USER_ID, userId).limit(1);

        Query transQuery = transRef.whereEqualTo(DATE, dateParam).whereEqualTo(USER_ID, userId);
        transQuery.get().addOnSuccessListener(this::loadTransactions);

        Query milkCount = transRef.whereEqualTo(DATE, dateParam).whereEqualTo(TYPE, MILK_TYPE)
                .whereEqualTo(USER_ID, userId);
        milkCount.get().addOnSuccessListener(this::getMilkTypeCount);

        Query animalCount = transRef.whereEqualTo(DATE, dateParam).whereEqualTo(TYPE, ANIMAL_TYPE)
                .whereEqualTo(USER_ID, userId);
        animalCount.get().addOnSuccessListener(this::getAnimalSaleCount);
    }

    private void getAnimalSaleCount(QuerySnapshot queryDocumentSnapshots) {
        if (!queryDocumentSnapshots.isEmpty()) {
            getAnimalCount(queryDocumentSnapshots.toObjects(Transaction.class));
        } else {
            Log.d(TAG, "getAnimalSaleCount: Size is zero");
        }
    }

    private void getAnimalCount(List<Transaction> toObjects) {
        animalCount = toObjects.size();
    }

    private void getMilkTypeCount(QuerySnapshot queryDocumentSnapshots) {
        if (!queryDocumentSnapshots.isEmpty()) {
            getSize(queryDocumentSnapshots.toObjects(Transaction.class));
        } else {
            Log.d(TAG, "getMilkTypeCount: Size is zero");
        }
    }

    private void getSize(List<Transaction> toObjects) {
        milkSize = toObjects.size();
    }

    private void loadTransactions(QuerySnapshot queryDocumentSnapshots) {
        if (!queryDocumentSnapshots.isEmpty()) {
            txtSearchDate.setError("");
            List<Transaction> transactions = queryDocumentSnapshots.toObjects(Transaction.class);
            mapTransactions(transactions);
            noContent.setVisibility(View.GONE);
        } else {
            txtSearchDate.setError(getString(R.string.no_record));
            noContent.setVisibility(View.VISIBLE);
            noContent.setText(R.string.no_record);
            showReport.setVisibility(View.GONE);
        }
    }

    private void mapTransactions(List<Transaction> transactions) {
        totalTransactions = transactions.size();
        List<Transaction> transactionList = new ArrayList<>(transactions);
        List<Float> floatList = new ArrayList<>();
        for (int i = 0; i < transactionList.size(); i++) {
            String cash = transactionList.get(i).getCash();
            float f = Float.parseFloat(cash);
            floatList.add(f);
        }
        sumFloats(floatList);
    }

    private void sumFloats(List<Float> floatList) {
        totalSales = 0;
        for (int i = 0; i < floatList.size(); i++) {
            totalSales += floatList.get(i);
        }
        generateReport();
    }

    private void generateReport() {
        mReports = new ArrayList<>();
        String date = inputDate.getText().toString();
        Query query = reportRef.whereEqualTo(DATE, date).whereEqualTo(USER_ID, userId);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                mReports.addAll(queryDocumentSnapshots.toObjects(Report.class));
                populateViews(mReports);
            } else {
                generateNewReport(date);
            }
        });
    }

    private void generateNewReport(String date) {
        showReport.setVisibility(View.GONE);
        Report report = new Report(reportId, milkSize, animalCount, totalSales,
                totalTransactions, date, userId);
        reportRef.document(reportId).set(report).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                openDialog(date);
            } else {
                Toast.makeText(this, R.string.report_error, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Log.d(TAG, "generateReport: Error " + e));
    }

    private void openDialog(String date) {
        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.report_dialog);
        initDialogViews(mDialog, date);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    private void initDialogViews(Dialog dialog, String date) {
        TextView proceed = dialog.findViewById(R.id.txtProceed);
        TextView cancel = dialog.findViewById(R.id.txtCancel);
        TextView info = dialog.findViewById(R.id.txtReportInfo);

        info.setText(MessageFormat.format("{0} {1}", date, getString(R.string.info_msg)));

        proceed.setOnClickListener(v -> showReport());
        cancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showReport() {
        mReports = new ArrayList<>();
        mQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()){
                mReports.addAll(queryDocumentSnapshots.toObjects(Report.class));
                mDialog.dismiss();
                populateViews(mReports);
            } else {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Log.d(TAG, "showReport: Error " + e));
    }

    private void populateViews(List<Report> reports) {
        showReport.setVisibility(View.VISIBLE);
        Report report = reports.get(0);
        reportDate.setText(report.getDate());
        milkSales.setText(String.valueOf(report.getMilkSales()));
        animalSales.setText(String.valueOf(report.getAnimalSales()));
        total.setText(String.valueOf(report.getTotalTransactions()));
        totalAmount.setText(String.valueOf(report.getTotalCash()));
    }
}
