package com.example.przemek.fedi;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.*;

/**
 * Created by Przemek on 2017-06-24.
 */

public class FediCore {
    private static final float BLUR_RADIUS = 15f;

    int _width, _height;
    Bitmap _inputBitmap, _outputBitmap;
    RenderScript _renderScript;
    Allocation _inAllocation, _outAllocation;

    // przemysleć czy alokacja powinna byc jako atrybut klasy
    // zrefaktoryzowac

    void DestroyObjects(){
        _renderScript.destroy();
        _inAllocation.destroy();
        _outAllocation.destroy();
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

        _outputBitmap = Bitmap.createBitmap(image);

        ScriptC_grayscale_average grayscale_average = new ScriptC_grayscale_average(_renderScript);

        _inAllocation = Allocation.createFromBitmap(_renderScript, _inputBitmap);
        _outAllocation = Allocation.createFromBitmap(_renderScript, _outputBitmap);

        grayscale_average.forEach_grayscale_average(_inAllocation,_outAllocation);
        _outAllocation.copyTo(_outputBitmap);

        DestroyObjects();
        grayscale_average.destroy();

        return _outputBitmap;
    }

    public Bitmap Invert(Context context, Bitmap image){
        _renderScript = RenderScript.create(context);

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

}
