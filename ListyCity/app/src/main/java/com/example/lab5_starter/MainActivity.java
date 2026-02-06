package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private Button editCityButton;
    private ListView cityListView;
    private City selectedCity;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;
    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        citiesRef.addSnapshotListener((value, error) -> {
            if(error != null){
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                cityArrayList.clear();
                for(QueryDocumentSnapshot snapshot: value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name, province));
                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });

        // Set views
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        addCityButton = findViewById(R.id.buttonAddCity);
        editCityButton = findViewById(R.id.buttonEditCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        //addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });
        deleteCityButton.setOnClickListener(view -> {
            City city =selectedCity;
            deleteCity(city);

        });

        editCityButton.setOnClickListener(view -> {
            City city = selectedCity;
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });


        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            selectedCity = city;
            //cityListView.setItemChecked(i, true);
            cityListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            //CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            //cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        String oldCityName = city.getName();
        city.setName(title);
        city.setProvince(year);
        cityArrayAdapter.notifyDataSetChanged();


        // Updating the database using delete + addition
        DocumentReference docRef = citiesRef.document(oldCityName);
        docRef.delete();
        docRef = citiesRef.document(city.getName());
        docRef.set(city);

    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);

//        citiesRef
//                .document(city.getName())
//                .set(city)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d("Firestore", "DocumentSnapshot successfully written!");
//                    }
//                });

    }

    public void deleteCity(City city){
        cityArrayList.remove(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete();
    }

    public void addDummyData(){
        City m1 = new City("Edmonton", "AB");
        City m2 = new City("Vancouver", "BC");
        cityArrayList.add(m1);
        cityArrayList.add(m2);
        cityArrayAdapter.notifyDataSetChanged();
    }
}