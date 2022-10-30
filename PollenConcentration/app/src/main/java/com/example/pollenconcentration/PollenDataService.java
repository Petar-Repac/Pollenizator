package com.example.pollenconcentration;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PollenDataService {

    public static final String API_URL = "http://polen.sepa.gov.rs/api/opendata/";
    public Context context;
    private HashMap<Integer,AllergenDTO> allergens;

    public PollenDataService(Context context){
        this.context = context;
        String urlAllergens = API_URL +  "allergens/";
        //Generating a HTTP GET request
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,  urlAllergens, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        allergens = new HashMap<Integer, AllergenDTO>();

                        for(int i = 0; i < response.length(); i++){
                            try {
                                AllergenDTO allergen = new AllergenDTO(response.getJSONObject(i));
                                allergens.put(allergen.id, allergen);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Log.d("API_error", error.toString());
                            Log.d("API_response",new String(error.networkResponse.data, "UTF-8"));
                        }
                        catch (UnsupportedEncodingException e) {
                            Log.d("API_error", "getLocations error - unsupported encoding");
                        }
                        finally {
                            Toast.makeText(context, "API error", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // Add the request to the RequestQueue.
        RequestQSingleton.getInstance(context).addToRequestQueue(request);
    }

    //To be used for async callbacks
    public interface LocationResponseListener{
        void onError(VolleyError error);
        void onResponse(ArrayList<LocationDTO> response);
    }


    //To be used for async callbacks
    public interface PollenResponseListener{
        void onError(VolleyError error);
        void onResponse(HashMap<Integer,AllergenConcentration> response);
    }
    //Async Requests
    public void getLocations(LocationResponseListener responseListener){
        String url = API_URL + "locations/";


        //Generating a HTTP GET request
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        ArrayList<LocationDTO> locations = new ArrayList<LocationDTO>();

                        for(int i = 0; i < response.length(); i++){
                            try {
                                int id = response.getJSONObject(i).getInt("id");
                                String name = response.getJSONObject(i).getString("name");
                                locations.add(new LocationDTO(id, name));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        responseListener.onResponse(locations);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseListener.onError(error);
                    }
                }
        );

        // Add the request to the RequestQueue.
        RequestQSingleton.getInstance(context).addToRequestQueue(request);

    }

    /* Data flow is as such:
         API-pollens(location, dateFrom, dateTo) -> [{pollenId, date, concentrationIds[]}] => pollenIds
         API-concentrations( pollenIds[]) -> [{allergenId, value}] => allergenIds, value
         HashMap-allergens(allergenId) -> allergenName


         Call to concentrations by pollenIds[] is necessary for a bulk search
         in order to avoid too many API calls
    */
    public void getConcentrations(int locationId, String dateFrom, String dateTo, PollenResponseListener responseListener){
        String urlPollens = API_URL + "pollens/";

        urlPollens +="?location=" + locationId;
        urlPollens +="&date_after=" + dateFrom;
        urlPollens +="&date_before=" + dateTo;


        JsonObjectRequest pollenRequest = new JsonObjectRequest(Request.Method.GET,  urlPollens, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    String urlConcentrations = API_URL + "concentrations/";
                    HashMap <Integer, PollenDTO> pollens = new HashMap<Integer, PollenDTO>();
                    try {
                        JSONArray JSONresults = response.getJSONArray("results");
                        for(int i = 0; i < JSONresults.length(); i++){

                            //Creating a pollen DTO to sort concentrations by date later
                            int id =JSONresults.getJSONObject(i).getInt("id");
                            String date = JSONresults.getJSONObject(i).getString("date");
                            PollenDTO pollen = new PollenDTO(id, locationId, date);
                            pollens.put(id, pollen);

                            //constructing GET url for concentrations for a bulk API request
                            if(i == 0){
                                urlConcentrations += "?pollen_ids=" + id ;
                            }
                            else{
                                urlConcentrations += "&pollen_ids=" + id ;
                            }
                        }
                        //second async request
                        //entering callback hell :(
                        concentrationRequest(urlConcentrations, pollens, responseListener);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    responseListener.onError(error);
                }
            }
        );

        // Add the request to the RequestQueue.
        RequestQSingleton.getInstance(context).addToRequestQueue(pollenRequest);
    }

    //once all pollen ids are obtained, make call to get concentrations
    //of all allergens by pollen record
    private void concentrationRequest(String urlConcentrations, HashMap <Integer, PollenDTO> pollens, PollenResponseListener responseListener){

        JsonObjectRequest concentrationRequest = new JsonObjectRequest(Request.Method.GET, urlConcentrations, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        //Final result will be stored there
                        HashMap <Integer, AllergenConcentration> allergenData = new HashMap<Integer, AllergenConcentration>();
                        try {
                            JSONArray JSONresults = response.getJSONArray("results");

                            // Processing the GET response
                            for(int i = 0; i < JSONresults.length(); i++){

                                JSONObject concentration = JSONresults.getJSONObject(i);
                                int pollenId = concentration.getInt("pollen");
                                int allergenId = concentration.getInt("allergen");

                                //creating concentration measurement object
                                //used for determining trend
                                int concentrationValue = concentration.getInt("value");
                                String date = pollens.get(pollenId).getDate();
                                Measurement measurement = new Measurement(concentrationValue, date);

                                //Populating HashMap of resulst
                                if(!allergenData.containsKey(allergenId)){
                                    String allergenName = allergens.get(allergenId).localizedName;
                                    AllergenConcentration data = new AllergenConcentration(allergenId, allergenName);
                                    data.addConcentration(measurement);

                                    allergenData.put(allergenId,  data);
                                }
                                else{
                                    AllergenConcentration data = allergenData.get(allergenId);
                                    data.addConcentration(measurement);
                                }

                            }
                            //calculate trends only after all data is retrieved
                            for(AllergenConcentration row : allergenData.values()){
                                row.calcTrend();
                            }
                            responseListener.onResponse(allergenData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        responseListener.onError(error);
                    }
                });

        // Add the request to the RequestQueue.
        RequestQSingleton.getInstance(context).addToRequestQueue(concentrationRequest);
    }

}
