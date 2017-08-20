package com.example.przemek.fedi;

import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Przemek on 2017-05-23.
 */

public class ImageInfo extends AppCompatActivity {

    DisplayMetrics _dm;
    int _dispW, _dispH;
    TextView _imageInfoText;
    ExifInterface _exifInterface;
    Map<String, String> _exifTagMap;
    String _imgPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Informacje");
        setContentView(R.layout.imageinfolayout);

        _imageInfoText = (TextView)findViewById(R.id.imageInfoText);
        _imageInfoText.setMovementMethod(new ScrollingMovementMethod());

        FillMap();
        InitDialog();
        LoadImageInfo();

    }

    void FillMap(){
        _exifTagMap = new LinkedHashMap<String, String>();
        _exifTagMap.put(ExifInterface.TAG_DATETIME, "Data");
        _exifTagMap.put(ExifInterface.TAG_ARTIST, "Artysta");
        _exifTagMap.put(ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION, "Opis ustawień urządzenia");
        _exifTagMap.put(ExifInterface.TAG_MAKE, "Urządzenie");
        _exifTagMap.put(ExifInterface.TAG_MODEL, "Model");
        _exifTagMap.put(ExifInterface.TAG_IMAGE_LENGTH, "Długość");
        _exifTagMap.put(ExifInterface.TAG_IMAGE_WIDTH, "Szerokość");
        _exifTagMap.put(ExifInterface.TAG_ISO_SPEED_RATINGS, "ISO");
        _exifTagMap.put(ExifInterface.TAG_FOCAL_LENGTH, "Długość ogniskowej");
        _exifTagMap.put(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, "Zoom cyfrowy");
        _exifTagMap.put(ExifInterface.TAG_FLASH, "Lampa błyskowa");
        _exifTagMap.put(ExifInterface.TAG_ORIENTATION, "Orientacja");
        _exifTagMap.put(ExifInterface.TAG_BRIGHTNESS_VALUE, "Jasność");
        _exifTagMap.put(ExifInterface.TAG_CONTRAST, "Kontrast");
        _exifTagMap.put(ExifInterface.TAG_EXPOSURE_MODE, "Tryb ekspozycji");
        _exifTagMap.put(ExifInterface.TAG_APERTURE_VALUE, "Przesłona");
        _exifTagMap.put(ExifInterface.TAG_WHITE_BALANCE, "Balans bieli");
        _exifTagMap.put(ExifInterface.TAG_COLOR_SPACE, "Przestrzeń kolorów");
        _exifTagMap.put(ExifInterface.TAG_SATURATION, "Nasycenie");
        _exifTagMap.put(ExifInterface.TAG_SHARPNESS, "Ostrość");
        _exifTagMap.put(ExifInterface.TAG_COMPRESSION, "Kompresja");
        _exifTagMap.put(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, "Kompensacja ekspozycji");
        _exifTagMap.put(ExifInterface.TAG_EXPOSURE_INDEX, "Indeks ekspozycji");
        _exifTagMap.put(ExifInterface.TAG_GPS_LATITUDE, "GPS: szerokość");
        _exifTagMap.put(ExifInterface.TAG_GPS_LATITUDE_REF, "GPS: kierunek szerokości");
        _exifTagMap.put(ExifInterface.TAG_GPS_LONGITUDE, "GPS: długość ");
        _exifTagMap.put(ExifInterface.TAG_GPS_LONGITUDE_REF, "GPS: kierunek długości");
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
                _imgPath = bundle.getString("IMAGE_INFO");
                _exifInterface = new ExifInterface(_imgPath);
                GetExifData(_exifInterface);
            }
            catch(IOException e){
                e.printStackTrace();
                Toast.makeText(this, "Nie można pobrać informacji o zdjęciu!", Toast.LENGTH_LONG).show();
            }
        }
    }

    String getTagString(String tagDefinition, String tag, ExifInterface exif)
    {
        String atribVal = ((exif.getAttribute(tag)!=null) ? exif.getAttribute(tag) : "Brak danych");
        return("<b>"+tagDefinition+"</b>" + ":<br>" +atribVal + "<br><br>");
    }

    @SuppressWarnings("deprecation")
    void GetExifData(ExifInterface exif){
        String myAttribute = "";
        myAttribute += ("<b>Nazwa pliku:</b><br>"+_imgPath.substring(_imgPath.lastIndexOf("/")+1)+"<br><br>");
        myAttribute += ("<b>Ścieżka do pliku:</b><br>"+_imgPath+"<br><br>");
        for(Map.Entry<String, String> entry  : _exifTagMap.entrySet()){
            myAttribute += getTagString(entry.getValue(),entry.getKey(),exif);
        }
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
