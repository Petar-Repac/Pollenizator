package com.example.pollenconcentration;

import org.json.JSONException;
import org.json.JSONObject;

public class AllergenDTO {

    public int id;
    public String name;
    public String localizedName;
    public int type;
    public int allergenicity;


    public AllergenDTO(JSONObject object) throws JSONException {

        this.id = object.getInt("id");
        this.name = object.getString("name");
        this.localizedName = object.getString("localized_name");
        this.type = object.getInt("type");
        this.allergenicity = object.getInt("allergenicity");

    }

}
