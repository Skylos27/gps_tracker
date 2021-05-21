package com.example.gps_tracker;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.Random;

public class GraphView extends View {
    private static final String TAG ="" ;
    private int points;
    public static int counter;
    public static Double[][] notcleanList;// = MainActivity.listPoint;
    public static long totaltime;// = MainActivity.totaltime;
    public static double[][] cleanList;
    public static double[][] averageSpeedList;


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
        paint.setTextSize(50);
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();

        int contentWidth = getWidth() - 2*paddingLeft;
        int contentHeight = getHeight() - 2*paddingLeft;


        canvas.drawLine(100, contentHeight-50, 100, 50, paint);
        canvas.drawLine(100, contentHeight-50, contentWidth-50, contentHeight-50, paint);
        int yaxis = 0;
        int xaxis = 0;
        // y axis construct
        for (int i = 0; i <= 10 ; i++){
            canvas.drawLine(100, contentHeight-50-yaxis, 110, contentHeight-50-yaxis, paint);
            if (i>0) canvas.drawText(Integer.toString(i),20 ,contentHeight-50-yaxis ,paint);
            yaxis += (contentHeight-100)/11;
        }
        int legensNumb = 0;
        // x axis construction
        if (totaltime <60) {
            for (int i = 0; i <= totaltime ; i++){

                if (xaxis< contentWidth-50-paddingLeft) {
                    if (i % 10 != 0)
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 60, paint);
                    else {
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 70, paint);
                        canvas.drawText(Integer.toString(legensNumb),100 + xaxis-20,contentHeight ,paint);
                        legensNumb +=10;
                    }
                }
                xaxis += (contentWidth-100-paddingLeft  ) / (totaltime+1);
            }
            averageSpeed(1,canvas);

        }
        // when total time is above 1 min and inferior to 10 min
        else if (totaltime<600 ) {
            for (int i = 0; i <= totaltime ; i+=10){

                if (xaxis< contentWidth-50-paddingLeft) {
                     if (i % 300 == 0) {
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 80, paint);
                        canvas.drawText(Integer.toString(legensNumb%60),100 + xaxis-20,contentHeight ,paint);
                        legensNumb +=1;
                    }
                    else if (i % 60 == 0) {
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 70, paint);
                        canvas.drawText(Integer.toString(legensNumb%60),100 + xaxis-20,contentHeight ,paint);
                        legensNumb +=1;
                    }

                }
                xaxis += 10* (contentWidth-100-paddingLeft ) / (totaltime);
            }
            averageSpeed(10,canvas);
        }


        else{
            int five = 5;
            int ten = 10;
            int hour = 0;
            for (int i = 0; i <= totaltime ; i+=300) {

                if (xaxis< contentWidth-50-paddingLeft) {
                    if (i % 3600 == 0) {
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 70, paint);
                        canvas.drawText(Integer.toString(ten%60),100 + xaxis-20,contentHeight ,paint);
                        hour +=1;
                    }
                    else if (i % 600 == 0) {
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 60, paint);
                        canvas.drawText(Integer.toString(ten%60),100 + xaxis-20,contentHeight ,paint);
                        ten +=10;
                    }
                    else if (i % 300 == 0) {
                        canvas.drawLine(100 + xaxis, contentHeight - 50, 100 + xaxis, contentHeight - 50, paint);
                        canvas.drawText(Integer.toString(ten%60),100 + xaxis-20,contentHeight ,paint);
                        five +=10;
                    }

                }
                xaxis += 300*(contentWidth-100-paddingLeft ) / (totaltime);
                averageSpeed(30,canvas);

            }

        }


        invalidateTextPaintAndMeasurements();


    }//onDraw()

    // Draw the curve of the average speed
    public void averageSpeed(int interval, Canvas canvas){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);

        averageSpeedList = new double[cleanList.length][2];
        int paddingLeft = getPaddingLeft();
        int contentWidth = getWidth() - 2*paddingLeft;
        int contentHeight = getHeight() - 2*paddingLeft;
        int add = (contentHeight-100)/11;
        int inter=0;

        //computing the average speed between 2 position separate by an interval "i" of time
        //for(int i = 0;i<cleanList.length && inter+interval<cleanList.length;i++){
//
        //    if (i == 0 || cleanList[i][3] > 0) {  // condition to avoid some nosedive
        //        averageSpeedList[i][0] = 1000* 3.6* MainActivity.distance(cleanList[inter][0],cleanList[inter][1],cleanList[inter+interval][0],cleanList[inter+interval][1])
        //                /(interval);
        //        averageSpeedList[i][1]=i*interval;
        //        inter+= interval;
        //    }
        //}

        for (int i = 0 ; i < cleanList.length; i++)Log.i(TAG, "Av speed = " + cleanList[i][4]);
        for (int i = 0 ; i+interval < cleanList.length; i++)Log.i(TAG, "Distance "+i+" = " + MainActivity.distance(cleanList[i][0],cleanList[i][1],cleanList[i+interval][0],cleanList[i+interval][1]));


        //double lastx = 100;
        //double lasty = contentHeight - 50 - (add * averageSpeedList[0][0]);
        ////drawing the curve
        //for (int i = 0; i< averageSpeedList.length-1;i++) { //
        //    if ( (averageSpeedList[i][0]>0.2 && averageSpeedList[i+1][0]>0.2 && averageSpeedList[i][0]< 1100) ) {//|| (i>0 && averageSpeedList[i-1][0]==0 && averageSpeedList[i][0]==0 && averageSpeedList[i+1][0]==0)
        //        canvas.drawLine((float) lastx, (float) lasty, (float) (lastx + interval * (contentWidth - 100 - paddingLeft) / (totaltime + 1)), (float) (contentHeight - 50 - (add * (int) averageSpeedList[i][0])), paint);
        //        lastx += interval * (contentWidth - 100 - paddingLeft) / (totaltime + 1);
        //        lasty = contentHeight - 50 - (add * (int) averageSpeedList[i][0]);
        //    }
        //    else if (i>1){
        //        canvas.drawLine((float) lastx, (float) lasty, (float) (lastx + interval * (contentWidth - 100 - paddingLeft) / (totaltime + 1)), (float) (contentHeight - 50 - (add * (int) averageSpeedList[i-2][0])), paint);
        //        lastx += interval * (contentWidth - 100 - paddingLeft) / (totaltime + 1);
        //        lasty = contentHeight - 50 - (add * (int) averageSpeedList[i-2][0]);
        //
        //    }
        //}

        //test for easiest
        double lastx = 100;
        double lasty = contentHeight - 50 - (add * cleanList[0][4]);
        //drawing the curve
        for (int i = 0; i+interval< cleanList.length;i+=interval) { //

                canvas.drawLine((float) lastx, (float) lasty, (float) (lastx + interval * (contentWidth - 100 - paddingLeft) / (totaltime + 1)), (float) (contentHeight - 50 - (add * (int) cleanList[i][4])), paint);
                lastx += interval * (contentWidth - 100 - paddingLeft) / (totaltime + 1);
                lasty = contentHeight - 50 - (add * (int) cleanList[i][4]);

        }

        //TODO: end the graph when there is still points after the interval

        //if (totaltime%interval>0){
        //    averageSpeedList[averageSpeedList.length-1][0] = 1000* 3.6* MainActivity.distance(cleanList[(int)((cleanList.length-(totaltime%interval)))][0],cleanList[(int)((cleanList.length-(totaltime%interval)))][1],cleanList[cleanList.length-1][0],cleanList[cleanList.length-1][1])
        //            /(interval);
        //    if (1>averageSpeedList[averageSpeedList.length-1][0] && averageSpeedList[averageSpeedList.length-1][0]<10)
        //    canvas.drawLine((float) lastx, (float) lasty, (float) (lastx + (totaltime%interval) * (contentWidth - 100 - paddingLeft) / (totaltime + 1)), (float) (contentHeight - 50 - (add * (int) averageSpeedList[ averageSpeedList.length-1][0])), paint);

        //}//canvas.drawLine((float) lastx, (float) lasty, (float) (lastx + interval * (contentWidth - 100 - paddingLeft) / (totaltime + 1)), (float) (contentHeight - 50 - (add * (int) averageSpeedList[ averageSpeedList.length-1][0])), paint);

    }






    public static void cleanList(){

        counter = 0;
        int test = 0;
        int i;
        while (notcleanList[test][3]!=0) {
            test+=1;
        }
        cleanList[0][0] = notcleanList[test][0];
        cleanList[0][1] = notcleanList[test][1];
        cleanList[0][2] = notcleanList[test][2];
        cleanList[0][3] = notcleanList[test][3];
        counter = 1;
        for (i = test;i < MainActivity.i; i++){
            if(cleanList[counter-1][3]<notcleanList[i][3] ) {
                cleanList[counter][0] = notcleanList[i][0];
                cleanList[counter][1] = notcleanList[i][1];
                cleanList[counter][2] = notcleanList[i][2];
                cleanList[counter][3] = notcleanList[i][3];
                cleanList[counter][4] = notcleanList[i][4];
                counter+=1;
            }
        }
    }

}
