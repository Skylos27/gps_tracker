package com.example.gps_tracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = null;
    private boolean isOn = false;
    public boolean onSecScreen = false;
    LocationManager locMan;
    double latitude;
    double longitude;
    Chronometer chrono;
    TextView lat;
    TextView lon;
    TextView dis;
    double alti;
    public static long totaltime;
    String filename;
    public File myFile;
    String formattedDate;
    public static Double[][] listPoint;
    Button onOff;
    Integer i ;
    private int totDist;
    private double totDistCalc;
    Double time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locMan = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        chrono = findViewById(R.id.chronoMeter);
        lat = findViewById(R.id.latitudeText);
        lon = findViewById(R.id.longitudeText);
        dis = findViewById(R.id.distanceText);
        onOff = findViewById(R.id.startButton);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; // In miles
        dist = dist * 1.609344; // in kilometers

        return (dist);
    }

    private void totalDistance(){
        if(i>0) totDistCalc += 1000 * distance(listPoint[i-1][0],listPoint[i-1][1],listPoint[i][0],listPoint[i][1]);
        totDist = (int)totDistCalc;
    }
    //  This function converts decimal degrees to radians
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    public void checkMyPermissions(View view){

            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
                requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, PackageManager.PERMISSION_GRANTED);

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);

            if (checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);


    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void recording(View view) {

        if(!isOn){
            createGPX();
            timer();
            totDist =0;
            totDistCalc= 0.0;
            i = 0;
            listPoint = new Double[100000][3];
            checkMyPermissions(view);
            isOn = true;
            createLocationListener();
            onOff.setText("Stop");


        }
        else{
            isOn = false;
            totaltime =  (SystemClock.elapsedRealtime() - chrono.getBase())/1000;
            chrono.stop();
            onOff.setText("Start");

            try {
                FileWriter fw = new FileWriter(myFile,true);
                BufferedWriter bw = new BufferedWriter(fw );
                PrintWriter pw = new PrintWriter(bw);
                pw.println("</gpx>");
                pw.flush();
                pw.close();
                bw.close();
                fw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(TAG, "Impossible to put the end");
            } catch (IOException e) {
                e.printStackTrace();
            }
            locMan = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            Intent switchActivityIntent = new Intent(this, ReportActivity.class);
            startActivity(switchActivityIntent);
        }
    }

    private void createLocationListener(){

            try {
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {


                        if(isOn) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            double alti = location.getAltitude();
                            listPoint[i][0]= latitude;listPoint[i][1]=longitude;listPoint[i][2]=alti;
                            totalDistance();
                            totaltime =  (SystemClock.elapsedRealtime() - chrono.getBase())/1000;
                            Log.i(TAG, "Total time = " + totaltime);
                            i+=1;
                            lon.setText("Current longitude: " + longitude);
                            lat.setText("Current latitude: " + latitude);
                            dis.setText("Total distance: "+ totDist);

                        }

                        try {
                            if (isOn) {
                                FileWriter fw = new FileWriter(myFile, true);
                                BufferedWriter bw = new BufferedWriter(fw);
                                PrintWriter pw = new PrintWriter(bw);
                                pw.println("<wpt lat=\"" + latitude + "\" lon=\"" + longitude + "\">\n" +
                                        "    <ele>" + alti + "</ele>\n" +
                                        "    <time>" + formattedDate + "</time>\n" +
                                        "  </wpt>");
                                pw.flush();
                                pw.close();
                                bw.close();
                                fw.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.i(TAG, "Impossible to add points");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                        if (provider == LocationManager.GPS_PROVIDER) {
                            //Show last known.
                            Location loc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                            if (loc != null) {
                                //Now if the location is not null just show it.
                                lat.setText("" + loc.getLatitude());
                                lon.setText("" + loc.getLongitude());
                            }//inner if

                        }//outer if

                    }//onProviderEnabled()

                    @Override
                    public void onProviderDisabled(String provider) {

                        if (provider != LocationManager.GPS_PROVIDER) {
                            //Just show default string.
                            lat.setText("Currently unavailable");
                            lon.setText("Currently unavailable");

                        }//if

                    }//onProviderDisabled()


                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }//onStatusChanged()

                });

            }// try
            catch (SecurityException se) {
                se.printStackTrace();
            }//catch

    }
     //Timer for the UI
    private void timer(){
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();

    }
    // create a GPX file
    private void createGPX(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        formattedDate = df.format(c.getTime());
        filename = formattedDate;
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator +  "GPXtracks/");
        while(!folder.exists()) folder.mkdirs();


        myFile = new File(Environment.getExternalStorageDirectory()+ File.separator +  "GPXtracks/", filename+".gpx");
        try {
            FileWriter fw = new FileWriter(myFile,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                    "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"byHand\" version=\"1.1\" \n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                    "    xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
            pw.flush();
            pw.close();
            bw.close();
            fw.close();
            Log.i(TAG,myFile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "Impossible to create the file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}