package com.example.przemek.fedi;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/***
 * Klasa aktywności Edytora zdjęć.
 */
public class Editor extends AppCompatActivity {

    static final int REQUEST_CODE = 0, READ_URI_PERMISSION = 1;
    final int ADJUSTMENT_COUNT = 6, DETAILS_COUNT = 2, FILTERS_COUNT = 10, WHITE_BALANCE_COUNT = 2, ROTATIONS_COUNT = 3;
    final String[] _adjustmentValues = {"Jasność", "Kontrast", "Nasycenie","Prześwietlenia", "Cienie", "Temperatura"};
    final String[] _detailsValues = {"Struktura", "Wyostrzanie"};
    final String[] _filtersValues = {"Negatyw", "Szarość 1", "Rozmycie", "F1", "F1", "F1", "F1", "F1", "F1", "F1"};
    final String[] _whiteBalanceValues = {"Temperatura", "Odcień"};
    final String[] _rotationValues = {"Kąt", "90 w lewo", "90 w prawo"};


    FediCore _coreOperation;
    Bitmap _inputBitmap, _resultBitmap;
    /// NOWE
    Bitmap _imageBitmap;
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
//    ImageView _imageView;
    ZoomPinchImageView _zoomPinchImageView;

    Intent _launchedIntent;

    Uri _imageUri = null;

    boolean _intentHasExtras;
    //Bitmap _imageBitmap;

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

    /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TESTOWO!!!!!!!!!!!!!!!!!!! */
    // listener dla kliknietego buttona
    View.OnClickListener btnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // reset slidera i labela po kazdym kliknieciu buttona
            _optionsLabel = ((Button) v).getText().toString();
            ResetSliderLayout();
            _sliderOptLayout.setVisibility(View.VISIBLE);

            if(_optionsLabel.equals("Rozmycie")){
                // dorobić pasek ladowaniie
                BlurEffect();
            }
            else if(_optionsLabel.equals("Negatyw")){
                InvertEffect();
            }
            else if(_optionsLabel.equals("Szarość 1")){
                GrayscaleAverageEffect();
            }
        }
    };
    // PRZENIESC EFEKTY DO INNEJ KLASY
    // PRZENIESC EFEKTY DO INNEJ KLASY
    // PRZENIESC EFEKTY DO INNEJ KLASY
    void BlurEffect(){
        try {
            _inputBitmap = GetBitmapFromUri(_imageUri);
            _resultBitmap = _coreOperation.Blur(this, _inputBitmap);
            _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
            Glide
                    .with( this )
                    .load( GetImageUri(this, _resultBitmap) )
                    .diskCacheStrategy( DiskCacheStrategy.NONE )
                    .skipMemoryCache( true )
                    .into( _zoomPinchImageView);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Nie można użyć rozmycia", Toast.LENGTH_SHORT).show();

        }
    }
    void GrayscaleAverageEffect(){
        try {
            _inputBitmap = GetBitmapFromUri(_imageUri);
            _resultBitmap = _coreOperation.GrayScaleAvg(this, _inputBitmap);
            _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
            Glide
                    .with( this )
                    .load( GetImageUri(this, _resultBitmap) )
                    .diskCacheStrategy( DiskCacheStrategy.NONE )
                    .skipMemoryCache( true )
                    .into( _zoomPinchImageView);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Nie można użyć rozmycia", Toast.LENGTH_SHORT).show();

        }
    }
    void InvertEffect(){
        try {
            _inputBitmap = GetBitmapFromUri(_imageUri);
            _resultBitmap = _coreOperation.Invert(this, _inputBitmap);
            _zoomPinchImageView.SetImgUri(GetImageUri(this, _resultBitmap));
            Glide
                    .with( this )
                    .load( GetImageUri(this, _resultBitmap) )
                    .diskCacheStrategy( DiskCacheStrategy.NONE )
                    .skipMemoryCache( true )
                    .into( _zoomPinchImageView);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Nie można użyć rozmycia", Toast.LENGTH_SHORT).show();

        }
    }
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
            _optSliderText.setText(_optionsLabel+" "+_valFromSlider);
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

//        _imageView = (ImageView)findViewById(R.id.ImageView);
        _zoomPinchImageView = (ZoomPinchImageView)findViewById(R.id.zoomPinchImageView);
//        _infoButton = (Button)findViewById(R.id.infoButton);

        CheckActivity();
        InitOptionsBar();
        InitSliderListener();

        //===========================================================================================            INIT CORA
        _coreOperation = new FediCore();

        // odswiezanie wartosci ze slidera
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

            //ScaleDownToView(); // zeskalowane zdjecie
//                standrad bitmap loading vs glide version
//                try{
//                    _imageBitmap = GetBitmapFromUri(uri);
//                    _imageView.setImageBitmap(_imageBitmap); - using standard bitmap processing vs using glide
//                }
//                catch(IOException e){
//                    e.printStackTrace();
//                }
//            }
        }

        //kadrowanie
//        if(requestCode == 2 && resultCode == RESULT_OK && resultData != null)
//        {
//            Bundle extras = resultData.getExtras();
//            Bitmap image = extras.getParcelable("data");
//            _zoomPinchImageView.setImageBitmap(image);
//        }
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

//                try{
//                    _imageBitmap = GetBitmapFromUri(_imageUri);
//                    _zoomPinchImageView.setImageBitmap(_imageBitmap);
//                }
//                catch(IOException e){
//                    e.printStackTrace();
//                }
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ZAWSZE PRZESTAWIAC URI PRZED WYWOLANIEM ZDJECIA !!!!!!!!!!!!!!!!!!!!
            _zoomPinchImageView.SetImgUri(_imageUri);

        }
    }

    void ResetOptionsLayout(){
//        if(buttonList != null)
//            buttonList.clear();
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
//        _optionsLayout.setVisibility((optionsVisibility==View.VISIBLE && (_currBottomButton.equals(_prevBottomButton))) ? View.INVISIBLE : View.VISIBLE);
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

    void CropImage(View v){
        try{
//            Toast.makeText(this, "DUPAAA!!!", Toast.LENGTH_SHORT).show();
//            Intent cropIntent = new Intent("com.android.camera.action.CROP");
//            cropIntent.setDataAndType(_imageUri,"image/*");
//            cropIntent.putExtra("crop","true");
//            cropIntent.putExtra("outputX",180);
//            cropIntent.putExtra("outputY",180);
//            cropIntent.putExtra("aspectX",3);
//            cropIntent.putExtra("aspectX",4);
//            cropIntent.putExtra("scaleUpIfNeeded",true);
//            cropIntent.putExtra("return-data",true);
            //startActivityForResult(cropIntent,1);
//            Intent imageDownload = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            imageDownload.putExtra("crop", "true");
//            imageDownload.putExtra("aspectX", 1);
//            imageDownload.putExtra("aspectY", 1);
//            imageDownload.putExtra("outputX", 200);
//            imageDownload.putExtra("outputY", 200);
//            imageDownload.putExtra("return-data", true);
//            startActivityForResult(imageDownload, 2);
        }catch (ActivityNotFoundException ex){
            Toast.makeText(this, "Brak aktywności", Toast.LENGTH_SHORT).show();
        }
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

    /***
     * Skalowanie zdjęcia do rozmiaru panelu image view
     * Pobierane są wymiary panelu imageview, nastepnie przeliczany wspolczynnik skali, a na koncu
     * wartosci
     */
    void ScaleDownToView(){
        // było _imageView
        int imageViewWidth = _zoomPinchImageView.getWidth();
        int imageViewHeight = _zoomPinchImageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(_imageUri.toString(),bmOptions);

        int imageWidth = bmOptions.outWidth;
        int imageHeight = bmOptions.outHeight;
        int scaleFactor = Math.min(imageWidth/imageViewWidth, imageHeight/imageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        Bitmap photoReducedSizeBitmap = BitmapFactory.decodeFile(_imageUri.toString(), bmOptions);
        _zoomPinchImageView.setImageBitmap(photoReducedSizeBitmap);

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
