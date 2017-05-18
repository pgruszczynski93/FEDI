package com.example.przemek.fedi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
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

    public ZoomPinchImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int imgWidth, imgHeight;
        imgWidth = MeasureSpec.getSize(widthMeasureSpec);
        imgHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(Math.min(imgWidth, _imageW), Math.min(imgHeight, _imageH));

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
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
