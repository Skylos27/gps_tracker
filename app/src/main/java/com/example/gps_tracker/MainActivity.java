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
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
    double alti;
    String totaltime;
    String filename;
    String path;
    File myFile;
    String formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
        formattedDate = df.format(c.getTime());
        filename = formattedDate;
        path = Environment.getExternalStorageDirectory().toString() + "/GPStracks/"+filename+".gpx";
        locMan = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        chrono = findViewById(R.id.chronoMeter);
        lat = findViewById(R.id.latitudeText);
        lon = findViewById(R.id.longitudeText);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; // In miles
        dist = dist * 1.609344; // in kilometers

        return (dist);
    }


    //  This function converts decimal degrees to radians
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    public void checkMyPermissions(View view){
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_DENIED){
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

    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    public void recording(View view) {
        isOn = true;
        createGPX();
        timer();
        checkMyPermissions(view);
        createLocationListener();
    }


    @Override
    protected void onStop(){
        super.onStop();
        try {
            FileOutputStream f = new FileOutputStream(myFile);
            PrintWriter pw = new PrintWriter(f);
            pw.println("</gpx>");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }

        locMan = null;
    }

    private void createLocationListener(){
        try{
            locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    double alti = location.getAltitude();
                    lon.setText("Current longitude: "+longitude);
                    lat.setText("Current latitude: "+latitude);
                    try {
                        FileOutputStream f = new FileOutputStream(myFile);
                        PrintWriter pw = new PrintWriter(f);
                        pw.println("<wpt lat=\""+latitude+"\" lon=\""+longitude+"\">\n" +
                                "    <ele>"+alti+"</ele>\n" +
                                "    <time>"+formattedDate+"</time>\n"+
                                "  </wpt>");
                        pw.flush();
                        pw.close();
                        f.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Log.i(TAG, "******* File not found. Did you" +
                                " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onProviderEnabled(String provider){

                    if(provider == LocationManager.GPS_PROVIDER){
                        //Show last known.
                        Location loc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);


                        if(loc != null) {
                            //Now if the location is not null just show it.
                            lat.setText("" + loc.getLatitude());
                            lon.setText("" + loc.getLongitude());
                        }//inner if

                    }//outer if

                }//onProviderEnabled()

                @Override
                public void onProviderDisabled(String provider){

                    if(provider != LocationManager.GPS_PROVIDER){
                        //Just show default string.
                        lat.setText("Currently unavailable");
                        lon.setText("Currently unavailable");

                    }//if

                }//onProviderDisabled()


                public void onStatusChanged(String provider, int status, Bundle extras){

                }//onStatusChanged()

            });

        }// try
        catch(SecurityException se){
            se.printStackTrace();
        }//catch
    }
    // Timer for the UI
    private void timer(){
        chrono.setBase(SystemClock.elapsedRealtime());
        chrono.start();
        totaltime = chrono.toString();
    }

    private void createGPX(){
        myFile = new File(path);
        try {
            FileOutputStream f = new FileOutputStream(myFile);
            PrintWriter pw = new PrintWriter(f);
            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n" +
                    "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"byHand\" version=\"1.1\" \n" +
                    "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                    "    xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">");
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}