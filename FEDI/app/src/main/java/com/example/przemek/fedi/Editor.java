package com.example.przemek.fedi;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
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
import android.widget.HorizontalScrollView;
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
import java.util.ArrayList;

/***
 * Klasa aktywności Edytora zdjęć.
 *
 */
///PAMIETAC O ZWALNIANIU PAMIECI W ALOKACJIACH .DESTROY()
public class Editor extends AppCompatActivity {

    static final int REQUEST_CODE = 0, READ_URI_PERMISSION = 1;
    final int ADJUSTMENT_COUNT = 8, DETAILS_COUNT = 7, FILTERS_COUNT = 15, WHITE_BALANCE_COUNT = 2, ROTATIONS_COUNT = 4,
            GRAYSCALE_COUNT = 6, NATURALFILTERS_COUNT = 5, NOISE_COUNT = 4, BLUR_COUNT = 3, HISTOGRAM_COUNT = 3;
    final String[] _adjustmentValues = {"Jasność", "Kontrast", "Nasycenie", "Gamma", "Wypełnienie światłem", "Prześwietlenia", "Cienie", "Temperatura"};
    final String[] _detailsValues = {"Proste wyostrzanie", "Unsharp mask", "Maksimum", "Minimum", "Roberts", "Sobel", "Płaskorzeźba"};
    final String[] _filtersValues = {"Wypełnienie światłem", "Winietowanie", "Negatyw", "Solaryzacja",
            "Sepia", "Bloom", "Soft glow", "Mozaika", "Farba olejna", "Zamiana kanału",
            "Kwantyzacja", "Poruszenie", "Czarne światło", "Kropkowanie", "Progowanie"};
    final String[] _whiteBalanceValues = {"Temperatura", "Odcień"};
    final String[] _rotationValues = {"90 lewo", "90 prawo", "Przerzuć pion", "Przerzuć poziom"};
    final String[] _grayscalesValues = {"Średnia", "Luminancja", "Desaturacja", "Dekompozycja", "1-Kanał", "N-Szarości"};
    final String[] _naturalFiltersValues = {"Ogień", "Lód", "Woda", "Ziemia", "Atmosfera"};
    final String[] _noiseFiltersValues = {"Szum", "Szum pieprz i sól", "Odszumianie: średnia", "Odszumianie: mediana"};
    final String[] _blurFiltersValues = {"Rozmycie", "Rozmycie: średnia", "Rozmycie: mediana"};
    final String[] _histogramFiltersValues = {"Histogram","Wyrównanie","Rozciągnięcie"};


    //    FediCore _coreOperation;
    final int NUM_BITMAPS = 2, UNSHARP_ALLOCS = 4;
    int _currentBitmap = 0;
    Bitmap[] _bitmapsOut, _tmpBitmaps;
    Allocation _inAllocation, _orgImageAlloc, _unsharpBlurAlloc, _unsharpContrastAlloc, _unsharpDiffAllc;
    Allocation[] _outAllocations;

    //**************** przemyslec
    ScriptC_brightness _rsBrightness;
    ScriptC_saturation _rsSaturation;
    ScriptC_contrast _rsContrast;
    ScriptC_grayscale_average _rsGSAverage;
    ScriptC_grayscale_yuv _rsGSyuv;
    ScriptC_grayscale_desaturation _rsGSDestaturation;
    ScriptC_grayscale_decomposition _rsGSDecomposition;
    ScriptC_grayscale_onechannel _rsGSOnechannel;
    ScriptC_grayscale_xlevels _rsGSlevels;
    ScriptC_invert _rsInvert;
    ScriptC_sepia _rsSepia;
    ScriptC_treshold _rsTreshold;
    ScriptIntrinsicBlur _rsBlur;
    ScriptIntrinsicBlend _rsBlend;
    ScriptC_color_shift _rsColorShift;
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
    ScriptC_blacklight_filter _rsBlackLight;
    ScriptC_gamma_correction _rsGammaCor;
    ScriptC_solarization _rsSolarization;
    ScriptC_sharp _rsSharp;
    ScriptC_simple_dithering _rsDithering;
    ScriptC_quantization _rsQuantization;
    ScriptC_mosaic_filter _rsMosaic;
    ScriptC_oilpainting_filter _rsOilPaint;
    ScriptC_fill_pattern _rsFillPat;
    ScriptC_mist_filter _rsMist;
    ScriptC_vignette_filter _rsVignette;
    ScriptC_soft_glow _rsSoftGlow;
    ScriptC_equalize_histogram _rsHistogramEq;
    ScriptC_stretch_histogram _rsHistogramSt;
    ScriptC_unsharp_mask _rsUnsharpMask;
    ScriptC_flip_filter _rsFlip;
    ScriptC_min_max_filter _rsMinmax;
    ScriptC_edges_filters _rsEdges;
    ScriptC_emboss_relief_filter _rsEmbossRel;
    ScriptC_noise_filters _rsNoise;
    ScriptC_denoise_filters _rsDenoise;
    //**************************
    RenderScriptTask _currentTask;
    RenderScript rs;

    Bitmap _inputBitmap, _resultBitmap;


    //tablice: dopasowań, detali, filtrów, balansu bieli
    Button[] _adjustmentsButtonsList, _detailsButtonList, _filtersButtonList, _wbButtonList, _rotationButtonList,
    _natururalFiltButtonList, _grayScaleButtonList, _noiseButtonList, _blurButtonList, _histogramButtonList;
    //stringi: obecnie wcisniety, poprzednio wcisniety (przycisk), etykieta przy sliderze
    String _currBottomButton, _prevBottomButton = "", _optionsLabel;
    String _currOptionButton, _prevOptionButton = "";
    String _processedUriStr, _currentUriStr;


    LinearLayout _optionsLayout, _sliderOptLayout;
    TextView _optSliderText;
    SeekBar _optSlider;
    // okno alertow
    AlertDialog.Builder _builder;

    // widoki zdjecia
    //ImageView _imageView;
    public ZoomPinchImageView _zoomPinchImageView;
    Intent _launchedIntent;

    Uri _imageUri = null, _processedUri = null, _currentUri ;
    boolean _intentHasExtras, _processed, _optChanged = false, _groupChanged = false;
    boolean _doubleBackToExitPressedOnce, _uiShowed, _previewDisabled = true, _imgFulScreenDisabled = true;

    int _initCounter = 0, _changesInCategories = 0;
    ArrayList<Bitmap> _history = new ArrayList<>();
    MenuItem _saveItem;

    LinearLayout _topMenu, _bottoMenu;
    HorizontalScrollView _scrollPanel;

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
                index = _currentBitmap;

                if(_rsKernel.equals("Jasność")){
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
                else if(_rsKernel.equals("Średnia")){
                    _rsGSAverage.forEach_grayscale_average(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Luminancja")){
                    _rsGSyuv.forEach_grayscale_yuv(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Bloom")){
                    // poprawic tego blooma - nie dziala zmianaspolczynnika swiatala;  wspolcznnik dobrany doswiadczalnie
                    _rsBloom.set_brightTreshold(values[0]/100.0f + 0.25f);
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
                else if(_rsKernel.equals("Czarne światło")){
                    _rsBlackLight.set_filter_strenght(values[0]);
                    _rsBlackLight.forEach_blacklight_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Desaturacja")){
                    _rsGSDestaturation.forEach_grayscale_desaturation(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Dekompozycja")){
                    _rsGSDecomposition.set_decomposition_type(values[0].intValue());
                    _rsGSDecomposition.forEach_grayscale_desaturation(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("1-Kanał")){
                    _rsGSOnechannel.set_channel(values[0].intValue());
                    _rsGSOnechannel.forEach_grayscale_desaturation(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("N-Szarości")){
                    _rsGSlevels.set_levels(values[0].intValue());
                    _rsGSlevels.forEach_grayscale_xlevels(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Zamiana kanału")){
                    _rsColorShift.set_shift(values[0].intValue());
                    _rsColorShift.forEach_color_shift(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Gamma")){
                    _rsGammaCor.set_gamma(values[0]);
                    _rsGammaCor.forEach_color_shift(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Solaryzacja")){
                    _rsSolarization.set_treshold(values[0]);
                    _rsSolarization.forEach_solarization(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Proste wyostrzanie")){
                    _rsSharp.set_img_in(_inAllocation);
                    _rsSharp.set_width(_inputBitmap.getWidth());
                    _rsSharp.set_height(_inputBitmap.getHeight());
                    _rsSharp.set_intensity(values[0].intValue());
                    _rsSharp.forEach_sharp(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Kropkowanie")){
                    _rsDithering.forEach_simple_dithering(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Kwantyzacja")){
                    _rsQuantization.set_treshold(values[0].intValue()+1);
                    _rsQuantization.forEach_quantization(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Mozaika")){
                    _rsMosaic.set_img_in(_inAllocation);
                    _rsMosaic.set_size(values[0].intValue()+1);
                    _rsMosaic.forEach_mosaic(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Farba olejna")){
                    _rsOilPaint.set_img_in(_inAllocation);
                    _rsOilPaint.set_width(_inputBitmap.getWidth());
                    _rsOilPaint.set_height(_inputBitmap.getHeight());
                    _rsOilPaint.set_size(values[0].intValue()+1);
                    _rsOilPaint.forEach_oilpainting_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Wypełnienie światłem")){
                    _rsFillPat.set_img_in(_inAllocation);
                    _rsFillPat.set_width(_inputBitmap.getWidth());
                    _rsFillPat.set_height(_inputBitmap.getHeight());
                    _rsFillPat.set_intensity(values[0]);
                    _rsFillPat.forEach_fill_pattern(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Poruszenie")){
                    _rsMist.set_img_in(_inAllocation);
                    _rsMist.set_width(_inputBitmap.getWidth());
                    _rsMist.set_height(_inputBitmap.getHeight());
                    _rsMist.forEach_mist_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Winietowanie")){
                    _rsVignette.set_img_in(_inAllocation);
                    _rsVignette.set_width(_inputBitmap.getWidth());
                    _rsVignette.set_height(_inputBitmap.getHeight());
                    _rsVignette.set_size(values[0]);
                    _rsVignette.forEach_vignette_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Soft glow")){
                    _rsSoftGlow.set_img_in(_inAllocation);
                    _rsSoftGlow.set_width(_inputBitmap.getWidth());
                    _rsSoftGlow.set_height(_inputBitmap.getHeight());
                    _rsSoftGlow.forEach_soft_glow(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Wyrównanie")){
                    _rsHistogramEq.set_size(_inputBitmap.getHeight() * _inputBitmap.getWidth());
                    _rsHistogramEq.invoke_setup();
                    _rsHistogramEq.forEach_equalize_histogram_rgbyuv(_inAllocation, _outAllocations[index]);
                    _rsHistogramEq.invoke_equalize_y_histogram();
                    _rsHistogramEq.forEach_equalize_histogram_yuvrgb(_outAllocations[index], _inAllocation);
                    _outAllocations[index] = _inAllocation;
                }
                else if(_rsKernel.equals("Rozciągnięcie")){
                    _rsHistogramSt.set_img_in(_inAllocation);
                    _rsHistogramSt.set_img_out(_outAllocations[index]);
                    _rsHistogramSt.invoke_setup();
                    _rsHistogramSt.forEach_stretch_histogram(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Unsharp mask")){
                    _rsBlur.setRadius(5.0f);

                    _orgImageAlloc.copyFrom(_inAllocation);

                    _rsBlur.setInput(_orgImageAlloc);
                    _rsBlur.forEach(_outAllocations[index]);

                    _unsharpBlurAlloc.copyFrom(_outAllocations[index]);

                    _rsUnsharpMask.set_blurred_img(_unsharpBlurAlloc);
                    _rsUnsharpMask.forEach_unsharp_mask(_inAllocation, _outAllocations[index]);

                    _unsharpDiffAllc.copyFrom(_outAllocations[index]);
                    _rsContrast.set_contrast_value(0.7f);

                    _rsContrast.forEach_contrast(_inAllocation, _outAllocations[index]);
                    _unsharpContrastAlloc.copyFrom(_outAllocations[index]);
                    _rsUnsharpMask.set_org_img(_inAllocation);
                    _rsUnsharpMask.set_unsharp_img(_unsharpDiffAllc);
                    _rsUnsharpMask.set_threshold(values[0]);
                    _rsUnsharpMask.forEach_unsharp_mask_mix(_unsharpContrastAlloc, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Przerzuć poziom")){
                    _rsFlip.set_img_in(_inAllocation);
                    _rsFlip.invoke_setup();
                    _rsFlip.set_direction(0);
                    _rsFlip.forEach_flip_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Przerzuć pion")){
                    _rsFlip.set_img_in(_inAllocation);
                    _rsFlip.invoke_setup();
                    _rsFlip.set_direction(1);
                    _rsFlip.forEach_flip_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("90 lewo")){
                    _rsFlip.set_img_in(_inAllocation);
                    _rsFlip.invoke_setup();
                    _rsFlip.set_direction(2);
                    _rsFlip.forEach_flip_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("90 prawo")){
                    _rsFlip.set_img_in(_inAllocation);
                    _rsFlip.invoke_setup();
                    _rsFlip.set_direction(3);
                    _rsFlip.forEach_flip_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Maksimum")){
                    _rsMinmax.set_img_in(_inAllocation);
                    _rsMinmax.invoke_setup();
                    _rsMinmax.set_size(values[0].intValue());
                    _rsMinmax.set_type(0);
                    _rsMinmax.forEach_min_max_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Minimum")){
                    _rsMinmax.set_img_in(_inAllocation);
                    _rsMinmax.invoke_setup();
                    _rsMinmax.set_size(values[0].intValue());
                    _rsMinmax.set_type(1);
                    _rsMinmax.forEach_min_max_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Roberts")){
                    _rsEdges.set_img_in(_inAllocation);
                    _rsEdges.invoke_setup();
                    _rsEdges.forEach_robers_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Sobel")){
                    _rsEdges.set_img_in(_inAllocation);
                    _rsEdges.invoke_setup();
                    _rsEdges.forEach_sobel_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Płaskorzeźba")){
                    _rsEmbossRel.set_img_in(_inAllocation);
                    _rsEmbossRel.invoke_setup();
                    _rsEmbossRel.set_mask_type(values[0].intValue());
                    _rsEmbossRel.forEach_emboss_relief_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Szum pieprz i sól")){
                    _rsNoise.set_img_in(_inAllocation);
                    _rsNoise.invoke_setup();
                    _rsNoise.set_threshold(values[0]);
                    _rsNoise.forEach_salt_pepper_noise(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Szum")){
                    _rsNoise.set_img_in(_inAllocation);
                    _rsNoise.invoke_setup();
                    _rsNoise.set_threshold(values[0]);
                    _rsNoise.forEach_homogeneous_noise(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Odszumianie: średnia") || _rsKernel.equals("Rozmycie: średnia") ){
                    _rsDenoise.set_img_in(_inAllocation);
                    _rsDenoise.invoke_setup();
                    _rsDenoise.set_size(values[0].intValue());
                    _rsDenoise.forEach_average_filter(_inAllocation, _outAllocations[index]);
                }
                else if(_rsKernel.equals("Odszumianie: mediana") || _rsKernel.equals("Rozmycie: mediana")){
                    _rsDenoise.set_img_in(_inAllocation);
                    _rsDenoise.invoke_setup();
                    _rsDenoise.set_size(values[0].intValue());
                    _rsDenoise.forEach_median_filter(_inAllocation, _outAllocations[index]);
                }
                _outAllocations[index].copyTo(_bitmapsOut[index]);
                _resultBitmap = _bitmapsOut[index];
                _currentBitmap = (_currentBitmap + 1) % NUM_BITMAPS;

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
                    finish();
                    startActivity(new Intent(Editor.this, Editor.class));
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };

    DialogInterface.OnClickListener _saveClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    _saveItem.setIcon(R.mipmap.save);
                    if(_resultBitmap != null)
                        SaveBitmap(_resultBitmap);
                    else
                        ShowAlert("Nie wprowadzono zmian!",_confirmListener,false);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    _saveItem.setIcon(R.mipmap.save);
                    break;
            }
        }
    };

    DialogInterface.OnClickListener _changeOptionListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            _changesInCategories = 0;
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    if(_optChanged && _processed){
                        //Toast.makeText(getApplicationContext(), "PRZETWORZONE  + zmiana", Toast.LENGTH_LONG).show();
                        ApplyBitmapChanges(getApplicationContext());
                    }
                    else if(_processed){
                        //Toast.makeText(getApplicationContext(), "PRZETWORZONE", Toast.LENGTH_LONG).show();
                        ApplyBitmapChanges(getApplicationContext());

                    }
                    else {
                        Toast.makeText(getApplicationContext(), "NIE PRZETWORZONE", Toast.LENGTH_LONG).show();
                    }

                    if(_resultBitmap != null){
                        _processedUriStr = _currentUriStr;
                        _processedUri = Uri.parse(_processedUriStr);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "DUPA", Toast.LENGTH_LONG).show();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    if(_processedUriStr != null){
                        _imageUri = (_processedUriStr.length() > 0) ? Uri.parse(_processedUriStr) : _imageUri;
                    }
                    try {
                        _inputBitmap = GetBitmapFromUri(_imageUri);
                        _resultBitmap = Bitmap.createBitmap(_inputBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    _zoomPinchImageView.SetBitmap(_inputBitmap);
                    break;
            }
        }
    };

    DialogInterface.OnClickListener _confirmListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    break;
            }
        }
    };

    void CreateScript() {
        RenderScript rs = RenderScript.create(this);
// UWAZAC TUTAJ s
        _inAllocation = Allocation.createFromBitmap(rs, _inputBitmap);
        // to unsharp mask
        _orgImageAlloc = Allocation.createFromBitmap(rs, _tmpBitmaps[0]);
        _unsharpBlurAlloc = Allocation.createFromBitmap(rs, _tmpBitmaps[1]);
        _unsharpContrastAlloc = Allocation.createFromBitmap(rs, _tmpBitmaps[2]);
        _unsharpDiffAllc = Allocation.createFromBitmap(rs, _tmpBitmaps[3]);
        /// maska wyostrzajaca
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
        _rsGSDestaturation =  new ScriptC_grayscale_desaturation(rs);
        _rsGSDecomposition = new ScriptC_grayscale_decomposition(rs);
        _rsGSOnechannel = new ScriptC_grayscale_onechannel(rs);
        _rsGSlevels = new ScriptC_grayscale_xlevels(rs);
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
        _rsBlackLight = new ScriptC_blacklight_filter(rs);
        _rsColorShift = new ScriptC_color_shift(rs);
        _rsGammaCor = new ScriptC_gamma_correction(rs);
        _rsSolarization = new ScriptC_solarization(rs);
        _rsSharp = new ScriptC_sharp(rs);
        _rsDithering = new ScriptC_simple_dithering(rs);
        _rsQuantization = new ScriptC_quantization(rs);
        _rsMosaic = new ScriptC_mosaic_filter(rs);
        _rsOilPaint = new ScriptC_oilpainting_filter(rs);
        _rsFillPat = new ScriptC_fill_pattern(rs);
        _rsMist = new ScriptC_mist_filter(rs);
        _rsVignette = new ScriptC_vignette_filter(rs);
        _rsSoftGlow = new ScriptC_soft_glow(rs);
        _rsHistogramEq = new ScriptC_equalize_histogram(rs);
        _rsHistogramSt = new ScriptC_stretch_histogram(rs);
        _rsUnsharpMask = new ScriptC_unsharp_mask(rs);
        _rsFlip = new ScriptC_flip_filter(rs);
        _rsMinmax = new ScriptC_min_max_filter(rs);
        _rsEdges = new ScriptC_edges_filters(rs);
        _rsEmbossRel = new ScriptC_emboss_relief_filter(rs);
        _rsNoise = new ScriptC_noise_filters(rs);
        _rsDenoise = new ScriptC_denoise_filters(rs);
    }


    void UpdateImage(final float f) {
        if (_currentTask != null) {
            _currentTask.cancel(false);
        }
        _currentTask = new RenderScriptTask(_optionsLabel);
        _currentTask.execute(f);
    }

    void SetOptionSlider(int progress, int max, float imageValue){
        _sliderOptLayout.setVisibility(View.VISIBLE);
        _optSlider.setProgress(progress);
        _optSlider.setMax(max);
        UpdateImage(imageValue);
    }

    View.OnClickListener btnClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ++_changesInCategories;
            _optionsLabel = ((Button) v).getText().toString();
            ResetSliderLayout();
            _currOptionButton = _optionsLabel;
            InitRenderScriptOps();
            CreateScript();

            if(_optionsLabel.equals("Jasność") || _optionsLabel.equals("Kontrast") || _optionsLabel.equals("Odcień")||
                    _optionsLabel.equals("Temperatura") || _optionsLabel.equals("Prześwietlenia") || _optionsLabel.equals("Cienie")){

                SetOptionSlider(100,200,0.0f);
            }
            else if(_optionsLabel.equals("1-Kanał") || _optionsLabel.equals("N-Szarości") || _optionsLabel.equals("Zamiana kanału") ||
                    _optionsLabel.equals("Proste wyostrzanie")){
                SetOptionSlider(0,2,0.0f);
            }
            else if(_optionsLabel.equals("Rozmycie") || _optionsLabel.equals("Bloom")){
                SetOptionSlider(0,24,1.0f);
            }
            else if(_optionsLabel.equals("Wypełnienie światłem") || _optionsLabel.equals("Poruszenie") || _optionsLabel.equals("Solaryzacja") ||
                    _optionsLabel.equals("Winietowanie") || _optionsLabel.equals("Unsharp mask") ||  _optionsLabel.equals("Szum pieprz i sól") ||
                    _optionsLabel.equals("Szum")){
                SetOptionSlider(0,100,0.0f);
            }
            else if(_optionsLabel.equals("Nasycenie")){
                SetOptionSlider(100,200,1.0f);
            }
            else if(_optionsLabel.equals("Progowanie")){
                SetOptionSlider(50,100,0.5f);
            }
            else if(_optionsLabel.equals("Czarne światło")){
                SetOptionSlider(0,6,1.0f);
            }
            else if(_optionsLabel.equals("Dekompozycja") ||  _optionsLabel.equals("Odszumianie: mediana") ||
                    _optionsLabel.equals("Rozmycie: mediana")) {
                SetOptionSlider(0,1,0.0f);
            }
            else if(_optionsLabel.equals("Gamma")){
                SetOptionSlider(100,798,1.0f);
            }
            else if(_optionsLabel.equals("Kwantyzacja")){
                SetOptionSlider(0,31,1.0f);
            }
            else if(_optionsLabel.equals("Mozaika")){
                SetOptionSlider(0,100,1.0f);
            }
            else if(_optionsLabel.equals("Farba olejna")){
                SetOptionSlider(0,50,1.0f);
            }
            else if(_optionsLabel.equals("Minimum") || _optionsLabel.equals("Maksimum")){
                SetOptionSlider(0,4,0.0f);
            }
            else if(_optionsLabel.equals("Odszumianie: średnia") || _optionsLabel.equals("Rozmycie: średnia") ){
                SetOptionSlider(0,2,0.0f);
            }
            else if(_optionsLabel.equals("Płaskorzeźba")){
                SetOptionSlider(0,12,0.0f);
            }
            else if(_optionsLabel.equals("Histogram")){
                ShowAlert("Obecnie niedostępne.", _confirmListener, false);
            }
            else{
                _processed = true;
                UpdateImage(0.0f);
                _sliderOptLayout.setVisibility(View.INVISIBLE);
            }

        }
    };

    public Uri GetImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    SeekBar.OnSeekBarChangeListener sbValueChange = new SeekBar.OnSeekBarChangeListener(){
        float value;
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if(_optionsLabel.equals("Jasność") || _optionsLabel.equals("Kontrast")){
                value = (((float)(progress - 100))/100.0f);
            }
            else if(_optionsLabel.equals("Nasycenie") || _optionsLabel.equals("Wypełnienie światłem") || _optionsLabel.equals("Progowanie") ||
                    _optionsLabel.equals("Solaryzacja") || _optionsLabel.equals("Winietowanie") ||
                    _optionsLabel.equals("Unsharp mask") || _optionsLabel.equals("Szum pieprz i sól") || _optionsLabel.equals("Szum")){
                value = (float)progress/100.0f;
            }
            else if(_optionsLabel.equals("Kwantyzacja") || _optionsLabel.equals("Mozaika") || _optionsLabel.equals("Farba olejna") ||
                _optionsLabel.equals("Rozmycie") || _optionsLabel.equals("Bloom") ){
                value = progress+1;
            }
            else if(_optionsLabel.equals("Czarne światło") || _optionsLabel.equals("Dekompozycja") || _optionsLabel.equals("1-Kanał") ||
                    _optionsLabel.equals("N-Szarości") || _optionsLabel.equals("Zamiana kanału") || _optionsLabel.equals("Proste wyostrzanie") ||
                    _optionsLabel.equals("Minimum") || _optionsLabel.equals("Maksimum") || _optionsLabel.equals("Płaskorzeźba") ||
                    _optionsLabel.equals("Odszumianie: średnia") || _optionsLabel.equals("Odszumianie: mediana") ||
                    _optionsLabel.equals("Rozmycie: średnia") || _optionsLabel.equals("Rozmycie: mediana")){
                value = progress;
            }
            else if(_optionsLabel.equals("Odcień") || _optionsLabel.equals("Temperatura")){
                value = (((float)((progress - 100)>>1))/100.0f);
            }
            else if(_optionsLabel.equals("Prześwietlenia") || _optionsLabel.equals("Cienie")){
                value = (((float)((progress - 100)>>2))/100.0f);
            }
            else if(_optionsLabel.equals("Gamma")){
                value = ((float)(progress+1)/100.0f);
            }
            UpdateImage(value);
            _optSliderText.setText(_optionsLabel+" "+value);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            _processed = true;
        }
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

        _uiShowed = false;
        _scrollPanel = (HorizontalScrollView)findViewById(R.id.bottomScrollPanel);
        _topMenu = (LinearLayout) findViewById(R.id.topMenuPanel);
        _bottoMenu = (LinearLayout)findViewById(R.id.bottomMenuLayout);
        _zoomPinchImageView = (ZoomPinchImageView)findViewById(R.id.zoomPinchImageView);
        CheckActivity();
        InitOptionsBar();
        InitSliderListener();
    }

    void ShowUI(){
        _topMenu.setVisibility((_uiShowed) ? View.VISIBLE : View.INVISIBLE);
        _bottoMenu.setVisibility((_uiShowed) ? View.VISIBLE : View.INVISIBLE);
        _scrollPanel.setVisibility((_uiShowed) ? View.VISIBLE : View.INVISIBLE);
        _uiShowed = !_uiShowed;
    }

    @Override
    public void onBackPressed() {
        if (_doubleBackToExitPressedOnce) {
            super.onBackPressed();
            finish();
            startActivity(new Intent(Editor.this, MainMenu.class));
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

    void ChangeMenuItemIcon(MenuItem item, int icon){
        item.setIcon(icon);
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
            case R.id.action_preview:
                ShowUI();
                _previewDisabled = !_previewDisabled;
                try{
                    if(_previewDisabled){

                        //sprawdzac czy obraz byl przetworzony bo inaczej krasz
                        item.setIcon(R.mipmap.review);
                        if(_resultBitmap !=null){
                            _inputBitmap = GetBitmapFromUri(GetImageUri(this,_resultBitmap));
                        }

                    }
                    else{
                        if(_resultBitmap == null){
                            ShowAlert("Zdjęcie nie zostało wcześniej zmodyfikowane.", _confirmListener, false);
                        }
                        item.setIcon(R.mipmap.review_on);
                        _inputBitmap = GetBitmapFromUri(_imageUri);
                    }
                    _zoomPinchImageView.SetBitmap(_inputBitmap);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_open:
                ShowAlert("Zmiany zostaną utracone. Kontynuować?",_dialogClickListener, true);
                return true;
            case R.id.action_reset_scale:
                ResetScale();
                return true;
            case R.id.action_save:
                if(_previewDisabled){
                    _saveItem = item;
                    item.setIcon(R.mipmap.save_on);
                    ShowAlert("Czy chcesz zapisać zmiany?", _saveClickListener, true);
                    return true;
                }
            case R.id.action_fullscreen:
                if(_previewDisabled){
                    _imgFulScreenDisabled = !_imgFulScreenDisabled;
                    ChangeMenuItemIcon(item, (_imgFulScreenDisabled) ? R.mipmap.full_screen : R.mipmap.full_screen_on);
                    ShowUI();
                }
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
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK && resultData != null){
            SetImageToView(resultData);
            _optionsLayout.setVisibility(View.INVISIBLE);
            _sliderOptLayout.setVisibility(View.INVISIBLE);
            ResetSliderLayout();
            ResetScale();
        }
        else{
            finish();
            startActivity(new Intent(Editor.this, MainMenu.class));
        }
    }

    void InitRenderScriptOps() {
        try {

            Toast.makeText(getApplicationContext(), "2N "+_currOptionButton + " S " + _prevOptionButton+" OPT "+_optChanged + " PROCS "+_processed + " GRUP "+_groupChanged, Toast.LENGTH_LONG).show();

            if(_initCounter < 1){
                _prevOptionButton = _currOptionButton;
            }

            if(!_currOptionButton.equals(_prevOptionButton)){
                _prevOptionButton = _currOptionButton;
                _optChanged = true;
            }
            else{
                _optChanged = false;
            }

// POPRAWIC TE GOWNO
            if((_optChanged && _processed)){
                ApplyBitmapChanges(this);
//                _currentUri = GetImageUri(this,_resultBitmap);
//                _currentUriStr = _currentUri.toString();
//                _inputBitmap = GetBitmapFromUri(_currentUri);
            }
            else {
                _inputBitmap = GetBitmapFromUri((_resultBitmap != null ) ? GetImageUri(this,_resultBitmap) : _imageUri);
            }
            //_inputBitmap = GetBitmapFromUri(_imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (_initCounter < 1){
            _bitmapsOut = new Bitmap[NUM_BITMAPS];
            _tmpBitmaps = new Bitmap[UNSHARP_ALLOCS];
            for (int i = 0; i < NUM_BITMAPS; ++i) {
                _bitmapsOut[i] = Bitmap.createBitmap(_inputBitmap.getWidth(),
                        _inputBitmap.getHeight(), _inputBitmap.getConfig());
            }
            for (int i = 0; i < UNSHARP_ALLOCS; ++i) {
                _tmpBitmaps[i] = Bitmap.createBitmap(_inputBitmap.getWidth(),
                        _inputBitmap.getHeight(), _inputBitmap.getConfig());
            }
        }

        // --------------------------------------------------------------------- JCt przesnuesc
        _processed = false;

        _currentBitmap += (_currentBitmap + 1) % NUM_BITMAPS;
        ++_initCounter;

    }


    void ApplyBitmapChanges(Context context){
        _currentUri = GetImageUri(context,_resultBitmap);
        _currentUriStr = _currentUri.toString();
        try {
            _inputBitmap = GetBitmapFromUri(_currentUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //podzielic te metode na inne metody
    void SaveBitmap(Bitmap bmp){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "FEDI");
        File mypath=new File(directory,"FEDI_"+ System.currentTimeMillis() +".jpg");

        FileOutputStream fos = null;
        try {
            if(!directory.exists()){
                directory.mkdirs();
            }
            fos = new FileOutputStream(mypath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                fos.flush();
                fos.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        MediaScannerConnection.scanFile(this,
            new String[] { mypath.getAbsolutePath().toString() }, null,
            new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                }
            });

        ShowAlert("Zapisano pomyślnie!",_confirmListener,false);
    }

    void ChangeImageOrientation(Bitmap bitmap){
        int orientation;
        ExifInterface exifInterface = null;
        Matrix matrix = new Matrix();

        try {
            exifInterface = new ExifInterface(UriConverter.getPath(this, _imageUri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
//            case ExifInterface.ORIENTATION_ROTATE_270:
//                matrix.setRotate(270);
//                break;
            default:
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        _zoomPinchImageView.SetBitmap(rotatedBitmap);
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

    void ShowAlert(String message, DialogInterface.OnClickListener clickListener, boolean manyOptions){
        _builder = new AlertDialog.Builder(this);
        _builder.setCancelable(false);
        if(manyOptions){
            _builder.setMessage(message).setPositiveButton("Tak", clickListener)
                    .setNegativeButton("Anuluj", clickListener).show();
        }
        else{
            _builder.setMessage(message).setPositiveButton("OK", clickListener).show();
        }
    }

    /***
     * Metoda odpowiedzialna za uruchomienie intencji eksploratora plików w celu wybrania zdjęcia to załadowania, do widoku ImageView
     */
    void OpenImageBrowser(){
        _history.clear();
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

            Glide
                    .with( this )
                    .load(_imageUri)
                    .diskCacheStrategy( DiskCacheStrategy.NONE )
                    .skipMemoryCache( true )
                    .into( _zoomPinchImageView);


            try {
                _inputBitmap = GetBitmapFromUri(_imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            _processed = false; // flaga iformujaca o tym czy zdjecie zostalo w jakikolwiek sposob przetworzeone

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

        if(_currBottomButton.equals(_prevBottomButton)){
            _groupChanged = false;
        }
        else{
            if(_prevBottomButton.length() > 0 && _changesInCategories > 0){
                ShowAlert("Zapisać zmiany w grupie \""+_prevBottomButton
                        +"\"?",_changeOptionListener, true);
            }
            _groupChanged = true;
        }

        if(_currBottomButton.equals(_prevBottomButton) && (optionsVisibility==View.VISIBLE)){
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

    void FillOptionsBar(String[] buttonValues, Button[] buttonList, int buttonCount){
        for(int i=0; i<buttonCount; i++){
            buttonList[i] = new Button(this);
            buttonList[i].setTag(buttonValues[i]);
            buttonList[i].setText(buttonValues[i]);
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

    void FillButtons(String curButtonLabel, Button[] buttonList, String[] labels, int count){
        _currBottomButton = curButtonLabel;
        buttonList = new Button[count];
        SetOptionsVisibility(_currBottomButton);
        FillOptionsBar(labels, buttonList ,count);
        //Toast.makeText(getApplicationContext(), "2 GRUP "+_groupChanged, Toast.LENGTH_SHORT).show();
    }

    public void ShowAdjustments(View v){
        FillButtons("Dopasuj", _adjustmentsButtonsList, _adjustmentValues, ADJUSTMENT_COUNT);
    }
    public void ShowNoise(View v){
        FillButtons("Filtry: szum", _noiseButtonList, _noiseFiltersValues, NOISE_COUNT);
    }
    public void ShowDetails(View v){
        FillButtons("Szczegóły", _detailsButtonList, _detailsValues, DETAILS_COUNT);
    }
    public void ShowFilters(View v){
        FillButtons("Filtry: ogólne", _filtersButtonList, _filtersValues, FILTERS_COUNT);
    }
    public void ShowWhiteBalance(View v){
        FillButtons("Balans bieli", _wbButtonList, _whiteBalanceValues, WHITE_BALANCE_COUNT);
    }
    public void ShowRotation(View v){
        FillButtons("Obróć", _rotationButtonList, _rotationValues, ROTATIONS_COUNT);
    }
    public void ShowNaturalFilters(View v){
        FillButtons("Filtry: natura", _natururalFiltButtonList, _naturalFiltersValues, NATURALFILTERS_COUNT);
    }
    public void ShowGrayscaleFilters(View v){
        FillButtons("Filtry: szarości", _grayScaleButtonList, _grayscalesValues, GRAYSCALE_COUNT);
    }
    public void ShowBlurFilters(View v){
        FillButtons("Rozymcie", _blurButtonList, _blurFiltersValues, BLUR_COUNT);
    }
    public void ShowHistogram(View v){
        FillButtons("Histogram", _histogramButtonList, _histogramFiltersValues, HISTOGRAM_COUNT);
    }


    public void SaveAdjustment(View v){
        //DestroyScript();
    }

    public void CancelAdjustment(View v){
//        ChangeImageOrientation(_inputBitmap);
        Toast.makeText(this, "dupaaa  a a  a a ", Toast.LENGTH_LONG).show();
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

    Bitmap GetBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);

        int bmpH = bitmap.getHeight(), bmpW = bitmap.getWidth();
        parcelFileDescriptor.close();

        if(bmpH >= 1800 || bmpW >=1800){
            int newHeight = 1800, newWidth;
            float aspectRatio = (bmpW > bmpH) ? ((float)bmpW/(float)bmpH) : ((float)bmpW/(float)bmpH);
            newWidth = Math.round(newHeight*aspectRatio);
            return  Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        }
        else{
            return bitmap;
        }
    }


    /***
     * Metoda odpowiedzialna za wyświetlenie metadanych ze zdjęcia.
     */
    void ShowImageInfo(){
        Intent info = new Intent(Editor.this, ImageInfo.class);
        info.putExtra("IMAGE_INFO",UriConverter.getPath(this, _imageUri));
        startActivity(info);
    }

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
