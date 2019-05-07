package com.ea.exploreathens.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.ea.exploreathens.ExpandableWeatherAdapter;
import com.ea.exploreathens.R;
import com.ea.exploreathens.RequestHelper;
import com.ea.exploreathens.code.CodeUtility;
import com.ea.exploreathens.code.WeatherForecast;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WeatherFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class WeatherFragment extends Fragment {


    private String default_place = "Athens,GR";

    private ExpandableListView listView;
    private ExpandableWeatherAdapter weatherAdapter;

    private SwipeRefreshLayout pullToRefresh;
    private TextView place, datetime;
    private View wView;

    private OnFragmentInteractionListener mListener;

    public WeatherFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_weather);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Toolbar toolbar = findViewById(R.id.app_toolbar);
        //TextView title = findViewById(R.id.toolbarTitle);
        //title.setText("Weatherforecast");
        //setSupportActionBar(toolbar);
        //setTitle("Weatherforecast");
        wView = inflater.inflate(R.layout.fragment_weather, container, false);

        // Drag down to refresh weather
        pullToRefresh = wView.findViewById(R.id.weatherlistRefreshLayout);
        pullToRefresh.setRefreshing(true);

        pullToRefresh.setOnRefreshListener(() -> {
            updateWeather();
        });

        listView = wView.findViewById(R.id.weatherListView);
        place = wView.findViewById(R.id.weatherlistPlace);
        datetime = wView.findViewById(R.id.weatherlistDateTime);

        updateWeather();
        return wView;
    }

    public void updateWeather(){
        listView.clearAnimation();
        new WeatherRequest().execute(CodeUtility.baseURL + "/weather/forecast/" + default_place);
    }

    public void setWeather(WeatherForecast forecast) {
        weatherAdapter = new ExpandableWeatherAdapter(getContext(), forecast);
        listView.setAdapter(weatherAdapter);
        listView.expandGroup(0, true);
        //listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
        //    Toast.makeText(this, forecast.getForecast().get(position).toString(), Toast.LENGTH_LONG).show();
        //});

        place.setText(forecast.getName());
        datetime.setText(getResources().getString(R.string.fragment_weather_updatedText) + " " + forecast.getDate() + " " + forecast.getTime());

        pullToRefresh.setRefreshing(false);
    }

    public void showError(String message){
        CodeUtility.buildError(getActivity(), message).show();
        pullToRefresh.setRefreshing(false);
    }

    /*@Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }*/

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

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

    class WeatherRequest extends AsyncTask< String, Void, String > {

        private String contentType;

        @Override
        protected String doInBackground(String... urls) {

            String responseType = "";

            try {
                if(!CodeUtility.internetAvailable(getContext()))
                    return "Internet not available";

                RequestHelper helper = new RequestHelper();
                helper.getRequestContent(urls[0]);
                contentType = helper.responseType;
                Log.d("json-response", helper.responseContent);
                Log.d("json-responsetype", helper.responseType);
                Log.d("json-responsecode", ""+helper.responseCode);

                return helper.responseContent;
            } catch (Exception e) {
                String err = (e.getMessage() == null) ? "SD Card failed" : e.getMessage();
                Log.e("connection-error", err);
                return "Error: No internet!";
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
                Log.e("error", ""+obj);
            } else if (obj instanceof WeatherForecast){
                // Otherwise a request to /sites must return AL<Site> so you can just cast
                // Sites
                WeatherForecast f = (WeatherForecast) obj;
                setWeather(f);

                Log.d("json-weatherforecast", f.toString());
            } else
                Log.e("json-error", "JSON Parse error. Type not found");
        }

    }

}
