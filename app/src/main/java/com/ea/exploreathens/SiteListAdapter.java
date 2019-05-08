package com.ea.exploreathens;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Site;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;


public class SiteListAdapter extends ArrayAdapter<Site> {

    private Context mContext;
    private ArrayList<Site> siteList; // Sitelist containing visible sites
    private ArrayList<Site> orig = new ArrayList<>(); // Original list containing all sites at all times

    public SiteListAdapter(@NonNull Context context, ArrayList<Site> list) {
        super(context, 0 , list);
        mContext = context;
        siteList = list;
        orig.addAll(list);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.sitelist_item,parent,false);

        //listItem.setTag(getItemId(position));
        Site site = siteList.get(position);

        ImageView image = listItem.findViewById(R.id.sitelistImage);
        try {
            File file = new File(site.getImageLocalPath(0));
            Picasso.get().load(file).into(image);
        } catch(Exception e){
            // File was not downloaded yet
        }

        if(!CodeUtility.internetAvailable(mContext)){
            // TODO Wenn kein internet lese aus lokalen dateien aus
            // nicht nur images sondern auch json sites mit infos
        } else
            Picasso.get().load(CodeUtility.baseURL + "/image/" + site.getImageRequestPaths().get(0)).into(image);
        //displayImage(site, image);
        //MainActivity.downloadCovers(mContext, site);
        //
        //ImageRequest ir = new ImageRequest(mContext, site, image);
        //ir.execute();

        // Display file from internal storage

        TextView name = listItem.findViewById(R.id.sitelistNameTv);
        name.setText(site.getName());

        TextView address = listItem.findViewById(R.id.sitelistStreetTv);
        String street = site.getAddress();
        if(street.length() > 30)
            street = street.substring(0, 30-3) + "...";

        address.setText(street);
        Log.d("distance", "Current position: " + CodeUtility.curlat + " " + CodeUtility.curlng + " " + site.getX() + " " + site.getY());
        TextView distance = listItem.findViewById(R.id.sitelistDistanceTv);

        double dist = CodeUtility.haversine(CodeUtility.curlat, CodeUtility.curlng, site.getX(), site.getY()) / 1000;
        Log.d("distance", site.getName() + ": "+dist);
        if(dist > 0 && dist < 10000 * 1000) { // Entfernung nicht mehr als 3000 km
            distance.setVisibility(View.VISIBLE);
            distance.setText(String.format("%.2f", dist) + " km");
            site.setDistance(dist);
        } else
            distance.setVisibility(View.INVISIBLE);


        return listItem;
    }

    @Override
    public Filter getFilter() {
        return new SiteFilter();
    }

    class SiteFilter extends Filter {

        private SiteFilter filter;

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            constraint = constraint.toString().trim().toLowerCase();

            FilterResults oReturn = new FilterResults();
            Log.d("filter", "filtering for " + constraint);
            if (constraint != null && constraint.toString().length() > 0) {
                ArrayList<Site> results = new ArrayList<>();
                if (orig != null && orig.size() > 0) {
                    for(Site s : orig) {
                        Site result = s.search(""+constraint);
                        if(result != null)
                            results.add(s);
                    }
                }
                Log.d("filter", "Found: " + results.toString());
                oReturn.values = results;
                oReturn.count = results.size();
            } else {
                oReturn.values = orig;
                oReturn.count = orig.size();
            }
            return oReturn;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            Log.d("filter", "updating list with " + results.values.toString());

            updateSites((ArrayList<Site>) results.values);
        }

    }

    public void updateSites(ArrayList<Site> sites){
        clear();
        notifyDataSetChanged();
        sites.forEach(e->{
            this.add(e);
        });
        notifyDataSetInvalidated();
        Log.d("sites", "Sites updated with " + sites.size() + " sites");
    }

}