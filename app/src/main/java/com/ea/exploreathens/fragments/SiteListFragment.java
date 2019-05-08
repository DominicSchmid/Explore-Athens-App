package com.ea.exploreathens.fragments;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import com.ea.exploreathens.R;
import com.ea.exploreathens.RequestHelper;
import com.ea.exploreathens.SiteActivity;
import com.ea.exploreathens.SiteListAdapter;
import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Site;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SiteListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SiteListFragment extends Fragment {


    ListView listView;
    SiteListAdapter siteAdapter;
    ConstraintLayout searchBar;

    SwipeRefreshLayout pullToRefresh;

    private OnFragmentInteractionListener mListener;

    View wView;

    public SiteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        updateSites();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        wView = inflater.inflate(R.layout.fragment_site_list, container, false);

        listView = wView.findViewById(R.id.sitelist);
        pullToRefresh = wView.findViewById(R.id.sitelistRefreshLayout);
        pullToRefresh.setRefreshing(true);

        pullToRefresh.setOnRefreshListener(() -> {
            // First delete old images
            ContextWrapper cw = new ContextWrapper(getContext());
            try {
                FileUtils.deleteDirectory(cw.getDir("siteImages", Context.MODE_PRIVATE));

                // UPDATE LOCATION
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                String provider = locationManager.getBestProvider(new Criteria(), false);

                @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);
                if (location != null){
                    CodeUtility.curlat = location.getLatitude();
                    CodeUtility.curlng = location.getLongitude();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateSites();
        });

        SearchView searchStuff = wView.findViewById(R.id.toolbarSearch);
        searchStuff.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("asf", "");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("search", newText);
                if(siteAdapter != null)
                    siteAdapter.getFilter().filter(newText);

                return false;
            }
        });


        return wView;
    }

    public void updateSites(){
        Log.d("info", "updating sites...");
        new SiteRequest().execute(CodeUtility.baseURL + "/sites?lan=" + CodeUtility.getLocale(getContext())); // TODO put coordinates in request?
    }

    private void createNotificationChannel(String name) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "Description of " + name;
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(name, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            CodeUtility.NOTIFICATION_CHANNEL = name;
        }
    }

    public void setSites(ArrayList<Site> sites) {
        // Called after a GET request for sites gets the result
        listView.clearAnimation();
        CodeUtility.setSites(sites);

        siteAdapter = new SiteListAdapter(getContext(),sites);
        listView.setAdapter(siteAdapter);
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id)-> {
            Intent intent = new Intent(getContext(), SiteActivity.class);
            Bundle b = new Bundle();
            b.putInt("siteindex", position);
            intent.putExtras(b); //Put your id to your next Intent
            startActivityForResult(intent, 1);
        });

        pullToRefresh.setRefreshing(false);
    }

    public void showError(String message){
        CodeUtility.buildError(getActivity(), message).show();
        pullToRefresh.setRefreshing(false);
    }


    public static String saveImageToFile(Context context, Bitmap image, String file) {
        ContextWrapper cw = new ContextWrapper(context);
        File directory = cw.getDir("siteImages", Context.MODE_PRIVATE);
        String localpath = "";

        if(!directory.exists())
            directory.mkdirs();

        try {
            File f = new File(directory, file);
            FileOutputStream out = new FileOutputStream(f);
            image.compress(Bitmap.CompressFormat.JPEG, 70, out);

            Log.d("image", "Saved image to '" + f.getAbsolutePath() + "'");

            out.flush();
            out.close();
            return f.getAbsolutePath();
        } catch (IOException io) {
            Log.e("image-io-exception", io.toString());
        }
        return localpath;
    }

 /*   @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
*/
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    class SiteRequest extends AsyncTask< String, Void, String > {

        private String contentType;

        @Override
        protected String doInBackground(String... urls) {

            String responseType = "";

            try {
                if(!CodeUtility.internetAvailable(getContext()))
                    return "Internet not available";

                RequestHelper helper = new RequestHelper();
                helper.getRequestContent(urls[0]);
                Log.d("connection-helper", "Helper " + helper.toString() + " returned ");
                contentType = helper.responseType;

                Log.d("json-response", helper.responseContent);
                Log.d("json-responsetype", helper.responseType);
                Log.d("json-responsecode", ""+helper.responseCode);

                return helper.responseContent;
            } catch (Exception e) {
                e.printStackTrace();
                //String err = (e.getMessage() == null) ? "SD Card failed" : e.getMessage();
                //og.e("connection-error", ""+err);
                return "The operation can not be performed on the selected server (" + CodeUtility.baseURL + ")";
            }

        }

        @Override
        protected void onPostExecute(String response){
            if(getContext() == null)
                return;

            Object obj = CodeUtility.getJSON(contentType, response);

            if(obj instanceof String) {
                // Error
                showError(""+obj);
                Log.e("error", obj.toString());
                // If response is String an error occured and you can read it by calling string
            } else if (obj instanceof ArrayList){
                // Otherwise a request to /sites must return AL<Site> so you can just cast
                // Sites
                ArrayList<Site> sites = (ArrayList<Site>) obj;
                setSites(sites);

                Log.d("json-sites", sites.toString());
            } else
                Log.e("json-error", "JSON Parse error. Type not found");
        }

    }

}
