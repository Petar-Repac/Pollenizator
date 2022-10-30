package com.example.pollenconcentration;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AllergenConcentration {

    public int allergenId;
    public String name;
    public double avgConcentration;
    public ArrayList<Measurement> concentrations;
    private int sum;
    public String trend;
    private double slope;

    public AllergenConcentration(int allergenId, String name){
        concentrations = new ArrayList<Measurement>();
        this.allergenId = allergenId;
        this.name = name;
    }

    public void addConcentration(Measurement measurement){
        concentrations.add(measurement);
        sum += measurement.value;

        avgConcentration = (double) sum / (double) concentrations.size();
    }

    // Using linear regression to get slope
    public void calcTrend(){
        sortMeasurements();
        double n = (double)concentrations.size();

        if( n <= 1) {
            trend = "/";
            return;
        }

        //Variables for least squares method
        double sumX = 0;
        double sumSS = 0;
        double sumSP = 0;
        double sumY = sum;
        double meanY = sumY / n;
        Date firstDate = concentrations.get(0).getDate();
        ArrayList <Double> arrX= new ArrayList<Double>();


        for (int i = 0; i < concentrations.size(); i++){
            //x value is 1 for first date, increasing by 1 for each date up until the last date
            //from measurement arraylist
            Date currDate = concentrations.get(i).getDate();
            long dtMs = Math.abs(currDate.getTime() - firstDate.getTime());
            long X = TimeUnit.DAYS.convert(dtMs, TimeUnit.MILLISECONDS) + 1;
            sumX += X;
            arrX.add((double)X);
        }
        double meanX = sumX /n;

        for (int i = 0; i < concentrations.size(); i++){
           sumSS += (arrX.get(i) - meanX) * (arrX.get(i) - meanX);
           sumSP += (arrX.get(i) - meanX) * (concentrations.get(i).value - meanY);
        }

        double slope = sumSP / sumSS;

        this.slope = slope;
        if( slope > 0.1){
            trend = "Растући";
        }
        else if (slope <= 0.1 && slope >= -0.1){
            trend = "Стабилан";
        }
        else {
            trend = "Опадајући";
        }
    }



    @Override
    public String toString(){
        String result = "|" + name + " "+ avgConcentration + " " + trend +" "+ slope +" measurements:";
        for(Measurement m : concentrations){
            result +=  m.toString();
        }
        result += "|";
        return  result;
    }

    private class  MeasurementComparator implements  Comparator<Measurement>{
        @Override
        public int compare(Measurement m1, Measurement m2) {
            return m1.getDate().compareTo(m2.getDate());
        }
    }
    private  void sortMeasurements(){
        Collections.sort(concentrations, new MeasurementComparator());
    }
}
