package com.example.przemek.fedi;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    static int SPLASH_TIMEOUT = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideNotifAndTitleBars();
        setContentView(R.layout.activity_home);

        LoadMainMenu();

    }

    void HideNotifAndTitleBars(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("FEDI");
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }

    }

    @Override
    public void onBackPressed() {
        return;
    }
    /***
     * This method is used to finish current intent and start main menu activity after 1.5s.
     */
    void LoadMainMenu(){
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                Intent homeIntent = new Intent(HomeActivity.this, MainMenu.class);
                startActivity(homeIntent);
                finish();
            }
        },SPLASH_TIMEOUT);
    }
}
