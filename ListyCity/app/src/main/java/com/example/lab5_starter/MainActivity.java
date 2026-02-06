package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;

    private CollectionReference citiesRef;
    private City selectedcity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        FirebaseFirestore.setLoggingEnabled(true); //enable error logging
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("Cities");
        citiesRef.addSnapshotListener((value,error)->{
            if (error != null){
                Log.e("Firestore", error.toString()); //this is how you create your own error logs

            }
            if (value != null && !value.isEmpty()){
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot:value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));


                }

                cityArrayAdapter.notifyDataSetChanged();
            }
        }); //we ask for a snapshot of the database, then firebase responds when it feels like

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

//        addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedcity = cityArrayAdapter.getItem(i);
            //dont declare a new variable here

        });

        //we need to create the delete city functionality here
        Button DeleteButton = findViewById(R.id.buttonDeleteCity);

        DeleteButton.setOnClickListener(view -> {

            if (selectedcity != null) {
                deleteCity(selectedcity);
                selectedcity = null;
            }
        });


    }

    @Override
    public void updateCity(City city, String title, String year) {
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();
//        DocumentReference docRef = citiesRef.document(city.getName());
//        docRef.set(city);

        citiesRef
                .document(city.getName())
                .set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firestore","DocumentSnapshot successfully written");
                    }
                });

    }

    public void deleteCity(City city){
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();
        citiesRef
                .document(city.getName())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firestore","City is deleted");
                    }
                });
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}