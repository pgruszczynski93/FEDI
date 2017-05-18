package com.example.przemek.fedi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Info extends AppCompatActivity {

    Button _okInfoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        _okInfoButton = (Button)findViewById(R.id.OkInfoButton);
    }

    public void GoToMenu(View view){
        startActivity(new Intent(getApplicationContext(), MainMenu.class));
        finish();
    }
}
