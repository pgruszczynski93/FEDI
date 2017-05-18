package com.example.przemek.fedi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.io.IOException;

/**
 * Created by Przemek on 2017-05-14.
 */

public class ZoomPinchImageView extends ImageView {


    // dorobic AscyncTask
    final static float _minZoom = 0.6f, _maxZoom = 3.f;

    Bitmap _bitmap;
    ScaleGestureDetector _scaleGestureDetector;
    int _imageW, _imageH;
    float _scaleFactor = 1.f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    public ZoomPinchImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        _scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleChanger());
    }

    private class ScaleChanger extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            _scaleFactor *= detector.getScaleFactor();
            _scaleFactor = Math.max(_minZoom, Math.min(_maxZoom, _scaleFactor));
            invalidate();
            requestLayout();
            return super.onScale(detector);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int imgWidth, imgHeight, scaledWidth, scaledHeight;
        imgWidth = MeasureSpec.getSize(widthMeasureSpec);
        imgHeight = MeasureSpec.getSize(heightMeasureSpec);

        scaledWidth = Math.round(_imageW * _scaleFactor);
        scaledHeight = Math.round(_imageH * _scaleFactor);

        setMeasuredDimension(Math.min(imgWidth, scaledWidth), Math.min(imgHeight, scaledHeight));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
//        canvas.scale(_scaleFactor,_scaleFactor);
        canvas.scale(_scaleFactor,_scaleFactor, _scaleGestureDetector.getFocusX(), _scaleGestureDetector.getFocusY());
        canvas.drawBitmap(_bitmap,0,0,null);
        canvas.restore();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void SetImgUri(Uri uri){
        try{
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            float aspectRatio = (float)bitmap.getHeight()/(float)bitmap.getWidth();
            DisplayMetrics dispmetr = getResources().getDisplayMetrics();
            _imageW = dispmetr.widthPixels;
            _imageH = Math.round(_imageW*aspectRatio);
            _bitmap = Bitmap.createScaledBitmap(bitmap, _imageW, _imageH, false);
            invalidate();
            requestLayout();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
