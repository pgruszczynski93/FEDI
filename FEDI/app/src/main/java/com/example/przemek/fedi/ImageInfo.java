package com.example.przemek.fedi;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Przemek on 2017-05-23.
 */

public class ImageInfo extends AppCompatActivity {

    DisplayMetrics _dm;
    int _dispW, _dispH;
    TextView _imageInfoText;
    ExifInterface _exifInterface;
    SpannableString _spanString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageinfolayout);

        _imageInfoText = (TextView)findViewById(R.id.imageInfoText);
        _imageInfoText.setMovementMethod(new ScrollingMovementMethod());

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
        Intent imageInfoIntent = getIntent();
        Bundle bundle = imageInfoIntent.getExtras();

        if(bundle!=null){
            try{
                _exifInterface = new ExifInterface(bundle.getString("IMAGE_INFO"));
                GetExifData(_exifInterface);
            }
            catch(IOException e){
                e.printStackTrace();
                Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show();
            }
        }
    }

    String getTagString(String tag, ExifInterface exif)
    {
        return("<b>"+tag+"</b>" + " : " + exif.getAttribute(tag) + "<br>");
    }

    @SuppressWarnings("deprecation")
    void GetExifData(ExifInterface exif){
        String myAttribute = "<b>Exif information</b><br>";
        myAttribute += getTagString(ExifInterface.TAG_DATETIME, exif);
        myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
        myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        myAttribute += getTagString(ExifInterface.TAG_APERTURE_VALUE, exif);
        myAttribute += getTagString(ExifInterface.TAG_ARTIST, exif);
        myAttribute += getTagString(ExifInterface.TAG_BRIGHTNESS_VALUE, exif);
        myAttribute += getTagString(ExifInterface.TAG_COLOR_SPACE, exif);
        myAttribute += getTagString(ExifInterface.TAG_COMPRESSION, exif);
        myAttribute += getTagString(ExifInterface.TAG_CONTRAST, exif);
        myAttribute += getTagString(ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION, exif);
        myAttribute += getTagString(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, exif);
        myAttribute += getTagString(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, exif);
        myAttribute += getTagString(ExifInterface.TAG_EXPOSURE_INDEX, exif);
        myAttribute += getTagString(ExifInterface.TAG_EXPOSURE_MODE, exif);
        myAttribute += getTagString(ExifInterface.TAG_FOCAL_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_ISO_SPEED_RATINGS, exif);
        myAttribute += getTagString(ExifInterface.TAG_SATURATION, exif);
        myAttribute += getTagString(ExifInterface.TAG_SHARPNESS, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            _imageInfoText.setText(Html.fromHtml(myAttribute,Html.FROM_HTML_MODE_COMPACT));
        }
        else {
            _imageInfoText.setText(Html.fromHtml(myAttribute));
        }
    }

    public void CloseInfo(View v){
        finish();
    }
}
