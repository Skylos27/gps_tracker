package com.example.gps_tracker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

public class GraphView extends View {
    private int points;

    public GraphView(Context context) {
        super(context);
        init(null, 0);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        //Set the background color.
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                setBackgroundColor(Color.BLACK);

            case Configuration.UI_MODE_NIGHT_NO:
                setBackgroundColor(Color.WHITE);
        }
        //setBackgroundColor(Color.BLACK);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }//init()

    private void invalidateTextPaintAndMeasurements() {

    }//invalidateTextPaintAndMeasurements()


    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();

        int contentWidth = getWidth() - 2*paddingLeft;
        int contentHeight = getHeight() - 2*paddingLeft;

        //Bounds of squares to be drawn.
        int rectBounds = contentWidth- paddingLeft;

        //Side length of the square.
        int sideLength = rectBounds - 10 ;

        canvas.drawLine(100, contentHeight-50, 100, 100, paint);
        canvas.drawLine(100, contentHeight-50, contentWidth-50, contentHeight-50, paint);
        int yaxis = 0;
        int xaxis = 0;
        for (int i = 0; i <= 10 ; i++){
            canvas.drawLine(100, contentHeight-50-yaxis, 110, contentHeight-50-yaxis, paint);
            yaxis += (contentHeight-100)/11;
        }
        long totaltime = MainActivity.totaltime;
        if (totaltime <60) {
            for (int i = 0; i <= totaltime ; i++){

                if (xaxis< contentWidth-50-paddingLeft) {
                    if (i % 10 != 0)
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 60, paint);
                    else
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 70, paint);
                }
                xaxis += (contentWidth-100-paddingLeft  ) / (totaltime+1);
            }

        }
        else if (totaltime<600 ) {
            for (int i = 0; i <= totaltime ; i+=20){

                if (xaxis< contentWidth-50-paddingLeft) {
                    if (i % 20 == 0)
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 70, paint);
                    else if (i % 300 == 0)
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 80, paint);
                }
                xaxis += 20* (contentWidth-100-paddingLeft ) / (totaltime);
            }
        }
        else{
            for (int i = 0; i <= totaltime ; i+=300) {

                if (xaxis< contentWidth-50-paddingLeft) {
                    if (i % 600 == 0)
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 60, paint);
                    else if (i % 300 == 0)
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 50, paint);
                }
                xaxis += 300*(contentWidth-100-paddingLeft ) / (totaltime);


            }
        }

        invalidateTextPaintAndMeasurements();





    }//onDraw()

}
