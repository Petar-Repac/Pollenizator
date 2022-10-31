package com.example.pollenconcentration;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;

public class Util {

    public static boolean isInternetConnected(Context context){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            return true;
        }
        else
            return false;

    }


    public static void showAlert(Context context, String title, String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton("У реду",null);
        alert.show();
    }

    public static void handleHttpError(Context context, VolleyError error){
        try {
            Log.d("Volley error", error.toString());
            Log.d("Volley error response",new String(error.networkResponse.data, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            Log.d("Volley error", "unsupported encoding");
        }
        finally {
            Util.showAlert(context, "Грешка", "Дошло је до грешке. Молимо покушајте касније.");
        }
    }
}
