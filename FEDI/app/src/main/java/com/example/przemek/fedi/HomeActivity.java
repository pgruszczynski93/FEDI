package com.example.przemek.fedi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    static int SPLASH_TIMEOUT = 2000;
    static final int EXTERNAL_STORAGE_RESULT = 1;

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideNotifAndTitleBars();
        setContentView(R.layout.activity_home);
        AskPermissions();
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

    void AskPermissions(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            LoadMainMenu();
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this,"Czy chcesz zmodyfikować zawartość Karty Pamięci?", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_RESULT);
            }
        }
    }

    /***
     * Metoda sprawdzająca czy aplikacja posiada odpowiednie uprawnienia w celu przeprowadzenia działań.
     * @param requestCode Kod zapytania.
     * @param permissions Pozwolenia przyznane aplikacji.
     * @param grantResult Status uzyskania.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult){
        if(requestCode == EXTERNAL_STORAGE_RESULT){
            if(grantResult[0] == PackageManager.PERMISSION_GRANTED){
                LoadMainMenu();
            }
            else{
                finishAffinity();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        }

    }
}
