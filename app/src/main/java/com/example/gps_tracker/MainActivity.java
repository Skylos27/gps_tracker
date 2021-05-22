package com.example.gps_tracker;


import androidx.appcompat.app.AppCompatActivity;


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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = null;
    private boolean isOn = false;
    LocationManager locMan;
    double latitude;
    double longitude;
    Chronometer chrono;
    TextView speedTV;
    TextView dis;
    double alti;
    public static long totaltime;
    String filename;
    public File myFile;
    String formattedDate;
    public static Double[][] listPoint;
    Context context;
    Button onOff;
    public static int i ;
    public static double totDist;
    private double totDistCalc;
    DecimalFormat df;
    public static double maxAlti,minAlti = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        locMan = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        chrono = findViewById(R.id.chronoMeter);
        speedTV = findViewById(R.id.avSpeed);
        dis = findViewById(R.id.distanceText);
        onOff = findViewById(R.id.startButton);
        df = new DecimalFormat("#.##");
        createLocationListener();
    }
    // calculate the distance between 2 coordinates
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515; // In miles
        dist = dist * 1.609344; // in kilometers

        return (dist);
    }
    //  This function converts decimal degrees to radians
    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    //  This function converts radians to decimal degrees
    public static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    // compute the total distance of the trip with the coordinates that we have
    private void totalDistance(){
        if (i > 0 && distance(listPoint[i - 1][0], listPoint[i - 1][1], listPoint[i][0], listPoint[i][1])<1)
            totDistCalc += distance(listPoint[i - 1][0], listPoint[i - 1][1], listPoint[i][0], listPoint[i][1]);

        totDist = totDistCalc;
    }
    // check all the permissions needed at the start of the app
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



    public void recording(View view) {
        // start the recording
        if(!isOn){
            createGPX();
            totaltime = 0;
            timer();
            totDist =0;
            totDistCalc= 0.0;
            i = 0;

            checkMyPermissions(view);
            isOn = true;
            //createLocationListener();
            onOff.setText("Stop");


        }
        //stop the recording and switch to the second activity
        else{

            totaltime =  (SystemClock.elapsedRealtime() - chrono.getBase())/1000;
            Log.i(TAG, "Total time = " + totaltime);
            chrono.stop();
            speedTV.setText("Current speed: ");
            dis.setText("Total distance: ");
            chrono.setText("00:00");
            onOff.setText("Start");
            // write the end of the GPX file
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
            isOn = false;
            locMan = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            if (i>0)
            {
                Intent switchActivityIntent = new Intent(this, ReportActivity.class);
                startActivity(switchActivityIntent);
            }
            else{
                Toast alert = Toast.makeText(context,"Your trip is really too short",Toast.LENGTH_LONG);
                alert.show();
            }
        }
    }

    private void createLocationListener(){
        // the worst thing of the project but at least that can last for 11 days ...
        listPoint = new Double[1000000][5];
            try {
                locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, (float) 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        // create the list with coordinates, atitude, time and average speed
                        if(isOn) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            double alti = location.getAltitude();
                            if (alti > maxAlti) maxAlti = alti;
                            if (alti < minAlti) minAlti = alti;
                            totaltime =  ((SystemClock.elapsedRealtime() - chrono.getBase())/1000);
                            listPoint[i][0]= latitude;
                            listPoint[i][1]=longitude;
                            listPoint[i][2]=alti;
                            listPoint[i][3]=(double)totaltime*1000;
                            if (i>1 && 1000*(distance(listPoint[i-1][0],listPoint[i-1][1],listPoint[i][0],listPoint[i][1])*3.6)/(listPoint[i][3]-listPoint[i-1][3])<1000)
                                listPoint[i][4]= (double)(1000*1000*(distance(listPoint[i-1][0],listPoint[i-1][1],listPoint[i][0],listPoint[i][1])*3.6)/(listPoint[i][3]-listPoint[i-1][3]));
                            else listPoint[i][4] = (double) 0;
                            totalDistance();
                            if (i>0 && (listPoint[i][4] != 0)) {
                                speedTV.setText("Current speed: " + df.format(listPoint[i][4]) + " km/h");
                            }
                            dis.setText("Total distance: "+ df.format(totDist)+" km");

                            Log.i(TAG, "Speed handmade = " + listPoint[i][4]);
                            Log.i(TAG, "Total time = " + (double)totaltime);
                            i+=1;
                        }

                        try {
                            // write the coordinates on the DPX file
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


                    }//onProviderEnabled()

                    @Override
                    public void onProviderDisabled(String provider) {

                        if (provider != LocationManager.GPS_PROVIDER) {
                            //Just show default string.
                            Toast alert = Toast.makeText(context,"Your GPS provider is disabled",Toast.LENGTH_SHORT);
                            alert.show();

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
        //write the header
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