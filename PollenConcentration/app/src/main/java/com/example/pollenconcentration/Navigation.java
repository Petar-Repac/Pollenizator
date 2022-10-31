package com.example.pollenconcentration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class Navigation extends AppCompatActivity {

    private Button btnConcentrations;
    private Button btnHowToUse;
    private Button btnAboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        btnConcentrations = findViewById(R.id.btnConcentrations);
        btnAboutUs = findViewById(R.id.btnAboutUs);
        btnHowToUse = findViewById(R.id.btnHowToUse);

        btnConcentrations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Check for internet connection first
                if(!Util.isInternetConnected(Navigation.this)){
                    Util.showAlert(Navigation.this, "Обавештење",
                            "Морате бити повезани на Интернет.");
                }
                else{
                    openActivity("Concentrations");
                }

            }
        });
        btnAboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity("AboutUs");
            }
        });
        btnHowToUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivity("HowToUse");
            }
        });

    }

    private void openActivity(String activityName){

        Intent intent;
        switch(activityName){
            case "Concentrations":
                intent = new Intent(this, Concentrations.class);
            break;
            case "HowToUse":
                intent = new Intent(this, HowToUse.class);
            break;
            case "AboutUs":
                intent = new Intent(this, AboutUs.class);
            break;
            default:
                intent = new Intent(this, Concentrations.class);
            break;
        }

        startActivity(intent);
    }
}