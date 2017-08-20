package com.example.przemek.fedi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;

public class Info extends AppCompatActivity {


    DisplayMetrics _dm;
    int _dispW, _dispH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menuinfo);

        getSupportActionBar().setTitle("O aplikacji");
        setContentView(R.layout.imageinfolayout);

        InitDialog();

    }

    public void ShowMainMenu(View view){
        finish();
    }

    void InitDialog(){
        _dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(_dm);

        _dispW = _dm.widthPixels;
        _dispH = _dm.heightPixels;

        getWindow().setLayout((int)(_dispW * .9), (int)(_dispH*.9));
    }
}
