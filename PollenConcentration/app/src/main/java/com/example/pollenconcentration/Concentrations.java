package com.example.pollenconcentration;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import ir.androidexception.datatable.DataTable;
import ir.androidexception.datatable.model.DataTableHeader;
import ir.androidexception.datatable.model.DataTableRow;

public class Concentrations extends AppCompatActivity {

    PollenDataService service;
    Spinner dropdownLocations;
    Button btnSearch;
    Button btnFrom;
    Button btnTo;
    DatePickerDialog datePickerDialog;
    DataTable dataTable;

    //search form purposes
    String dateFrom;
    String dateTo;
    int locationId;

    private void initializeComponents(){
        //Assigning values to each control in the layout
        dropdownLocations = findViewById(R.id.dropdown_locations);
        btnSearch = findViewById(R.id.button_search);
        btnFrom = findViewById(R.id.btn_from);
        btnTo = findViewById(R.id.btn_to);
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
        dropdownLocations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                 LocationDTO location = (LocationDTO) dropdownLocations.getSelectedItem();
                 locationId = location.id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                dropdownLocations.setSelection(0);
            }

        });
        btnFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateFrom);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);

                    datePickerDialog = new DatePickerDialog(Concentrations.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            btnFrom.setText("Од:" + year + "-" + (month + 1) + "-" + day);
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
        btnTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateTo);
                    Calendar calendar = new GregorianCalendar();
                    calendar.setTime(date);

                    datePickerDialog = new DatePickerDialog(Concentrations.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                            btnTo.setText("До:" + year + "-" + (month + 1) + "-" + day);
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
        btnSearch.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                //Check for internet connection first
                if(!Util.isInternetConnected(Concentrations.this)){
                    Util.showAlert(Concentrations.this, "Обавештење",
                            "Морате бити повезани на Интернет.");
                }
                else{
                    if(checkDates() == 0) search();
                }

            }
        });
    }


    private void search(){
        LocationDTO location = (LocationDTO) dropdownLocations.getSelectedItem();
        service.getConcentrations(location.id, dateFrom, dateTo, new PollenDataService.PollenResponseListener(){

            @Override
            public void onError(VolleyError error) {
                Util.handleHttpError(Concentrations.this, error);
            }

            @Override
            public void onResponse(HashMap<Integer, AllergenConcentration> response) {
                if(response.size() == 0){
                    Toast.makeText(Concentrations.this, "Нису вршена мерења на изабраној локацији у назначеном периоду.", Toast.LENGTH_LONG).show();
                }
                ArrayList<AllergenConcentration> data = new ArrayList<AllergenConcentration>(response.values());
                populateDataTable(data);
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

                btnFrom.setText("Од:" + dateFrom);
                btnTo.setText("До:" + dateTo);

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
                Util.handleHttpError(Concentrations.this, error);
            }
            @Override
            public void onResponse(ArrayList<LocationDTO> response) {
                ArrayAdapter<LocationDTO> adapter =
                        new ArrayAdapter<LocationDTO>(Concentrations.this, android.R.layout.simple_spinner_dropdown_item, response);
                dropdownLocations.setAdapter(adapter);
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
        btnTo.setText("До:" + to);

        calendar.add(Calendar.DATE, -7);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) ;
        day = calendar.get(Calendar.DAY_OF_MONTH);

        String from = year + "-" + (month + 1) + "-" + day;
        dateFrom = from;
        btnFrom.setText("Од:" + from);

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