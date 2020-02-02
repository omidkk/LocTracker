package net.omidkk.loctracker.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.omidkk.loctracker.R;
import net.omidkk.loctracker.fragments.HomeFragment;
import net.omidkk.loctracker.fragments.ProfileFragment;
import net.omidkk.loctracker.fragments.SettingsFragment;
import net.omidkk.loctracker.utils.Constants;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private boolean backButtonPressedOnce = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initViews();


        setupSettings();

        checkLocationPermission();

        setBottomNavigationMenu();

        HomeFragment homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_fragments, homeFragment)
                .commit();
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    //sets maximum distance settings in firstRun run
    private void setupSettings() {
        SharedPreferences preferences = getSharedPreferences(Constants.KEY_SETTINGS_PREFERENCES, MODE_PRIVATE);
        if (preferences.getInt(Constants.KEY_DISTANCE, 0) == 0) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(Constants.KEY_DISTANCE, Constants.DEFAULT_VALUE_DISTANCE);
            editor.apply();
        }
    }

    private void setBottomNavigationMenu() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_profile:
                        ProfileFragment profileFragment = new ProfileFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame_fragments, profileFragment)
                                .commit();
                        break;

                    case R.id.action_home:
                        HomeFragment homeFragment = new HomeFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame_fragments, homeFragment, "home")
                                .commit();
                        break;

                    case R.id.action_settings:
                        SettingsFragment settingsFragment = new SettingsFragment();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.frame_fragments, settingsFragment)
                                .commit();
                        break;
                }
                return true;
            }
        });
        bottomNavigationView.getMenu().getItem(1).setChecked(true);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1234);
        }
    }

    //exit app on double back button pressed
    @Override
    public void onBackPressed() {
        if (backButtonPressedOnce) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Constants.KEY_EXIT, true);
                startActivity(intent);
        }

        this.backButtonPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                backButtonPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1234: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HomeFragment homeFragment = new HomeFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.frame_fragments, homeFragment, "home")
                            .commit();
                } else {

                    onBackPressed();

                }
                return;
            }
        }
    }
}
