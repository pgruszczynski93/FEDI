package com.example.przemek.fedi;

import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Przemek on 2017-09-16.
 */

// do poprawy w innej wersji
public class HardwareInfo extends AppCompatActivity{
    DisplayMetrics _dm;
    int _dispW, _dispH;
    TextView _hardwareInfoText;
    Map<String, String> _hardwareMap;
    String mVendor;
    GLSurfaceView mGlSurfaceView;

    GLSurfaceView.Renderer mGlRenderer = new GLSurfaceView.Renderer() {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {// TODO Auto-generated method stub
//            Log.d(TAG, "gl renderer: "+gl.glGetString(GL10.GL_RENDERER));
//            Log.d(TAG, "gl vendor: "+gl.glGetString(GL10.GL_VENDOR));
//            Log.d(TAG, "gl version: "+gl.glGetString(GL10.GL_VERSION));
//            Log.d(TAG, "gl extensions: "+gl.glGetString(GL10.GL_EXTENSIONS));

            mVendor = gl.glGetString(GL10.GL_VENDOR);
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    _hardwareInfoText.setText(mVendor);

                }
            });}

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            // TODO Auto-generated method stub

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Informacje Sprzętowe");
        setContentView(R.layout.imageinfolayout);

        _hardwareInfoText = (TextView)findViewById(R.id.imageInfoText);
        _hardwareInfoText.setMovementMethod(new ScrollingMovementMethod());

        FillMap();
        InitDialog();
        FillTextView();
    }

    void InitDialog(){
        _dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(_dm);

        _dispW = _dm.widthPixels;
        _dispH = _dm.heightPixels;

        getWindow().setLayout((int)(_dispW * .9), (int)(_dispH*.9));
    }

    void FillMap(){
        _hardwareMap = new LinkedHashMap<String, String>();
        _hardwareMap.put(Build.MANUFACTURER, "Producent: ");
        _hardwareMap.put(Build.MODEL, "Model:");
        _hardwareMap.put(Build.SERIAL, "Numer seryjny:");
        _hardwareMap.put(Build.ID, "ID urządzenia:");
        _hardwareMap.put(Build.TYPE, "System");
        _hardwareMap.put(Build.VERSION.RELEASE, "Wersja systemu:");
        _hardwareMap.put(Build.BOARD, "Płyta główna:");
        _hardwareMap.put(Build.DISPLAY, "Wyświetlacz:");
        _hardwareMap.put(Build.HARDWARE, "###################");


        _hardwareMap.put(Build.SERIAL, "1");
        _hardwareMap.put(Build.MODEL, "1");
        _hardwareMap.put(Build.ID, "1");
        _hardwareMap.put(Build.MANUFACTURER, "1");
        _hardwareMap.put(Build.BRAND, "1");
        _hardwareMap.put(Build.TYPE, "1");
        _hardwareMap.put(Build.USER, "1");
        _hardwareMap.put(Build.VERSION.INCREMENTAL, "1");
        _hardwareMap.put(Build.BOARD, "1");
        _hardwareMap.put(Build.BRAND, "1");
        _hardwareMap.put(Build.HOST, "1");
        _hardwareMap.put(Build.FINGERPRINT, "1");
        _hardwareMap.put(Build.VERSION.RELEASE, "1");
        _hardwareMap.put(Build.VERSION.RELEASE, "1");
    }

    String GetTagString(String tagDefinition, String tagDescr)
    {
        return("<b>"+tagDefinition+"</b>" + ":<br>" +tagDescr+ "<br><br>");
    }

    void FillTextView(){
        String myAttribute = "";
        for(Map.Entry<String, String> entry  : _hardwareMap.entrySet()){
            myAttribute += GetTagString(entry.getValue(),entry.getKey());
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            _hardwareInfoText.setText(Html.fromHtml(myAttribute,Html.FROM_HTML_MODE_COMPACT));
        }
        else {
            _hardwareInfoText.setText(Html.fromHtml(myAttribute));
        }
    }

    public void CloseInfo(View v){
        finish();
    }

}
