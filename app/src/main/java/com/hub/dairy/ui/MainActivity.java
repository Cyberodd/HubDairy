
package com.hub.dairy.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.hub.dairy.R;
import com.hub.dairy.adapters.TabPagerAdapter;
import com.hub.dairy.dialogs.MeatDialog;
import com.hub.dairy.dialogs.MilkDialog;
import com.hub.dairy.dialogs.TransactionDialog;
import com.hub.dairy.fragments.AnimalFragment;
import com.hub.dairy.fragments.TransactionFragment;
import com.hub.dairy.helpers.TransactionEvent;
import com.hub.dairy.models.MilkProduce;
import com.hub.dairy.models.Transaction;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.hub.dairy.helpers.Constants.ANIMALS;
import static com.hub.dairy.helpers.Constants.ANIMAL_ID;
import static com.hub.dairy.helpers.Constants.DATE;
import static com.hub.dairy.helpers.Constants.MILK_PRODUCE;
import static com.hub.dairy.helpers.Constants.TIME;
import static com.hub.dairy.helpers.Constants.TRANSACTIONS;
import static com.hub.dairy.helpers.Constants.USER_ID;

public class MainActivity extends AppCompatActivity implements TransactionDialog.TransInterface,
        MilkDialog.MilkInterface, MeatDialog.MeatInterface {

    private static final String TAG = "MainActivity";
    private Toolbar mToolbar;
    private ViewPager mPager;
    private TabLayout mTabLayout;
    private FloatingActionButton mFab, mAddAnimal, mAddTransaction;
    private boolean isFabOpen = true;
    private TextView txtAddAnimal, txtTransaction;
    private FirebaseUser mUser;
    private FirebaseFirestore mDatabase;
    private String userId;
    private CollectionReference transRef, milkRef;
    private MilkProduce mMilkProduce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        mDatabase = FirebaseFirestore.getInstance();
        transRef = database.collection(TRANSACTIONS);
        milkRef = mDatabase.collection(MILK_PRODUCE);
        if (mUser != null) {
            userId = mUser.getUid();
        } else {
            Log.d(TAG, "onActivityCreated: User not logged in");
        }

        initViews();

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Dairy");

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pagerAdapter.addFragments(new AnimalFragment(), "Animals");
        pagerAdapter.addFragments(new TransactionFragment(), "Transactions");
        mPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mPager);

        mFab.setOnClickListener(v -> displayAnimations());

        mAddAnimal.setOnClickListener(v -> navigateToAddAnimal());

        mAddTransaction.setOnClickListener(v -> toTransaction());
    }

    private void listenForChanges() {
        Query query = transRef.whereEqualTo(USER_ID, userId)
                .orderBy(TIME, Query.Direction.DESCENDING);
        query.addSnapshotListener(this, (queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.d(TAG, "listenForChanges: Error " + e);
                return;
            }
            List<Transaction> transactions = new ArrayList<>();
            if (queryDocumentSnapshots != null) {
                for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                    Transaction transaction = snapshot.toObject(Transaction.class);
                    transactions.add(transaction);
                }
                emitTransactions(transactions);
            } else {
                Log.d(TAG, "listenForChanges: Something went wrong");
            }
        });
    }

    private void toTransaction() {
        TransactionDialog dialog = new TransactionDialog();
        dialog.show(getSupportFragmentManager(), "TransactionDialog");
        closeFab();
    }

    private void navigateToAddAnimal() {
        Intent intent = new Intent(this, AnimalActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        closeFab();
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
        mAddTransaction.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        txtAddAnimal.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        txtTransaction.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        txtAddAnimal.setVisibility(View.VISIBLE);
        txtTransaction.setVisibility(View.VISIBLE);
    }

    private void closeFab() {
        isFabOpen = true;
        mAddAnimal.animate().translationY(0.0F);
        txtAddAnimal.animate().translationY(0.0F);
        mAddTransaction.animate().translationY(0.0F);
        txtTransaction.animate().translationY(0.0F);
        txtAddAnimal.setVisibility(View.GONE);
        txtTransaction.setVisibility(View.GONE);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.homeToolbar);
        mPager = findViewById(R.id.homePager);
        mTabLayout = findViewById(R.id.mainTabLayout);
        mFab = findViewById(R.id.btnFab);
        mAddAnimal = findViewById(R.id.btnAddAnimal);
        txtAddAnimal = findViewById(R.id.txtAddAnimal);
        mAddTransaction = findViewById(R.id.btnAddTransaction);
        txtTransaction = findViewById(R.id.txtAddTransaction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reports:
                toReports();
                return true;
            case R.id.action_profile:
                toProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toReports() {
        Intent intent = new Intent(this, ReportsActivity.class);
        startActivity(intent);
    }

    private void toProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            toLoginActivity();
        } else {
            listenForChanges();
        }
    }

    private void toLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void emitTransactions(List<Transaction> transactions) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactions(transactions);
        EventBus.getDefault().post(event);
    }

    @Override
    public void isSuccess(String animalId) {
        removeAnimalFromDb(animalId);
    }

    private void removeAnimalFromDb(String animalId) {
        if (mUser != null) {
            CollectionReference animalRef = mDatabase.collection(ANIMALS);
            animalRef.document(animalId).delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Produce added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "An error occurred", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d(TAG, "removeAnimalFromDb: User not logged in");
        }
    }

    @Override
    public void notifyMilkSale(String animalId, String date, String quantity, float remQty,
                               float currQty, String produceId) {
        float updatedQty = remQty - currQty;
        Map<String, Object> map = new HashMap<>();
        map.put("remQty", String.format(Locale.ENGLISH, "%.2f", updatedQty));
        milkRef.document(produceId)
                .set(map, SetOptions.merge())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Milk transaction added",
                                Toast.LENGTH_SHORT).show());
    }

    @Override
    public void notifyAnimalSale(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyInput() {
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void notifyUpdateInput(String quantity, String date, String animalId) {
        Query query = milkRef.whereEqualTo(ANIMAL_ID, animalId).whereEqualTo(DATE, date);
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                mMilkProduce = snapshot.toObject(MilkProduce.class);
            }
            getResponse(mMilkProduce, quantity);
        });
    }

    private void getResponse(MilkProduce milkProduce, String quantity) {
        if (milkProduce != null) {
            String remQty = milkProduce.getRemQty();
            String prodId = milkProduce.getProduceId();
            checkRemQty(remQty, prodId, quantity);
        } else {
            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRemQty(String remQty, String prodId, String quantity) {
        float finalQty = Float.parseFloat(remQty) + Float.parseFloat(quantity);
        String fnQty = String.format(Locale.ENGLISH, "%.2f", finalQty);
        if (remQty.equals("0.00")) {
            Map<String, Object> map = new HashMap<>();
            map.put("remQty", quantity);
            milkRef.document(prodId).set(map, SetOptions.merge()).addOnSuccessListener(aVoid ->
                    showToast());
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("remQty", fnQty);
            milkRef.document(prodId).set(map, SetOptions.merge()).addOnSuccessListener(aVoid ->
                    showToast());
        }
    }

    private void showToast() {
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
    }
}
