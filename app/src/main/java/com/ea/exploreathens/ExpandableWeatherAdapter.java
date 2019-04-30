package com.ea.exploreathens;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ea.exploreathens.code.Weather;
import com.ea.exploreathens.code.WeatherForecast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ExpandableWeatherAdapter extends BaseExpandableListAdapter {

    Context context;
    public static ArrayList<ArrayList<Weather>> childList;
    private ArrayList<String> parents;

    public ExpandableWeatherAdapter(Context ctx, WeatherForecast forecast){
        this.context = ctx;
        this.childList = forecast.getForecastSplitByDays();
        this.parents = forecast.getDayNames();

        Log.d("weather", "Initialized Expandable Adapter with " + childList.size() + " children and divided into '" + parents.toString()  + "'");
    }

    @Override
    public int getGroupCount() {
        return parents.size();
    }

    @Override
    public int getChildrenCount(int parent) {
        return childList.get(parent).size();
    }

    @Override
    public String getGroup(int parent) {
        return parents.get(parent);
    }

    @Override
    public Weather getChild(int parent, int child) {
        return childList.get(parent).get(child);
    }

    @Override
    public long getGroupId(int parent) {
        return parent;
    }

    @Override
    public long getChildId(int parent, int child) {
        return child;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.weatherlist_parent,parent,false);

        TextView parentTv = convertView.findViewById(R.id.weatherlistParentDayTv);
        parentTv.setText(parents.get(groupPosition));

        if(groupPosition == 0)
            parentTv.setText("TODAY");

        TextView parentAvgTemp = convertView.findViewById(R.id.weatherlistParentAvgTemp);
        if(groupPosition < childList.size())
            parentAvgTemp.setText(WeatherForecast.getAvgTemperature(childList.get(groupPosition)) + "°C");




        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.weatherlist_item,parent,false);

        Weather weather = getChild(groupPosition, childPosition);

        listItem.setOnClickListener(v -> {
            Toast.makeText(context, weather.toString(), Toast.LENGTH_LONG).show();
        });

        ImageView image = (ImageView)listItem.findViewById(R.id.weatherlistIcon);
        // TODO  getImage by path from site.localpath
        Picasso.get().load("http://openweathermap.org/img/w/" + weather.getIcon() + ".png").into(image);
        //image.setImageResource(R.drawable.ic_launcher_background);
        //if(position == 0)
        //    image.setImageResource(R.drawable.img1);

        TextView time = listItem.findViewById(R.id.weatherlistTimeTv);
        time.setText(""+weather.getTime());

        TextView date = listItem.findViewById(R.id.weatherlistDateTv);
        date.setText(""+weather.getDate().getDayOfWeek().name());
        date.setVisibility(View.INVISIBLE);

        // Only show date if it is a different day than before
        /*if(childPosition > 0){
            Weather yesterday = weatherList.get(childPosition - 1);
            if(!weather.getDate().isEqual(yesterday.getDate())){
                date.setVisibility(View.VISIBLE);

            }
        } else
            date.setVisibility(View.VISIBLE);
*/
        TextView description = listItem.findViewById(R.id.weatherlistDescriptionTv);
        description.setText(weather.getDescription());

        TextView minTemp = listItem.findViewById(R.id.weatherlistMinTempTv);
        minTemp.setText(weather.getMin_temp() + "°C");

        TextView maxTemp = listItem.findViewById(R.id.weatherlistMaxTempTv);
        maxTemp.setText(weather.getMax_temp() + "°C");

        return listItem;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}

