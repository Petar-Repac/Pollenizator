package com.example.pollenconcentration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import ir.androidexception.datatable.DataTable;
import ir.androidexception.datatable.model.DataTableHeader;
import ir.androidexception.datatable.model.DataTableRow;

public class Concentrations extends AppCompatActivity {

    PollenDataService service;
    Spinner dropdown_locations;
    Button btn_search;
    Button btn_from;
    Button btn_to;
    DatePickerDialog datePickerDialog;
    DataTable dataTable;

    //search form purposes
    String dateFrom;
    String dateTo;
    int locationId;

    private void initializeComponents(){
        //Assigning values to each control in the layout
        dropdown_locations = findViewById(R.id.dropdown_locations);
        btn_search = findViewById(R.id.button_search);
        btn_from = findViewById(R.id.btn_from);
        btn_to = findViewById(R.id.btn_to);
        //Initializing the Data Service used for API calls
        service = new PollenDataService(Concentrations.this);
        dataTable = findViewById(R.id.data_table);

        initializeDataTable();
    }
    private void populateComponents(){
        populateLocationDropdown();
        populateDatePickers();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_concentrations);

        initializeComponents();
        populateComponents();
        dropdown_locations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                 LocationDTO location = (LocationDTO) dropdown_locations.getSelectedItem();
                 locationId = location.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                dropdown_locations.setSelection(0);
            }

        });
        btn_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateFrom);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);

                    datePickerDialog = new DatePickerDialog(Concentrations.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            btn_from.setText("Од:" + year + "-" + (month + 1) + "-" + day);
                            dateFrom = year + "-" + (month + 1) + "-" + day;
                        }
                    }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) ,calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.show();
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateTo);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);

                    datePickerDialog = new DatePickerDialog(Concentrations.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            btn_to.setText("До:" + year + "-" + (month + 1) + "-" + day);
                            dateTo = year + "-" + (month + 1) + "-" + day;
                        }
                    }, calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) ,calendar.get(Calendar.DAY_OF_MONTH));
                    datePickerDialog.show();
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        btn_search.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(checkDates() == 0) search();
            }
        });
    }


    private void search(){
        LocationDTO location = (LocationDTO) dropdown_locations.getSelectedItem();
        service.getConcentrations(location.id, dateFrom, dateTo, new PollenDataService.PollenResponseListener(){

            @Override
            public void onError(VolleyError error) {
                Log.d("DataConcentration", error.toString());
            }

            @Override
            public void onResponse(HashMap<Integer, AllergenConcentration> response) {
                if(response.size() > 0){
                    ArrayList<AllergenConcentration> data = new ArrayList<AllergenConcentration>(response.values());
                    populateDataTable(data);
                }
                else{
                    Toast.makeText(Concentrations.this, "Нису вршена мерења на изабраној локацији у назначеном периоду.", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    //User date input check
    // returns 0 if input is OK or fixable
    // returns -1 if input is incompatible with search
    private int checkDates(){
        Date date1 = null;
        Date date2 = null;
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd");
        try {
            date1 = format.parse(dateFrom);
            date2 = format.parse(dateTo);

            //dateFrom is after dateTo
            if(date1.compareTo(date2) > 0){
                String temp = dateFrom;
                dateFrom = dateTo;
                dateTo = temp;

                btn_from.setText("Од:" + dateFrom);
                btn_to.setText("До:" + dateTo);

                //Toast.makeText(Concentrations.this,"Датум од не сме бити након датума до", Toast.LENGTH_SHORT).show();
            }
            else if (date1.compareTo(date2) == 0){
                Toast.makeText(Concentrations.this,"Изаберите два различита датума", Toast.LENGTH_SHORT).show();
                return  -1;
            }

            long dtMs = Math.abs(date2.getTime() - date1.getTime());
            long dtDays = TimeUnit.DAYS.convert(dtMs, TimeUnit.MILLISECONDS);

            if(dtDays > 12){
                Toast.makeText(Concentrations.this,"Разлика између датума не сме бити већа од 12 дана", Toast.LENGTH_SHORT).show();
                return  -1;
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private void populateLocationDropdown(){
        service.getLocations(new PollenDataService.LocationResponseListener() {
            @Override
            public void onError(VolleyError error) {
                try {
                    Log.d("API_error", error.toString());
                    Log.d("API_response",new String(error.networkResponse.data, "UTF-8"));
                }
                catch (UnsupportedEncodingException e) {
                    Log.d("API_error", "getLocations error - unsupported encoding");
                }
                finally {
                    Toast.makeText(Concentrations.this, "API error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onResponse(ArrayList<LocationDTO> response) {
                ArrayAdapter<LocationDTO> adapter =
                        new ArrayAdapter<LocationDTO>(Concentrations.this, android.R.layout.simple_spinner_dropdown_item, response);
                dropdown_locations.setAdapter(adapter);
            }
        });

    }
    private void populateDatePickers(){
        int year, month, day;
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        String to = year + "-" + (month + 1)+ "-" + day;
        dateTo = to;
        btn_to.setText("До:" + to);

        calendar.add(Calendar.DATE, -7);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) ;
        day = calendar.get(Calendar.DAY_OF_MONTH);

        String from = year + "-" + (month + 1) + "-" + day;
        dateFrom = from;
        btn_from.setText("Од:" + from);

    }
    private void initializeDataTable(){
        DataTableHeader header = new DataTableHeader.Builder()
                .item("Алерген", 5)
                .item("Концентрација", 5)
                .item("Тренд", 5).build();

        dataTable.setHeader(header);
        dataTable.inflate(Concentrations.this);
    }

    private void populateDataTable(ArrayList<AllergenConcentration> data){
        dataTable.removeAllViews();

        ArrayList<DataTableRow> rows = new ArrayList<>();

        for(AllergenConcentration record: data) {

            DataTableRow row = new DataTableRow.Builder()
                    .value(record.name)
                    .value(String.format("%.2f",record.avgConcentration))
                    .value(record.trend).build();
            rows.add(row);

        }


        dataTable.setRows(rows);
        dataTable.inflate(Concentrations.this);
    }
}