package com.example.android.sunshine.app;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by valeria on 2/13/15.
 */
public class WeatherDataParser {

    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex) throws JSONException{
        JSONObject jsonObject = new JSONObject(weatherJsonStr);
        JSONObject jsonObject1 = jsonObject.getJSONArray("list").getJSONObject(dayIndex).getJSONObject("temp");
        return jsonObject1.getDouble("max");
    }
}
