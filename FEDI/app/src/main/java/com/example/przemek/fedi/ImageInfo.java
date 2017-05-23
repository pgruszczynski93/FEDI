package com.example.przemek.fedi;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by Przemek on 2017-05-23.
 */

public class ImageInfo extends AppCompatActivity {

    DisplayMetrics _dm;
    int _dispW, _dispH;
    TextView _imageInfoText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageinfolayout);

        _imageInfoText = (TextView)findViewById(R.id.imageInfoText);

        InitDialog();
        LoadImageInfo();

    }

    void InitDialog(){
        _dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(_dm);

        _dispW = _dm.widthPixels;
        _dispH = _dm.heightPixels;

        getWindow().setLayout((int)(_dispW * .9), (int)(_dispH*.9));
    }

    void LoadImageInfo(){
        String s =";";
        for(int x = 0; x < 100; x++){
            s+=String.valueOf(x)+"\n";
        }

        _imageInfoText.setMovementMethod(new ScrollingMovementMethod());
        _imageInfoText.setText(s);
    }

    public void CloseInfo(View v){
        finish();
    }
}
