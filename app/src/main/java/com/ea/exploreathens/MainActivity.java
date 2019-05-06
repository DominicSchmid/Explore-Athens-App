package com.ea.exploreathens;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Site;
import com.ea.exploreathens.fragments.MapsFragment;
import com.ea.exploreathens.fragments.SiteListFragment;
import com.ea.exploreathens.fragments.WeatherFragment;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            String title = "";
            //startActivity(new Intent(getApplicationContext(), SiteListActivity.class));

            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_maps:
                    //mTextMessage.setText(R.string.title_home);
                    title = getResources().getString(R.string.title_maps);
                    fragmentTransaction.replace(R.id.main_content, new MapsFragment(), "FragmentName");
                    break;
                case R.id.navigation_sitelist:
                    title = getResources().getString(R.string.title_sitelist);
                    fragmentTransaction.replace(R.id.main_content, new SiteListFragment(), "FragmentName");
                    //mTextMessage.setText(R.string.title_dashboard);
                    break;
                case R.id.navigation_weather:
                    title = getResources().getString(R.string.title_weather); // TODO string resource
                    fragmentTransaction.replace(R.id.main_content, new WeatherFragment(), "FragmentName");
                    break;
                    //mTextMessage.setText(R.string.title_notifications);
                case R.id.navigation_settings:
                    title = getResources().getString(R.string.title_settings);
                    fragmentTransaction.replace(R.id.main_content, new MySettings(), "FragmentName");
                    break;
            }


            setTitle(title);
            fragmentTransaction.commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        //mTextMessage = findViewById(R.id.message);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        do {
            ActivityCompat.requestPermissions(this, CodeUtility.permissions,
                    0);
        } while (missingMapsPermissions());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.replace(R.id.main_content, new WeatherFragment(), "FragmentName");
        fragmentTransaction.replace(R.id.main_content, new MapsFragment(), "FragmentName");
        setTitle("Home");
        fragmentTransaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Checks for result calls from activities
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    // Extract the data returned from the child Activity.
                    String returnValue = data.getStringExtra("routeTo");
                    if(returnValue != null){
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        //fragmentTransaction.replace(R.id.main_content, new WeatherFragment(), "FragmentName");
                        fragmentTransaction.replace(R.id.main_content, new MapsFragment(), "FragmentName");
                        setTitle("Home");
                        fragmentTransaction.commit();


                        Site s = CodeUtility.getSiteByName(returnValue);
                        // TODO an maps activity route senden
                        // TODO start fragment containing routeto string


                    }
                }
                break;
            }
        }
    }

    private boolean missingMapsPermissions() {
        Log.d("permissions", "Checking permissions");
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }


}
