package com.example.pollenconcentration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Measurement {
    public int value;
    public String date;

    public Measurement(int concentration, String date){
        this.value = concentration;
        this.date = date;
    }

    // may need refactoring later
    public Date getDate()  {
        try {
            return  new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  null;
    }


    @Override
    public String toString(){
        return  "{value: "+ value + ", date:" + date +"}";
    }
}
