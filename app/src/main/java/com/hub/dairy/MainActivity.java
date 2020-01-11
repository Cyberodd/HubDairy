
package com.hub.dairy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.hub.dairy.adapters.TabPagerAdapter;
import com.hub.dairy.fragments.AnimalFragment;
import com.hub.dairy.fragments.ReportsFragment;
import com.hub.dairy.fragments.TransactionFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mPager;
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Dairy");

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        pagerAdapter.addFragments(new AnimalFragment(), "Animals");
        pagerAdapter.addFragments(new TransactionFragment(), "Transactions");
        pagerAdapter.addFragments(new ReportsFragment(), "Reports");
        mPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mPager);
    }

    private void initViews() {
        mToolbar = findViewById(R.id.homeToolbar);
        mPager = findViewById(R.id.homePager);
        mTabLayout = findViewById(R.id.mainTabLayout);
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
            case R.id.action_log_out:
                Toast.makeText(this, "Signing out", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_search:
                Toast.makeText(this, "Searching", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
