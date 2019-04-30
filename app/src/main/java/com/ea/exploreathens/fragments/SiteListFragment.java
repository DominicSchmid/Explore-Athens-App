package com.ea.exploreathens.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ea.exploreathens.R;
import com.ea.exploreathens.RequestHelper;
import com.ea.exploreathens.SiteActivity;
import com.ea.exploreathens.SiteListAdapter;
import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.Site;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

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

    /* TODO Beim Start der app soll ein site request durchgeführt werden, um die sites zu aktualisieren,
    TODO wenn man auf z.b. weather klickt, wird ein weather request durchgeführt und bei der Antwort wird die Weatheractivity gestartet */

    SwipeRefreshLayout pullToRefresh;

    private OnFragmentInteractionListener mListener;

    View wView;

    public SiteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        wView = inflater.inflate(R.layout.fragment_site_list, container, false);

        listView = wView.findViewById(R.id.sitelist);
        pullToRefresh = wView.findViewById(R.id.sitelistRefreshLayout);
        pullToRefresh.setRefreshing(true);

        updateSites();

        pullToRefresh.setOnRefreshListener(() -> {
            // First delete old images
            ContextWrapper cw = new ContextWrapper(getContext());
            try {
                FileUtils.deleteDirectory(cw.getDir("siteImages", Context.MODE_PRIVATE));
            } catch (IOException e) {
                e.printStackTrace();
            }

            updateSites();
            // pullToRefresh.setRefreshing(false);
        });

        /*try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch(Exception omegalul){
            Log.e("Site", "Backbutton failed!");
        }*/

       //FloatingActionButton fab = wView.findViewById(R.id.siteRouteBtn);
       // fab.setOnClickListener(view -> {
            /*Snackbar.make(view, "Route berechnen...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("routeTo", sitename); // Returns routeTo - sitename to the parent activity
            setResult(Activity.RESULT_OK, resultIntent);
            finish();*/
            // TODO move sendlocation somewhere else and put route here
            //showSendLocation(this);
            //Snackbar.make(view, site.getImageLocalPaths().toString(), Snackbar.LENGTH_LONG).show();
       // });


        SearchView searchStuff = wView.findViewById(R.id.toolbarSearch);
        /*searchStuff.setOnSearchClickListener(e->{
            getActivity().getSupportActionBar().setDisplayShowTitleEnabled(false);
            // title.setVisibility(View.INVISIBLE);
        });
        searchStuff.setOnCloseListener(()->{
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            //title.setVisibility(View.VISIBLE);
            return false;
        });*/

        searchStuff.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("asf", "");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("search", newText);
                siteAdapter.getFilter().filter(newText);
                return false;
            }
        });


        return wView;
    }

    public void updateSites(){
        Log.d("info", "updating sites...");
        new SiteRequest().execute(CodeUtility.baseURL + "/sites"); // TODO put coordinates in request?
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
        CodeUtility.sites = sites;

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
        CodeUtility.showError(getContext(), message);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    class SiteRequest extends AsyncTask< String, Void, String > {

        private String contentType;

        @Override
        protected String doInBackground(String... urls) {

            String responseType = "";

            try {
                //if(!CodeUtility.internetAvailable(getContext()))
                //    return "Internet not available";

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
                return "Oh no, an error occured :(";
            }

        }

        @Override
        protected void onPostExecute(String response){
            Object obj = getJSON(response);

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

        public Object getJSON(String responseContent){
            JSONParser parser = new JSONParser();

            try {
                // Depending on response-type return correct object
                if (contentType.equalsIgnoreCase("sites")) {
                    JSONArray ja = (JSONArray) parser.parse(responseContent);

                    ArrayList<Site> list = Site.parse(ja);
                    System.out.println(list.toString());
                    return list;
                }
            } catch (Exception ex) {
                String err = (ex.getMessage() == null) ? "SD Card failed" : ex.getMessage();
                ex.printStackTrace();
                Log.e("json-parse-error:", "Could not parse JSON response. Error: "+err);
            }

            return responseContent;
        }

    }

}
