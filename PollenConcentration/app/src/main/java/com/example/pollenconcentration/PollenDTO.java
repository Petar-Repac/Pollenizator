package com.example.pollenconcentration;

public class PollenDTO
{
    private int id;
    private int location; //not necessary for now, but may be useful later
    private String date;

    // Getters
    public int getId() {
        return id;
    }
    public int getLocation() {
        return location;
    }
    public String getDate() {
        return date;
    }



    public PollenDTO(int id, int location, String date){
        this.id = id;
        this.date = date;
    }


}
