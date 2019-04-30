package com.ea.exploreathens;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.TextView;

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
                    break;
                case R.id.navigation_sitelist:
                    title = "Sitelist";
                    fragmentTransaction.replace(R.id.main_content, new SiteListFragment(), "FragmentName");
                    //mTextMessage.setText(R.string.title_dashboard);
                    break;
                case R.id.navigation_weather:
                    title = "Weatherforecast"; // TODO string resource
                    fragmentTransaction.replace(R.id.main_content, new WeatherFragment(), "FragmentName");
                    break;
                    //mTextMessage.setText(R.string.title_notifications);
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

        // TODO check permissions

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //fragmentTransaction.replace(R.id.main_content, new WeatherFragment(), "FragmentName");
        fragmentTransaction.replace(R.id.main_content, new SiteListFragment(), "FragmentName");
        setTitle("Home");
        fragmentTransaction.commit();
    }



}
