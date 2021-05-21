package com.example.gps_tracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ReportActivity extends AppCompatActivity {
    Button backButton;
    private double averageSpeed = 0;
    private double totDistance = 0;
    private TextView averageSpeedTV;
    private TextView totDistTV;
    private TextView minMaxAltTV;
    private TextView totalTimeTV;
    DecimalFormat df = new DecimalFormat("#.##");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_layout);
        averageSpeedTV = findViewById(R.id.averageSpeedTV);
        totDistTV = findViewById(R.id.totalDistanceTV);
        minMaxAltTV=findViewById(R.id.minMaxAltTV);
        totalTimeTV = findViewById(R.id.totalTimeTV);
        backButton = findViewById(R.id.returnButton);
        GraphView.notcleanList = MainActivity.listPoint;
        GraphView.totaltime = MainActivity.totaltime;
        GraphView.counter = 0;
        GraphView.cleanList = new double[(int)MainActivity.totaltime+1][5];
        GraphView.cleanList();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getAverageSpeed();
        getTotalDistance();
        getMinMaxAlt();
        getTotalTime();

    }

    private void getAverageSpeed(){
        averageSpeed = (double)((1000*MainActivity.totDist)/(MainActivity.totaltime));
        averageSpeedTV.setText("Your average speed was: "+df.format(averageSpeed*3.6)+" km/h");
    }

    private void getTotalDistance(){
        totDistance = MainActivity.totDist;
        totDistTV.setText("The total distance of your trip is: "+df.format(totDistance)+" km");
    }
    private void getMinMaxAlt(){
        minMaxAltTV.setText("The minimum altitude during your trip is: " + MainActivity.minAlti+" meters\nThe maximum altitude during your trip is: "+MainActivity.maxAlti+" meters");
    }
    private void getTotalTime(){
        long millis = MainActivity.totaltime*1000;
        String hms = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        totalTimeTV.setText("Total time of the trip: "+ hms);

    }
}
