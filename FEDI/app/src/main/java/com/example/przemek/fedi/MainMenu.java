package com.example.przemek.fedi;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hitomi.cmlibrary.CircleMenu;
import com.hitomi.cmlibrary.OnMenuSelectedListener;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.jar.Manifest;

/***
 * Klasa aktywności menu glównego.
 */
public class MainMenu extends AppCompatActivity {
    /***
     * Zwracane przez aktywność kody.
     */
    static final int CAMERA_ACTIVITY_RESULT = 0, EXTERNAL_STORAGE_RESULT = 1, MAIN_MENU_REQUEST_CODE = 0;

    boolean _doubleBackToExitPressedOnce;

    /***
     * Obiekt będący refenencją do okrągłego menu;
     * Ladowana intencja z menu;
     * Otrzymana scieżka do zdjęcia;
     */
    CircleMenu _circleMenu;
    Intent _nextIntent;
    String _currentPhotoPath;


    /***
     * Tworzenie głównej aktywności.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HideNotifAndTitleBars();
        setContentView(R.layout.activity_main_menu);

        InstantiateCircleMenu();
    }


    void HideNotifAndTitleBars(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Menu Główne");
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
        }

    }

    /***
     * Metoda odpowiedzialna za wyswietlanie okrągłego menu. Wybrany przycisk prowadzi do odpowiednich aktywnsci
     */
    //sprawdzic
    void InstantiateCircleMenu(){
        _circleMenu = (CircleMenu) findViewById(R.id.circle_menu);
        _circleMenu.setMainMenu(Color.parseColor("#CDCDCD"), R.mipmap.shutter, R.mipmap.shutter);
        _circleMenu.addSubMenu(Color.parseColor("#258CFF"), R.mipmap.gallery)
            .addSubMenu(Color.parseColor("#30A400"), R.mipmap.camera)
            .addSubMenu(Color.parseColor("#FF4B32"), R.mipmap.abouticon)
            .setOnMenuSelectedListener(new OnMenuSelectedListener() {
                @Override
                public void onMenuSelected(int i) {
                    if(i==1){
                        AskCameraPermissions();
                    }
                    else{
                        _nextIntent = new Intent(MainMenu.this, ((i==0) ? Editor.class : Info.class));
                        startActivity(_nextIntent);
                    }
                    //finish();
                }
            });
    }

    /***
     * Metoda prosząca o odpowiednie uprawnienia podczas użytkownia aplikacji.
     */
// pytanie o uprawnienia do aparatu
    void AskCameraPermissions(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            MakePhoto();
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
                MakePhoto();
            }
            else{
                Toast.makeText(this,"Czy chcesz zmodyfikować zawartość Karty Pamięci?", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        }

    }

    /***
     * Metoda odpowiedzialna za zrobienie zdjęcia. Najpierw uruchamiana jest aktyność kamery. Później generowany timestamp - unikanlny identyfikator zdjęcia.
     * Gdy zdjęcie zostanie zrobione pomyślnie, aplikacja przechodzi w tryb edycji.
     */
    void MakePhoto(){
        _nextIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //if(_nextIntent.resolveActivity(getPackageManager())!=null){
        File photoFile = null;
        try{
            photoFile = SaveImage();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        // ANDROID 7.0 + wymaga FILEPROVIDERA, zamiast uri, content:uris
        //Use the FileProvider to get the Uri from the file
        if(photoFile!=null){
            Uri photoUri = FileProvider.getUriForFile(this,getApplicationContext().getPackageName()+".fileprovider",photoFile);
            _nextIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(_nextIntent,CAMERA_ACTIVITY_RESULT);
        }
        //}
        //_nextIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        //startActivityForResult(_nextIntent, CAMERA_ACTIVITY_RESULT);
    }

    /***
     * Metoda odpowiedzialna za zapisanie zrobionego zdjęcia w pamięci urządzenia.
     * @return Plik będący zdjęciem.
     * @throws IOException
     */
    File SaveImage() throws IOException{
        String imgName = "img_"+(new  java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        File capturedImage = File.createTempFile(imgName, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        _currentPhotoPath = capturedImage.getAbsolutePath();
        return capturedImage;
    }

    /***
     * Metoda uruchamiająca aktywność Edytora po zrobieniu zdjęcia.
     * @param requestCode Kod dostępu.
     * @param resultCode Kod rezultatu.
     * @param data Dane.
     */
    @Override
    protected void onActivityResult (int requestCode,int resultCode,Intent data){
        if(requestCode == CAMERA_ACTIVITY_RESULT && resultCode == RESULT_OK){
            Intent editorIntent = new Intent(getApplicationContext(), Editor.class);
            editorIntent.putExtra("IMAGE_TAKEN",_currentPhotoPath);
            startActivityForResult(editorIntent, MAIN_MENU_REQUEST_CODE);
        }
    }

    @Override
    public void onBackPressed() {

        if (_circleMenu.isOpened())
            _circleMenu.closeMenu();
        else{
            if (_doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            _doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Wciśnij WSTECZ ponownie, aby wyjść.", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    _doubleBackToExitPressedOnce=false;
                }
            }, 2000);

            //finish();
        }
    }

}
