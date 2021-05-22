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
    private double averageSpeed;
    private double totDistance ;
    private TextView averageSpeedTV;
    private TextView totDistTV;
    private TextView minMaxAltTV;
    private TextView totalTimeTV;
    DecimalFormat df = new DecimalFormat("#.##");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_layout);
        averageSpeed = 0;
        totDistance = 0;
        averageSpeedTV = findViewById(R.id.averageSpeedTV);
        totDistTV = findViewById(R.id.totalDistanceTV);
        minMaxAltTV=findViewById(R.id.minMaxAltTV);
        totalTimeTV = findViewById(R.id.totalTimeTV);
        backButton = findViewById(R.id.returnButton);
        GraphView.notcleanList = MainActivity.listPoint;
        GraphView.totaltime = MainActivity.totaltime;
        GraphView.counter = 0;
        GraphView.cleanList = new double[(int)MainActivity.totaltime+1][5];
        if(MainActivity.totaltime>0) GraphView.cleanList(); // avoid a crash if the index of the list is 0


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
    // function which compute the average speed of the full trip
    private void getAverageSpeed(){
        averageSpeed = (double)((1000*MainActivity.totDist)/(MainActivity.totaltime));
        averageSpeedTV.setText("Your average speed was: "+df.format(averageSpeed*3.6)+" km/h");
    }
    // take the total distace from the MainActivity and set the text for the TextView corresponding
    private void getTotalDistance(){
        totDistance = MainActivity.totDist;
        totDistTV.setText("The total distance of your trip is: "+df.format(totDistance)+" km");
    }

    // get the min and the max altitude from MainActivity and show them on screen
    private void getMinMaxAlt(){
        minMaxAltTV.setText("The minimum altitude during your trip is: " + MainActivity.minAlti+" meters\nThe maximum altitude during your trip is: "+MainActivity.maxAlti+" meters");
    }
    // get the total time of the trip from the MainActivity class ang print them with the adequate format
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
