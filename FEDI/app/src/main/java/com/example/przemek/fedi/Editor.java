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
import android.support.v8.renderscript.RenderScript;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
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
    final int ADJUSTMENT_COUNT = 6, DETAILS_COUNT = 2, FILTERS_COUNT = 11, WHITE_BALANCE_COUNT = 2, ROTATIONS_COUNT = 3;
    final String[] _adjustmentValues = {"Jasność", "Kontrast", "Nasycenie","Prześwietlenia", "Cienie", "Temperatura"};
    final String[] _detailsValues = {"Struktura", "Wyostrzanie"};
    final String[] _filtersValues = {"Negatyw", "Szarość - Średnia", "Szarość - YUV", "Rozmycie", "Bloom", "Sepia", "Progowanie", "F1", "F1", "F1", "F1"};
    final String[] _whiteBalanceValues = {"Temperatura", "Odcień"};
    final String[] _rotationValues = {"Kąt", "90 w lewo", "90 w prawo"};

//    FediCore _coreOperation;
    final int NUM_BITMAPS = 2;
    int mCurrentBitmap = 0;
    Bitmap[] _bitmapsOut;
    Allocation _inAllocation;
    Allocation[] _outAllocations;
    ScriptC_saturation _script;
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
        Boolean issued = false;

        protected Integer doInBackground(Float... values) {
            int index = -1;
            if (!isCancelled()) {
                issued = true;
                index = mCurrentBitmap;

                // Set global variable in RS
                _script.set_saturation_value(values[0]);

                // Invoke saturatio filter kernel
                _script.forEach_saturation(_inAllocation, _outAllocations[index]);

                // Copy to bitmap and invalidate image view
                _outAllocations[index].copyTo(_bitmapsOut[index]);
                mCurrentBitmap = (mCurrentBitmap + 1) % NUM_BITMAPS;
            }
            return index;
        }

        void updateView(Integer result) {
            if (result != -1) {
                // Request UI update
                _zoomPinchImageView.SetBitmap(_bitmapsOut[result]);
            }
        }

        protected void onPostExecute(Integer result) {
            updateView(result);
        }

        protected void onCancelled(Integer result) {
            if (issued) {
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

    private void createScript() {
        // Initialize RS
        RenderScript rs = RenderScript.create(this);

        // Allocate buffers
        _inAllocation = Allocation.createFromBitmap(rs, _inputBitmap);
        _outAllocations = new Allocation[NUM_BITMAPS];
        for (int i = 0; i < NUM_BITMAPS; ++i) {
            _outAllocations[i] = Allocation.createFromBitmap(rs, _bitmapsOut[i]);
        }

        // Load script
        _script = new ScriptC_saturation(rs);
    }

    private void updateImage(final float f) {
        if (_currentTask != null) {
            _currentTask.cancel(false);
        }
        _currentTask = new RenderScriptTask();
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



            long stop;
            long start = SystemClock.elapsedRealtime();
           // try{

                if(_optionsLabel.equals("Jasność")){
                    //_coreOperation = new FediCore(_inputBitmap, _zoomPinchImageView);
                    //_coreOperation.CreateRenderscript(getApplicationContext(), _optionsLabel);
                }
//                else if(_optionsLabel.equals("Kontrast")){
//                    ContrastEffect();
//                }
//                else if(_optionsLabel.equals("Nasycenie")){
//                    SaturationEffect();
//                }
//                else if(_optionsLabel.equals("Rozmycie")){
//                    BlurEffect();
//                }
//                else if(_optionsLabel.equals("Negatyw")){
//                    InvertEffect();
//                }
//                else if(_optionsLabel.equals("Szarość - Średnia")){
//                    GrayscaleAverageEffect();
//                }
//                else if(_optionsLabel.equals("Szarość - YUV")){
//                    GrayscaleYUVEffect();
//                }
//                else if(_optionsLabel.equals("Bloom")){
//                    BloomEffect();
//                }
//                else if(_optionsLabel.equals("Sepia")){
//                    SepiaEffect();
//                }
//                else if(_optionsLabel.equals("Progowanie")){
//                    TresholdEffect();
//                }
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

//    void ContrastEffect() throws IOException{
//        _inputBitmap = GetBitmapFromUri(_imageUri);
//        _resultBitmap = _coreOperation.Contrast(this, _inputBitmap);
//        _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//        Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
//
//    void SaturationEffect() throws IOException{
//        _inputBitmap = GetBitmapFromUri(_imageUri);
//        _resultBitmap = _coreOperation.Saturation(this, _inputBitmap);
//        _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//        Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
//
//    void BlurEffect() throws IOException{
//            _inputBitmap = GetBitmapFromUri(_imageUri);
//            _resultBitmap = _coreOperation.Blur(this, _inputBitmap);
//            _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//            Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
//    void GrayscaleAverageEffect() throws IOException{
//            _inputBitmap = GetBitmapFromUri(_imageUri);
//            _resultBitmap = _coreOperation.GrayScaleAvg(this, _inputBitmap);
//            _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//             Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//
//    }
//    void GrayscaleYUVEffect() throws IOException{
//            _inputBitmap = GetBitmapFromUri(_imageUri);
//            _resultBitmap = _coreOperation.GrayScaleYUV(this, _inputBitmap);
//            _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//            Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
//    void InvertEffect() throws IOException{
//            _inputBitmap = GetBitmapFromUri(_imageUri);
//            _resultBitmap = _coreOperation.Invert(this, _inputBitmap);
//            _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//            Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
//
//    void BloomEffect() throws IOException{
//        _inputBitmap = GetBitmapFromUri(_imageUri);
//        _resultBitmap = _coreOperation.Bloom(this, _inputBitmap);
//        _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//        Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
//
//    void SepiaEffect() throws IOException{
//        _inputBitmap = GetBitmapFromUri(_imageUri);
//        _resultBitmap = _coreOperation.Sepia(this, _inputBitmap);
//        _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//        Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
//
//    void TresholdEffect() throws IOException{
//        _inputBitmap = GetBitmapFromUri(_imageUri);
//        _resultBitmap = _coreOperation.Treshhold(this, _inputBitmap);
//        _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
//        Glide.with( this ).load( GetImageUri(this, _resultBitmap) ).diskCacheStrategy( DiskCacheStrategy.NONE ).skipMemoryCache( true ).into( _zoomPinchImageView);
//    }
    // PRZENIESC EFEKTY DO INNEJ KLASY
    // PRZENIESC EFEKTY DO INNEJ KLASY
    // PRZENIESC EFEKTY DO INNEJ KLASY
    public Uri GetImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TESTOWO!!!!!!!!!!!!!!!!!!! */

    // listener dla zmiany wartosci slidera
    SeekBar.OnSeekBarChangeListener sbValueChange = new SeekBar.OnSeekBarChangeListener(){
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            // TUTAJ WRZUCIC TAKZE WSZYSTKIE OPERACJE GRAFICZNE W MOMENCIE GDY SLIDER ZMIENIA WARTOSC
            _valFromSlider = progress-100;
           // _optSliderText.setText(_optionsLabel+" "+_valFromSlider);
            //try{
                if(_optionsLabel.equals("Nasycenie")){

                    float f = (((float)_valFromSlider)/100.0f);
                    updateImage(f);
                    _optSliderText.setText(_optionsLabel+" "+f);
                }
            //}
            //catch(IOException e){
              //  Toast.makeText(getApplicationContext(), "Błąd operacji", Toast.LENGTH_SHORT).show();
            //}
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
                //ResetScale();
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
            InitRenderScriptOps();
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
                .skipMemoryCache( true )
                .into(_zoomPinchImageView);
        mCurrentBitmap += (mCurrentBitmap + 1) % NUM_BITMAPS;

        createScript();
        updateImage(0.0f);
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
