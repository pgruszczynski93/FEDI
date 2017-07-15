package com.example.przemek.fedi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlend;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

/***
 * Klasa aktywności Edytora zdjęć.
 */
public class Editor extends AppCompatActivity {

    static final int REQUEST_CODE = 0, READ_URI_PERMISSION = 1;
    final int ADJUSTMENT_COUNT = 6, DETAILS_COUNT = 2, FILTERS_COUNT = 12, WHITE_BALANCE_COUNT = 2, ROTATIONS_COUNT = 3;
    final String[] _adjustmentValues = {"Jasność", "Kontrast", "Nasycenie","Prześwietlenia", "Cienie", "Temperatura"};
    final String[] _detailsValues = {"Struktura", "Wyostrzanie"};
    final String[] _filtersValues = {"Negatyw", "Szarość - Średnia", "Szarość - YUV", "Rozmycie", "Bloom",
            "Sepia", "Progowanie", "Atmosfera", "Ogień", "Lód", "Woda", "Ziemia"};
    final String[] _whiteBalanceValues = {"Temperatura - Kelvin", "Odcień"};
    final String[] _rotationValues = {"Kąt", "90 w lewo", "90 w prawo"};

//    FediCore _coreOperation;
    final int NUM_BITMAPS = 2;
    int mCurrentBitmap = 0;
    Bitmap[] _bitmapsOut;
    Allocation _inAllocation;
    Allocation[] _outAllocations;

    //**************** przemyslec
    ScriptC_brightness _rsBrightness;
    ScriptC_saturation _rsSaturation;
    ScriptC_contrast _rsContrast;
    ScriptC_grayscale_average _rsGSAverage;
    ScriptC_grayscale_yuv _rsGSyuv;
    ScriptC_invert _rsInvert;
    ScriptC_sepia _rsSepia;
    ScriptC_treshold _rsTreshold;
    ScriptIntrinsicBlur _rsBlur;
    ScriptIntrinsicBlend _rsBlend;
    ScriptC_bloom _rsBloom;
    ScriptC_tint _rsTint;
    ScriptC_simple_temperature _rsSimpleTemp;
    ScriptC_light_manager _rsLightManager;
    ScriptC_kelvin_temperature _rsKelvinTemp;
    ScriptC_atmosphere_filter _rsAtmosphere;
    ScriptC_fire_filter _rsFire;
    ScriptC_ice_filter _rsIce;
    ScriptC_water_filter _rsWater;
    ScriptC_earth_filter _rsEarth;
    //**************************
    RenderScriptTask _currentTask;

    Bitmap _inputBitmap, _resultBitmap;


    //tablice: dopasowań, detali, filtrów, balansu bieli
    Button[] _adjustmentsButtonsList, _detailsButtonList, _filtersButtonList, _wbButtonList, _rotationButtonList;
    //stringi: obecnie wcisniety, poprzednio wcisniety (przycisk), etykieta przy sliderze
    String _currBottomButton, _prevBottomButton = "", _optionsLabel;

    int _valFromSlider;
    LinearLayout _optionsLayout, _sliderOptLayout;
    TextView _optSliderText;
    SeekBar _optSlider;
    // okno alertow
    AlertDialog.Builder _builder;

    // widoki zdjecia
    //ImageView _imageView;
    public ZoomPinchImageView _zoomPinchImageView;

    Intent _launchedIntent;

    Uri _imageUri = null;

    boolean _intentHasExtras;
    //Bitmap _imageBitmap;


    private class RenderScriptTask extends AsyncTask<Float, Void, Integer> {
        Boolean _issued = false;
        String _rsKernel;

        public RenderScriptTask(String rsKernel){
            _rsKernel = rsKernel;
        }

        protected Integer doInBackground(Float... values) {
            int index = -1;
            if (!isCancelled()) {
                _issued = true;
                index = mCurrentBitmap;

                if(_rsKernel.equals("Jasność")){
                    //CreateScript(_optionsLabel);
                    _rsBrightness.set_brightness_value(values[0]);
                    _rsBrightness.forEach_brightness(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Kontrast")){
                    _rsContrast.set_contrast_value(values[0]);
                    _rsContrast.forEach_contrast(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Nasycenie")){
                    _rsSaturation.set_saturation_value(values[0]);
                    _rsSaturation.forEach_saturation(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Rozmycie")){
                    _rsBlur.setRadius(values[0]);
                    _rsBlur.setInput(_inAllocation);
                    _rsBlur.forEach(_outAllocations[index]);
                }
                else if(_rsKernel.equals("Negatyw")){
                    _rsInvert.forEach_invert(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Szarość - Średnia")){
                    _rsGSAverage.forEach_grayscale_average(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Szarość - YUV")){
                    _rsGSyuv.forEach_grayscale_yuv(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Bloom")){
                    // poprawic tego blooma - nie dziala zmianaspolczynnika swiatala;  wspolcznnik dobrany doswiadczalnie
                    _rsBloom.set_brightTreshold(values[0]/100.0f + 0.33f);
                    _rsBloom.forEach_bloom_bright_pass(_inAllocation, _outAllocations[index]);

                    _rsBlur.setRadius(values[0]);
                    _rsBlur.setInput(_inAllocation);
                    _rsBlur.forEach(_outAllocations[index]);

                    _rsBlend.forEachAdd(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Sepia")){
                    _rsSepia.forEach_sepia(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Progowanie")){
                    _rsTreshold.set_treshold_value(values[0]);
                    _rsTreshold.forEach_treshold(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Odcień")){
                    _rsTint.set_tint_value(values[0]);
                    _rsTint.forEach_tint(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Temperatura")){
                    _rsSimpleTemp.set_temperature_value(values[0]);
                    _rsSimpleTemp.forEach_simple_temperature(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Prześwietlenia")){
                    _rsLightManager.set_light_value(values[0]);
                    _rsLightManager.set_light_mode(true);
                    _rsLightManager.forEach_light_add_sub(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Cienie")){
                    _rsLightManager.set_light_value(values[0]);
                    _rsLightManager.set_light_mode(false);
                    _rsLightManager.forEach_light_add_sub(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Temperatura - Kelvin")){
                    _rsKelvinTemp.set_kelvin_value(values[0]*200);
                    _rsKelvinTemp.forEach_kelvin_temperature(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Atmosfera")){
                    _rsAtmosphere.forEach_atmosphere_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Ogień")){
                    _rsFire.forEach_fire_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Lód")){
                    _rsIce.forEach_ice_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Woda")){
                    _rsWater.forEach_water_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Ziemia")){
                    _rsEarth.forEach_earth_filter(_inAllocation, _outAllocations[index]);
                }
                _outAllocations[index].copyTo(_bitmapsOut[index]);
                mCurrentBitmap = (mCurrentBitmap + 1) % NUM_BITMAPS;
            }
            return index;
        }

        void updateView(Integer result) {
            if (result != -1) {
                _zoomPinchImageView.SetBitmap(_bitmapsOut[result]);
            }
        }

        protected void onPostExecute(Integer result) {
            updateView(result);
        }

        protected void onCancelled(Integer result) {
            if (_issued) {
                updateView(result);
            }
        }
    }

    // listener  dla messageboxow: uzyc tez do zapisu
    DialogInterface.OnClickListener _dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    OpenImageBrowser();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    DialogInterface.OnClickListener _saveClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    // tutaj wstawic zpisywanies
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    void CreateScript() {
        RenderScript rs = RenderScript.create(this);

        _inAllocation = Allocation.createFromBitmap(rs, _inputBitmap);
        _outAllocations = new Allocation[NUM_BITMAPS];
        for (int i = 0; i < NUM_BITMAPS; ++i) {
            _outAllocations[i] = Allocation.createFromBitmap(rs, _bitmapsOut[i]);
        }
        _rsBrightness = new ScriptC_brightness(rs);
        _rsContrast = new ScriptC_contrast(rs);
        _rsSaturation = new ScriptC_saturation(rs);
        _rsInvert = new ScriptC_invert(rs);
        _rsGSAverage = new ScriptC_grayscale_average(rs);
        _rsGSyuv = new ScriptC_grayscale_yuv(rs);
        _rsBloom = new ScriptC_bloom(rs);
        _rsSepia = new ScriptC_sepia(rs);
        _rsTreshold = new ScriptC_treshold(rs);
        _rsBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        _rsBlend = ScriptIntrinsicBlend.create(rs, Element.U8_4(rs));
        _rsBloom = new ScriptC_bloom(rs);
        _rsTint = new ScriptC_tint(rs);
        _rsSimpleTemp = new ScriptC_simple_temperature(rs);
        _rsLightManager = new ScriptC_light_manager(rs);
        _rsKelvinTemp = new ScriptC_kelvin_temperature(rs);
        _rsAtmosphere = new ScriptC_atmosphere_filter(rs);
        _rsFire = new ScriptC_fire_filter(rs);
        _rsIce = new ScriptC_ice_filter(rs);
        _rsWater = new ScriptC_water_filter(rs);
        _rsEarth = new ScriptC_earth_filter(rs);
    }

    void UpdateImage(final float f) {
        if (_currentTask != null) {
            _currentTask.cancel(false);
        }
        _currentTask = new RenderScriptTask(_optionsLabel);
        _currentTask.execute(f);
    }


    /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TESTOWO!!!!!!!!!!!!!!!!!!! */
    // listener dla kliknietego buttona
    View.OnClickListener btnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // reset slidera i labela po kazdym kliknieciu buttona

            _optionsLabel = ((Button) v).getText().toString();
            ResetSliderLayout();
            _sliderOptLayout.setVisibility(View.VISIBLE);

            //12.07.17
//            try{
//                _inputBitmap = GetBitmapFromUri(_imageUri);
//            }catch (IOException e){
//                Toast.makeText(getApplicationContext(), "Wyjebalo sie", Toast.LENGTH_LONG).show();
//            }

            InitRenderScriptOps();

            CreateScript();

            UpdateImage(0.0f);
//            _zoomPinchImageView.SetImgUri(GetImageUri(getApplicationContext(), _inputBitmap));


            long stop;
            long start = SystemClock.elapsedRealtime();
           // try{

                if(_optionsLabel.equals("Jasność")){
                    UpdateImage(0.0f);
                }
                else if(_optionsLabel.equals("Kontrast")){
                     UpdateImage(0.0f);
                }
                else if(_optionsLabel.equals("Nasycenie")){
                     UpdateImage(1.0f);
                }
                else if(_optionsLabel.equals("Rozmycie")){
                    _optSlider.setProgress(0);
                    _optSlider.setMax(24);
                    UpdateImage(1.0f);
                }
                else if(_optionsLabel.equals("Bloom")){
                    _optSlider.setProgress(0);
                    _optSlider.setMax(24);
                    UpdateImage(1.0f);
                     //UpdateImage(0.0f);
                    //_optSlider.setMax(200); // tymczasowo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                }
//                else if(_optionsLabel.equals("Sepia")){
//                    SepiaEffect();
//                }
                else if(_optionsLabel.equals("Progowanie")){
                    UpdateImage(0.5f);
                }
                else if(_optionsLabel.equals("Odcień")){
                    UpdateImage(0.0f);
                }
                else if(_optionsLabel.equals("Temperatura")){
                    UpdateImage(0.0f);
                }
                else if(_optionsLabel.equals("Prześwietlenia") || _optionsLabel.equals("Cienie") || _optionsLabel.equals("Atmosfera") ||
                        _optionsLabel.equals("Ogień") || _optionsLabel.equals("Lód") || _optionsLabel.equals("Woda") || _optionsLabel.equals("Ziemia")){

                    UpdateImage(0.0f);
                }
                else if(_optionsLabel.equals("Temperatura - Kelvin")){
                    UpdateImage(8000.0f);
                }
            }
            //catch (IOException e){
              //  Toast.makeText(getApplicationContext(), "Błąd operacji", Toast.LENGTH_SHORT).show();
            //}
//            stop = SystemClock.elapsedRealtime() - start;
//            Toast.makeText(getApplicationContext(), "CZAS "+Float.toString(stop/1000f), Toast.LENGTH_SHORT).show();
        };
//    };
    // PRZENIESC EFEKTY DO INNEJ KLASY
    // PRZENIESC EFEKTY DO INNEJ KLASY
    // PRZENIESC EFEKTY DO INNEJ KLASY
//    void BrightnessEffect(float brightness) throws IOException{
//        /***
//         * 1. po wcisnieciu przycisku tworzy obiekt renderscriptu
//         * 2. wlacza sie nasłuch na sliderze
//         * 3. okej tworzy bitmape
//         */
//        _resultBitmap = _coreOperation.Brightness(this, _inputBitmap, brightness);
//        _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//        Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//        _zoomPinchImageView.invalidate();
//    }

    public Uri GetImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TESTOWO!!!!!!!!!!!!!!!!!!! */

    // listener dla zmiany wartosci slidera
    SeekBar.OnSeekBarChangeListener sbValueChange = new SeekBar.OnSeekBarChangeListener(){
        float value;
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            // TUTAJ WRZUCIC TAKZE WSZYSTKIE OPERACJE GRAFICZNE W MOMENCIE GDY SLIDER ZMIENIA WARTOSC
                if(_optionsLabel.equals("Jasność")){
                    _valFromSlider = progress - 100;
                    value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Kontrast")){
                    _valFromSlider = progress - 100;
                    value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Nasycenie")){
                    _valFromSlider = progress;
                    value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Rozmycie")){
                     _valFromSlider = progress;
                     value = (((float)_valFromSlider)+1);
                }
                else if(_optionsLabel.equals("Bloom")){
                    _valFromSlider = progress;
                    value = (((float)_valFromSlider)+1);
                    //_valFromSlider = progress-100;
                    //value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Progowanie")){
                    _valFromSlider = progress/2;
                    value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Odcień")){
                    _valFromSlider = (progress - 100)/2;
                    value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Temperatura")){
                    _valFromSlider = (progress - 100)/2;
                    value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Prześwietlenia") || _optionsLabel.equals("Cienie")){
                    _valFromSlider = (progress - 100)/4;
                    value = (((float)_valFromSlider)/100.0f);
                }
                else if(_optionsLabel.equals("Temperatura - Kelvin")){
                    value = progress*200;
                }
                UpdateImage(value);
                _optSliderText.setText(_optionsLabel+" "+value);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };

    /***
     * Tworzenie widoku aktywności.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
// ma byc taka koleknosc
        HideNotifAndTitleBars();
        setContentView(R.layout.activity_editor);

       // _imageView = (ImageView)findViewById(R.id.imageView2);
        _zoomPinchImageView = (ZoomPinchImageView)findViewById(R.id.zoomPinchImageView);
        //**
       // _progressBar = (ProgressBar)findViewById(R.id.progressBar);

        //**
//        _infoButton = (Button)findViewById(R.id.infoButton);

        CheckActivity();
        InitOptionsBar();
        InitSliderListener();
    }

    /***
     * Metoda odpowiedzialna za wyświetlenie opcji menu (górny pasek)
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.editor_menu,menu);
            return true;
    }

    /***
     * Metoda odpowiedzialna za interakcje z elementami gornego menu.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_img_info:
                CheckGetInfoUriPermission();
                return true;
            case R.id.action_open:
                ShowAlert("Zmiany zostaną utracone. Kontynuować?",_dialogClickListener);
                return true;
            case R.id.action_reset_scale:
                ResetScale();
                return true;
            case R.id.action_save:
                ShowAlert("Czy chcesz zapisać zmiany?", _saveClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***
     * Metoda wywoływana po zakońeczniu działania aktywności galerii.
     * @param requestCode Kod zapytania.
     * @param resultCode  Kod rezultatu.
     * @param resultData Dane przekazane przez intencję.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData){
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            SetImageToView(resultData);
            //resetuj layout slidera po wczytaniu nowego zdjecia  i layouty
            _optionsLayout.setVisibility(View.INVISIBLE);
            _sliderOptLayout.setVisibility(View.INVISIBLE);
            ResetSliderLayout();
            ResetScale();
//
        }
    }

    void InitRenderScriptOps(){

        try{
            _inputBitmap = GetBitmapFromUri(_imageUri);
        }
        catch (IOException e){
            e.printStackTrace();
        }

        _bitmapsOut = new Bitmap[NUM_BITMAPS];
        for (int i = 0; i < NUM_BITMAPS; ++i) {
            _bitmapsOut[i] = Bitmap.createBitmap(_inputBitmap.getWidth(),
                    _inputBitmap.getHeight(), _inputBitmap.getConfig());
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        _bitmapsOut[mCurrentBitmap].compress(Bitmap.CompressFormat.JPEG, 100, stream);
        Glide.with(this)
                .load(stream.toByteArray())
                .asBitmap()
                .diskCacheStrategy( DiskCacheStrategy.NONE )
                .skipMemoryCache( false )
                .into(_zoomPinchImageView);


//        _zoomPinchImageView.SetBitmap(_bitmapsOut[mCurrentBitmap]);
        mCurrentBitmap += (mCurrentBitmap + 1) % NUM_BITMAPS;


    }

    void SaveBitmap(Bitmap bmp){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream("aa");
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap loadBitmap(int resource) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeResource(getResources(), resource, options);
    }

    /***
     * Metoda odpowiedzialna za schowanie paska powiadomień i nawigacji.
     */
    void HideNotifAndTitleBars(){
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }

    }

    void ShowAlert(String message, DialogInterface.OnClickListener clickListener){
        _builder = new AlertDialog.Builder(this);
        _builder.setMessage(message).setPositiveButton("Tak", clickListener)
                .setNegativeButton("Anuluj", clickListener).show();
    }

    /***
     * Metoda odpowiedzialna za uruchomienie intencji eksploratora plików w celu wybrania zdjęcia to załadowania, do widoku ImageView
     */
    void OpenImageBrowser(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    /***
     * Metoda odpowiedzialna za przekazanie zdjęcia zrobionego przez kamerę do komponentu ImageView.
     */
    void LoadImageFromCamera(){
        SetImageToView(_launchedIntent);
    }

    /***
     * Ustawienie zdjęcia z kamery bądz galerii do ImageView.
     * Korzysta z bilbioteki glide (szybsze operacje na mediach). Jeśli obiekt pod adresem uri jest pobrany z galerii nie wymaga dodatkowego konwertowania do klasy File.
     * @param resultData
     */
    void SetImageToView(Intent resultData){
        _intentHasExtras = resultData.hasExtra("IMAGE_TAKEN");
        if(resultData!=null) {
            _imageUri = (_intentHasExtras) ? Uri.fromFile(new File((Uri.parse(_launchedIntent.getStringExtra("IMAGE_TAKEN")).getPath()))) : resultData.getData();
//            Glide.with(this).load(_imageUri).centerCrop().into(_zoomPinchImageView); //uzywane jako thumbnail
//

            Glide
                    .with( this )
                    .load(_imageUri)
                    .diskCacheStrategy( DiskCacheStrategy.NONE )
                    .skipMemoryCache( true )
                    .into( _zoomPinchImageView);

            _zoomPinchImageView.SetImgUri(_imageUri);
        }
    }

    void ResetOptionsLayout(){
        if((_optionsLayout).getChildCount() > 0)
            ( _optionsLayout).removeAllViews();
    }

    void InitOptionsBar(){
        _optSliderText = (TextView)findViewById(R.id.optSliderText);
        _optSlider = (SeekBar)findViewById(R.id.optSlider);
        _optionsLayout = (LinearLayout)findViewById(R.id.topButtonsLayout);
        _sliderOptLayout = (LinearLayout)findViewById(R.id.scrollbarPanel);
        _optionsLayout.setVisibility(View.INVISIBLE);
        _sliderOptLayout.setVisibility(View.INVISIBLE);
    }

    void SetOptionsVisibility(String currBottomButton){
        int optionsVisibility = _optionsLayout.getVisibility();
        ResetOptionsLayout();
        if(optionsVisibility==View.VISIBLE && (_currBottomButton.equals(_prevBottomButton))){
            _optionsLayout.setVisibility(View.INVISIBLE);
            _sliderOptLayout.setVisibility(View.INVISIBLE);
        }
        else{
            _optionsLayout.setVisibility(View.VISIBLE);
            if(!_currBottomButton.equals(_prevBottomButton)){
                _sliderOptLayout.setVisibility(View.INVISIBLE);
            }
        }
        _prevBottomButton = _currBottomButton;
    }

    // DODAC ID DLA KAZDEGO BUTTONA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // SPRAWDZIC UUPRAWNIENIA DO KARTY PAMIECI !!!!!!!!!!!!!!!!!!!!!!!!!!! - jest ok stare thumbnaile
    void FillOptionsBar(String[] buttonValues, Button[] buttonList, int buttonCount){
        for(int i=0; i<buttonCount; i++){

            buttonList[i] = new Button(this);
            buttonList[i].setTag(buttonValues[i]);
            buttonList[i].setText(buttonValues[i]);
            // ma wyswietlic slider z odpowiednia etykieta
            buttonList[i].setOnClickListener(btnClicked);

            _optionsLayout.addView(buttonList[i],new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    void ResetSliderLayout(){
        _optSlider.setProgress(100);
        _optSliderText.setText(_optionsLabel+" 0");
    }

    void InitSliderListener(){
        _optSlider.setOnSeekBarChangeListener(sbValueChange);
    }

    void ShowAdjustments(View v){
        _currBottomButton = "adjustments";
        _adjustmentsButtonsList = new Button[ADJUSTMENT_COUNT];
        SetOptionsVisibility(_currBottomButton);
        FillOptionsBar(_adjustmentValues, _adjustmentsButtonsList ,ADJUSTMENT_COUNT);
    }

    void ShowDetails(View v){
        _currBottomButton = "details";
        _detailsButtonList = new Button[DETAILS_COUNT];
        SetOptionsVisibility(_currBottomButton);
        FillOptionsBar(_detailsValues, _detailsButtonList, DETAILS_COUNT);
    }
    void ShowFilters(View v){
        _currBottomButton = "filters";
        _filtersButtonList = new Button[FILTERS_COUNT];
        SetOptionsVisibility(_currBottomButton);
        FillOptionsBar(_filtersValues, _filtersButtonList, FILTERS_COUNT);
    }
    void ShowWhiteBalance(View v){
        _currBottomButton = "whitebalance";
        _wbButtonList = new Button[WHITE_BALANCE_COUNT];
        SetOptionsVisibility(_currBottomButton);
        FillOptionsBar(_whiteBalanceValues, _wbButtonList, WHITE_BALANCE_COUNT);
    }

    void ShowRotation(View v){
        _currBottomButton = "rotation";
        _rotationButtonList = new Button[ROTATIONS_COUNT];
        SetOptionsVisibility(_currBottomButton);
        FillOptionsBar(_rotationValues, _rotationButtonList, ROTATIONS_COUNT);
    }

    void ShowHistogram(View v){
        Toast.makeText(this, "dupa", Toast.LENGTH_SHORT).show();
    }

    /***
     * Metoda odpowiedzialna za zresetowanie skali zdjęcia.
     */
    void ResetScale(){
        _zoomPinchImageView.SetScaleFactor(1.f);
        _zoomPinchImageView.invalidate();
        _zoomPinchImageView.requestLayout();
    }

    /***
     * Metoda sprawdza z której aktywności pochodzi zdjęcie do wczytania w widoku ImageView.
     */
    void CheckActivity(){
        /***
         * Pobranie intencji i sprawedzenie czy posiada dodatkowe argumenty w celu przeprowadzenia odpowiednich akcji
         */
        _launchedIntent = getIntent();
        if(_launchedIntent.hasExtra("IMAGE_TAKEN")) LoadImageFromCamera();
        else OpenImageBrowser();
    }

    //without glide
    Bitmap GetBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }

    //dodać sprawdzanie orientacji zdjecia po zrobieniu go
    /***
     * OPCJONALNE
     *
     */

    /***
     * Metoda odpowiedzialna za wyświetlenie metadanych ze zdjęcia.
     */
    void ShowImageInfo(){
        Intent info = new Intent(Editor.this, ImageInfo.class);

        info.putExtra("IMAGE_INFO",UriConverter.getPath(this, _imageUri));
        startActivity(info);
    }

    // dorobić interfejs do pozwolen

    /***
     * Metoda odpowidzialna za przydzielenie uprawnień do czytania URI.
     */
    void CheckGetInfoUriPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            ShowImageInfo();
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this,"Zezwolić aplikacji na odczyt URI?", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_URI_PERMISSION);
            }
        }
    }

    /***
     * Metoda odpowiedzialna za dalszą interakcję po nadaniu uprawnień.
     * @param requestCode
     * @param permissions
     * @param grantResult
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult){
        if(requestCode == READ_URI_PERMISSION){
            if(grantResult[0] == PackageManager.PERMISSION_GRANTED){
                ShowImageInfo();
            }
            else{
                Toast.makeText(this,"Czy chcesz zmodyfikować zawartość Karty Pamięci?", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        }
    }
}
