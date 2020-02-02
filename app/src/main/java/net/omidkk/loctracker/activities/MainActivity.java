package net.omidkk.loctracker.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import net.omidkk.loctracker.model.User;
import net.omidkk.loctracker.utils.Constants;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // exit app when back button pressed
        if (getIntent().getBooleanExtra(Constants.KEY_EXIT, false)) {
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //checks if user is logged in to app or not. if not opens login page
        GoogleSignInAccount alreadyLoggedAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (alreadyLoggedAccount != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            User.setUser(alreadyLoggedAccount.getDisplayName(), alreadyLoggedAccount.getPhotoUrl());
            startActivity(intent);

        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);

        }
    }
}
