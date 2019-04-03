package com.prashant.neosoftdemo.Utils;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

import com.prashant.neosoftdemo.Pojjo.Place;
import com.prashant.neosoftdemo.Pojjo.Weather;
import com.prashant.neosoftdemo.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;

public class CommonUtils
{
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    public static boolean checkGps(Context context)
    {
        boolean flag;
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        flag = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(flag && (getLocationMode(context) == 3) )
        {
            flag = true;
        }
        else
        {
            if(getLocationMode(context) != 3)
            {
                Toast.makeText(context, "" + context.getResources().getString(R.string.set_gps_accurate), Toast.LENGTH_LONG).show();
            }
            flag = false;
        }

        return flag;
    }

    public static int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();

        }
        return 0;
    }

    public static ArrayList<Place> parseResponse(JSONArray data)
    {
        JSONObject object;
        Place place = null;
        JSONArray weatherData;
        ArrayList<Place> placeArrayList = new ArrayList<>();

        for(int i = 0; i < data.length();i++)
        {
            try
            {
                object = data.getJSONObject(i);
                place = new Place();

                place.setId(object.getLong("id"));
                place.setName(object.getString("name"));


                place.setLat(object.getJSONObject("coord").getDouble("lat"));
                place.setLng(object.getJSONObject("coord").getDouble("lon"));

                place.setTemp(object.getJSONObject("main").getDouble("temp"));
                place.setPressure(object.getJSONObject("main").getLong("pressure"));
                place.setHumidity(object.getJSONObject("main").getLong("humidity"));
                place.setTemp_min(object.getJSONObject("main").getDouble("temp_min"));
                place.setTemp_max(object.getJSONObject("main").getDouble("temp_max"));

                place.setDt(object.getLong("dt"));

                place.setSpeed(object.getJSONObject("wind").getDouble("speed"));
                place.setDeg(object.getJSONObject("wind").getLong("deg"));

                place.setCountry(object.getJSONObject("sys").getString("country"));

                place.setRain(object.getString("rain"));
                place.setSnow(object.getString("snow"));

                place.setAll(object.getJSONObject("clouds").getLong("all"));

                weatherData = object.getJSONArray("weather");
                JSONObject weatherObj;
                Weather weather;
                ArrayList<Weather> weatherList = new ArrayList<>();

                for(int j = 0; j < weatherData.length(); j++)
                {
                    weatherObj = weatherData.getJSONObject(i);
                    weather = new Weather();

                    weather.setId(weatherObj.getLong("id"));
                    weather.setDesc(weatherObj.getString("description"));
                    weather.setMain(weatherObj.getString("main"));
                    weather.setIcon(weatherObj.getString("icon"));
                    weatherList.add(weather);
                }
                place.setWeatherListl(weatherList);
                
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            placeArrayList.add(place);
        }

        return placeArrayList;
    }
}
