package com.example.przemek.fedi;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/***
 * Klasa aktywności Edytora zdjęć.
 */
public class Editor extends AppCompatActivity {

    static final int REQUEST_CODE = 0;

    /***
     * Zmienna będąca referencją do komponentu ImageView;
     * Zmienna będąca referencją do obecnie uruchominonej intencji.
     */

    /***
     *
     *
     *
     * SPRAWDZIC TO URI
     *
     *
     *
     *
     */

    // animacja
    Animator _currentAnimator;
    int _animationDuration;

    // widoki zdjecia
    ImageView _imageView;
    ZoomPinchImageView _zoomPinchImageView;

    Intent _launchedIntent;

    // do metadanych
    Uri _imageUri = null;
    String _fileName;


    //Bitmap _imageBitmap;

    /***
     * Tworzenie widoku aktywności.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        _imageView = (ImageView)findViewById(R.id.ImageView);
        _zoomPinchImageView = (ZoomPinchImageView)findViewById(R.id.zoomPinchImageView);

        // dodać padding do widoku
        _imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(Editor.this, "Dlugi klik", Toast.LENGTH_SHORT).show();
//                ZoomImageAnimation();
                ZoomPinchPan();
                return true;
            }
        });

        _animationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        CheckActivity();
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
        boolean intentHasExtras = resultData.hasExtra("IMAGE_TAKEN");
        if(resultData!=null) {
            _imageUri = (intentHasExtras) ? Uri.fromFile(new File((Uri.parse(_launchedIntent.getStringExtra("IMAGE_TAKEN")).getPath()))) : resultData.getData();
            Glide.with(this).load(_imageUri).into(_imageView); //uzywane jako thumbnail
            //Glide.with(this).load(_imageUri).into(_zoomPinchImageView); //uzywane w zoom animation
//            Glide.with(this).load((intentHasExtras) ? new File(_imageUri.getPath()) : _imageUri).into(_imageView); //stary widok
        }
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
//    Bitmap GetBitmapFromUri(Uri uri) throws IOException{
//        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
//        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
//        parcelFileDescriptor.close();
//        return bitmap;
//    }

    /***
     * Skalowanie zdjęcia do rozmiaru panelu image view
     * Pobierane są wymiary panelu imageview, nastepnie przeliczany wspolczynnik skali, a na koncu
     * wartosci
     */
    void ScaleDownToView(){
        int imageViewWidth = _imageView.getWidth();
        int imageViewHeight = _imageView.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(_imageUri.toString(),bmOptions);

        int imageWidth = bmOptions.outWidth;
        int imageHeight = bmOptions.outHeight;
        int scaleFactor = Math.min(imageWidth/imageViewWidth, imageHeight/imageViewHeight);
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inJustDecodeBounds = false;

        Bitmap photoReducedSizeBitmap = BitmapFactory.decodeFile(_imageUri.toString(), bmOptions);
        _imageView.setImageBitmap(photoReducedSizeBitmap);

    }

    //dodać sprawdzanie orientacji zdjecia po zrobieniu go
    /***
     *
     * OPCJONALNE
     *
     *
     *
     */

    /***
     * Przyblizanie zdjęcia z pobranego thumbnaila.
     */
    void ZoomImageAnimation(){
        float zoomViewRatio, normalViewRatio, beginScale, beginWidth, widthDiff, beginHeight, heightDiff;
        Rect startDimensions = new Rect();
        Rect finalDimensions = new Rect();
        Point globalOffset = new Point();

        if(_currentAnimator != null){
            _currentAnimator.cancel();
        }
        Glide.with(this).load(_imageUri).into(_zoomPinchImageView);

        _imageView.getGlobalVisibleRect(startDimensions);
        findViewById(R.id.container).getGlobalVisibleRect(finalDimensions,globalOffset);

        startDimensions.offset(-globalOffset.x, -globalOffset.y);
        finalDimensions.offset(-globalOffset.x, -globalOffset.y);
        zoomViewRatio = (float)finalDimensions.width()/(float)finalDimensions.height();
        normalViewRatio = (float)startDimensions.width()/(float)startDimensions.height();

        if(zoomViewRatio > normalViewRatio){
            beginScale = (float) startDimensions.height() / finalDimensions.height();
            beginWidth = beginScale * finalDimensions.width();
            widthDiff = (beginWidth - startDimensions.width()) / 2;
            startDimensions.left -= widthDiff;
            startDimensions.right += widthDiff;
        }
        else{
            beginScale = (float) startDimensions.width() / finalDimensions.width();
            beginHeight = beginScale * finalDimensions.height();
            heightDiff = (beginHeight - startDimensions.height()) / 2;
            startDimensions.top -= heightDiff;
            startDimensions.bottom += heightDiff;
        }

        _imageView.setAlpha(0f);
        _zoomPinchImageView.setVisibility(View.VISIBLE);
        _zoomPinchImageView.setPivotX(0);
        _zoomPinchImageView.setPivotY(0);

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(_zoomPinchImageView, View.X, startDimensions.left, finalDimensions.left))
            .with(ObjectAnimator.ofFloat(_zoomPinchImageView, View.Y, startDimensions.top, finalDimensions.top))
            .with(ObjectAnimator.ofFloat(_zoomPinchImageView, View.SCALE_X, beginScale, 1f))
            .with(ObjectAnimator.ofFloat(_zoomPinchImageView, View.SCALE_Y, beginScale, 1f));

        set.setDuration(_animationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);

                _currentAnimator = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                _currentAnimator = null;
            }
        });

        set.start();
        _currentAnimator = set;
    }

    /***
     * Maksymalizacja okna
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View decoratorView = getWindow().getDecorView();
        if(hasFocus){
            decoratorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    void ZoomPinchPan(){
        _zoomPinchImageView.SetImgUri(_imageUri);
        _imageView.setAlpha(0.f);
        _zoomPinchImageView.setVisibility(View.VISIBLE);
    }

}
