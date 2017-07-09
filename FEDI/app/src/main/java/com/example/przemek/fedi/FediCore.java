package com.example.przemek.fedi;


import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v8.renderscript.*;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;

/**
 * Created by Przemek on 2017-06-24.
 */

public class FediCore {
    // ************************************************************************** sprawdzic
    static final float BLUR_RADIUS = 15f;
    final int BITMAPS_COUNT = 2;

    int _width, _height, _currentBitmap = 0;
    Bitmap _inputBitmap, _outputBitmap;
    Bitmap[] _outputBitmaps;
    ZoomPinchImageView _zoomPinchImageView;

    RenderScript _renderScript;
    Allocation _inAllocation, _outAllocation;
    Allocation[] _outAllocations;
    RenderScriptTask _currentTask;
    ScriptC_brightness _currentScript; // TESTOWO

    private class RenderScriptTask extends AsyncTask<Float, Void, Integer>{
        Boolean _issuedImg = false; // ukonczone zdjecie

        @Override
        protected Integer doInBackground(Float... values) {
            int index = -1;
            if(!isCancelled()){
                _issuedImg = true;
                index = _currentBitmap;

                _currentScript.set_brightness_value((float)(values[0]/255.0f));
//                _currentScript.set_brightness_value(0.78f);
                _currentScript.forEach_brightness(_inAllocation,_outAllocations[index
                        ]);
                _outAllocations[index].copyTo(_outputBitmaps[index]);
                _currentBitmap = (_currentBitmap + 1) % BITMAPS_COUNT;
            }
            return index;
        }

        void UpdateView(Integer result) {
            if (result != -1) {
                // Request UI update - dac GLIDE
                _zoomPinchImageView.SetBitmap(_outputBitmaps[result]);
            }
        }

        protected void onPostExecute(Integer result) {
            UpdateView(result);
        }

        protected void onCancelled(Integer result) {
            if (_issuedImg) {
                UpdateView(result);
            }
        }
    }

    public void UpdateImage(final float f) {
        if (_currentTask != null) {
            _currentTask.cancel(false);
        }
        _currentTask = new RenderScriptTask();
        _currentTask.execute(f);
    }
    // przemysleć czy alokacja powinna byc jako atrybut klasy
    // zrefaktoryzowac

    public FediCore(Context context, Bitmap inputBitmap, ZoomPinchImageView view){
        _inputBitmap = inputBitmap;
        _zoomPinchImageView = view;
        _outputBitmaps = new Bitmap[BITMAPS_COUNT];
        for(int i=0; i<BITMAPS_COUNT; ++i){
            _outputBitmaps[i] = Bitmap.createBitmap(_inputBitmap.getWidth(),
                    _inputBitmap.getHeight(), _inputBitmap.getConfig());
        }
        /// GLIDE
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        _outputBitmaps[_currentBitmap].compress(Bitmap.CompressFormat.JPEG, 100, stream);
        Glide.with(context)
                .load(stream.toByteArray())
                .asBitmap()
                .diskCacheStrategy( DiskCacheStrategy.NONE )
                .skipMemoryCache( true )
                .into(_zoomPinchImageView);
        //_zoomPinchImageView.setImageBitmap(_outputBitmaps[_currentBitmap]);
        _currentBitmap += (_currentBitmap+1)%BITMAPS_COUNT;
    }


    public void CreateRenderscript(Context context, String filterType){
        _renderScript = RenderScript.create(context);

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocations = new Allocation[BITMAPS_COUNT];
        for (int i = 0; i < BITMAPS_COUNT; ++i) {
            _outAllocations[i] = Allocation.createFromBitmap(_renderScript, _outputBitmaps[i]);
        }

        //_currentScript = (ScriptC_brightnessRenderscriptFactory.CreateScript(_renderScript, filterType);
        _currentScript = new ScriptC_brightness(_renderScript);
    }

    // ************************************************************************** sprawdzic


    void DestroyObjects(){
        _renderScript.destroy();
        _inAllocation.destroy();
        _outAllocation.destroy();
    }
//wywalic context
    public Bitmap Brightness(Context context, Bitmap image, float brightness_val){

        ScriptC_brightness brightness = new ScriptC_brightness(_renderScript);
        brightness.set_brightness_value((float)(brightness_val/255.0f));    // prog ma byc ze slidera

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        brightness.forEach_brightness(_inAllocation, _outAllocation);

        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        brightness.destroy();

        return _outputBitmap;
    }

    public Bitmap Blur(Context context, Bitmap image) {
        _width = image.getWidth();
        _height = image.getHeight();

        _inputBitmap = Bitmap.createScaledBitmap(image, _width, _height, false);
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        _renderScript = RenderScript.create(context);

        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(_renderScript, Element.U8_4(_renderScript));
        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        intrinsicBlur.setRadius(BLUR_RADIUS);
        intrinsicBlur.setInput(_inAllocation);
        intrinsicBlur.forEach(_outAllocation);
        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        intrinsicBlur.destroy();

        return _outputBitmap;
    }
    public Bitmap GrayScaleAvg(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);

        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        ScriptC_grayscale_average grayscale_average = new ScriptC_grayscale_average(_renderScript);

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        grayscale_average.forEach_grayscale_average(_inAllocation,_outAllocation);
        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        grayscale_average.destroy();

        return _outputBitmap;
    }

    public Bitmap GrayScaleYUV(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);

        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        ScriptC_grayscale_yuv grayscale_yuv = new ScriptC_grayscale_yuv(_renderScript);

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        grayscale_yuv.forEach_grayscale_yuv(_inAllocation,_outAllocation);
        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        grayscale_yuv.destroy();

        return _outputBitmap;
    }

    public Bitmap Invert(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);

        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(image);

        ScriptC_invert invert = new ScriptC_invert(_renderScript);

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        invert.forEach_invert(_inAllocation,_outAllocation);
        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        invert.destroy();

        return _outputBitmap;
    }

    //dodać parametr ze slidera - natezenie swiatla i slidery
    public Bitmap Bloom(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);

        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(_renderScript, Element.U8_4(_renderScript));
        ScriptIntrinsicBlend blend = ScriptIntrinsicBlend.create(_renderScript, Element.U8_4(_renderScript));
        ScriptC_bloom bloom = new ScriptC_bloom(_renderScript);

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        bloom.set_brightTreshold(0.15f);
        bloom.forEach_bloom_bright_pass(_inAllocation, _outAllocation);

        blur.setRadius(25.f);
        blur.setInput(_inAllocation);
        blur.forEach(_outAllocation);

        blend.forEachAdd(_inAllocation, _outAllocation);

        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        blur.destroy();
        blend.destroy();
        bloom.destroy();

        return _outputBitmap;
    }

    public Bitmap Sepia(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);

        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        ScriptC_sepia sepia = new ScriptC_sepia(_renderScript);

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        sepia.forEach_sepia(_inAllocation, _outAllocation);

        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        sepia.destroy();

        return _outputBitmap;
    }
//!!!!!!!!!!!!!!!!!!! PAMIETAC O NORMALIZACJI lub JEJ NIE UZYWAC1!!!
//!!!!!!!!!!!!!!!!!!! PAMIETAC O RZUTOWANIU DO FLOAT PRZY NORMALIZACJI lub JEJ NIE UZYWAC1!!!

    public Bitmap Treshhold(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);
        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        ScriptC_treshold treshold = new ScriptC_treshold(_renderScript);
        treshold.set_treshold_value(((float)128/(float)255));    // prog ma byc ze slidera

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        treshold.forEach_treshold(_inAllocation, _outAllocation);

        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        treshold.destroy();

        return _outputBitmap;
    }



    public Bitmap Contrast(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);
        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        ScriptC_contrast contrast = new ScriptC_contrast(_renderScript);
        contrast.set_contrast_value((float)-50/(float)255);    // kontrast : -255 : 255 ->  normalizować -1 : 1

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        contrast.forEach_contrast(_inAllocation, _outAllocation);

        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        contrast.destroy();

        return _outputBitmap;
    }

    public Bitmap Saturation(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);
        _inputBitmap = image;
        _outputBitmap = Bitmap.createBitmap(_inputBitmap);

        ScriptC_saturation saturation = new ScriptC_saturation(_renderScript);
        saturation.set_saturation_value((float)372/(float)255);    // kontrast : -255 : 255 ->  normalizować -1 : 1

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        saturation.forEach_saturation(_inAllocation, _outAllocation);

        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        saturation.destroy();

        return _outputBitmap;
    }
}
