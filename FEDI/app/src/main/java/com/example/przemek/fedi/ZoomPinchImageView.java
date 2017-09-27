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
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by Przemek on 2017-05-14.
 */

public class ZoomPinchImageView extends ImageView {

    final static float _minZoom = 1.f, _maxZoom = 3.f;
    final static int NONE = 0, PAN = 0, ZOOM = 2;

    Bitmap _bitmap;
    int _imageW, _imageH, _eventState;
    float _scaleFactor = 1.f, _startX, _startY, _translateX, _translateY, _prevTransX, _prevTransY;
    ScaleGestureDetector _scaleGestureDetector;

    public float GetScaleFactor(){
        return _scaleFactor;
    }
    public void SetScaleFactor(float scaleFactor){ _scaleFactor = scaleFactor; }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            _scaleFactor *= detector.getScaleFactor();
            _scaleFactor = Math.max(_minZoom, Math.min(_maxZoom, _scaleFactor));
            return super.onScale(detector);
        }
    }

    public ZoomPinchImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        _scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                _eventState = PAN;
                _startX = event.getX() - _prevTransX;
                _startY = event.getY() - _prevTransY;
                break;
            case MotionEvent.ACTION_UP:
                _eventState = NONE;
                _prevTransX = _translateX;
                _prevTransY = _translateY;
                break;
            case MotionEvent.ACTION_MOVE:
                _translateX = event.getX() - _startX;
                _translateY = event.getY() - _startY;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                _eventState = ZOOM;
                break;
        }
        _scaleGestureDetector.onTouchEvent(event);

        if((_eventState == PAN && _scaleFactor != _minZoom) || _eventState == ZOOM){
            invalidate();
            requestLayout();
        }

        return true;
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
        canvas.scale(_scaleFactor, _scaleFactor);
        if((_translateX * -1) < 0){
            _translateX = 0;
        }
        else if((_translateX * -1) > _imageW*_scaleFactor - getWidth()){
            _translateX = (_imageW * _scaleFactor - getWidth()) * -1;
        }
        if((_translateY * -1) < 0){
            _translateY = 0;
        }
        else if((_translateY * -1) > _imageH*_scaleFactor - getHeight()){
            _translateY = (_imageH * _scaleFactor - getHeight()) * -1;
        }
        canvas.translate(_translateX/_scaleFactor, _translateY/_scaleFactor);
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

    public void SetBitmap(Bitmap bitmap){
        _bitmap =  Bitmap.createScaledBitmap(bitmap, _imageW, _imageH, false);
        invalidate();
        requestLayout();
    }

}
