package com.example.pollenconcentration;

import java.util.ArrayList;

public class AllergenConcentration {

    public int allergenId;
    public String name;
    public double avgConcentration;
    public ArrayList<Integer> concentrations;
    private int sum;
    public String trend;


    public AllergenConcentration(int allergenId, String name){
        concentrations = new ArrayList<Integer>();
        this.allergenId = allergenId;
        this.name = name;
    }

    public void addConcentration(int concentration){
        concentrations.add(concentration);
        sum += concentration;

        avgConcentration = (double) sum / (double) concentrations.size();
    }

    // Using linear regression to get slope
    public void calcTrend(){
        int n =  concentrations.size();

        if( n <= 1) {
            trend = "/";
            return;
        }

        double sumX = n * (n + 1) / 2.0;
        double sumX2 = 0;
        double sumXY = 0;
        double sumY = sum;
        for (int i = 0; i < concentrations.size(); i++){
            sumX2 += (i+1) * (i+1);
            sumXY = (i+1) * concentrations.get(i);
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX*sumX);

        if( slope > 0.05){
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
        return  name + " "+ avgConcentration + " " + trend;
    }
}
