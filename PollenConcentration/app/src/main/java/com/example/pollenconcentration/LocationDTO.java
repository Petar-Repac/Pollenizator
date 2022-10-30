package com.example.pollenconcentration;

public class LocationDTO {
    public int id;
    public String name;


    public LocationDTO(int id, String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }
}
