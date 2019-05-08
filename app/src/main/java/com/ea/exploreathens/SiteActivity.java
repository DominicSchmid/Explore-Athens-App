package com.ea.exploreathens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Site;

public class SiteActivity extends AppCompatActivity {

    Site site;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site);

        Bundle b = getIntent().getExtras();

        // Liest die Site aus der übergebenen Index aus, um die Activity mit infos zu füllen
        if(b != null){
            site = CodeUtility.getSites().get(b.getInt("siteindex"));
            Log.d("info", "Got site through bundle: " +  site.toString());

            TextView name = findViewById(R.id.siteNameTv);
            TextView address = findViewById(R.id.siteAddressTv);
            TextView description = findViewById(R.id.siteDescriptionTv);

            // Set local image paths for this site
            ViewPager viewPager = findViewById(R.id.viewPager);
            ImageAdapter imageAdapter = new ImageAdapter(this, site.getImageRequestPaths());
            viewPager.setAdapter(imageAdapter);

            double dist = site.getDistance();
            if(dist > 0 && dist < 5000 * 1000)
                name.setText(site.getName() + " (" + String.format("%.2f", dist) + " km)");
            else
                name.setText(site.getName());
            address.setText(site.getAddress());
            description.setText(site.getDescription());
        }

        FloatingActionButton fab = findViewById(R.id.siteRouteBtn);
        fab.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("routeTo", site.getName()); // Returns routeTo - sitename to the parent activity
            setResult(Activity.RESULT_OK, resultIntent);

            Toast.makeText(this, getResources().getString(R.string.route_calculating) + " (" + String.format("%.2f", site.getDistance()) + " km)", Toast.LENGTH_LONG).show();

            finish();
        });
    }

}
