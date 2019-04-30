package com.ea.exploreathens;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ea.exploreathens.code.Weather;
import com.ea.exploreathens.code.WeatherForecast;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class WeatherAdapter extends ArrayAdapter<Weather> {

    private Context mContext;
    private List<Weather> weatherList;

    public WeatherAdapter(@NonNull Context context, WeatherForecast forecast) {
        super(context, 0 , forecast.getForecast());
        mContext = context;
        weatherList = forecast.getForecast();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.weatherlist_item,parent,false);

        Weather weather = weatherList.get(position);

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
        if(position > 0){
            Weather yesterday = weatherList.get(position - 1);
            if(!weather.getDate().isEqual(yesterday.getDate())){
                date.setVisibility(View.VISIBLE);

            }
        } else
            date.setVisibility(View.VISIBLE);

        TextView description = listItem.findViewById(R.id.weatherlistDescriptionTv);
        description.setText(weather.getDescription());

        TextView minTemp = listItem.findViewById(R.id.weatherlistMinTempTv);
        minTemp.setText(weather.getMin_temp() + "°C");

        TextView maxTemp = listItem.findViewById(R.id.weatherlistMaxTempTv);
        maxTemp.setText(weather.getMax_temp() + "°C");

        return listItem;
    }

    public void updateSites(ArrayList<Weather> weathers){
        weatherList.clear();
        weathers.forEach(e->{
            this.add(e);
        });
        Log.d("weather", "Weather updated with " + weathers.size() + " weathers");
    }


}
