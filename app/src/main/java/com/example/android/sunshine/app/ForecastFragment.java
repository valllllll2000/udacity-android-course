package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherConstants;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    protected static final String TAG = ForecastFragment.class.getSimpleName();
    private ForecastAdapter mForecastAdapter;
    private static final int LOADER_ID = 0;


    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                updateWeatherData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeatherData() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = defaultSharedPreferences
                .getString(getString(R.string.pref_location_key),
                        getString(R.string.pref_location_default));
        String metric = defaultSharedPreferences.getString(getString(R.string.pref_units_key),
                getString(R.string.pref_units_metric));
        Log.d(TAG, "Show temperature in "+metric);
        new FetchWeatherTask(getActivity()).execute(location, metric);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        ListView lv = (ListView) rootView.findViewById(R.id.listView_forecast);
        lv.setAdapter(mForecastAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Cursor item1 = (Cursor) parent.getItemAtPosition(position);
               if(item1 != null) {
                   String locationStr =  Utility.getPreferredLocation(getActivity());
                   Intent intent = new Intent(getActivity(), DetailActivity.class)
                           .setData(WeatherContract.WeatherEntry
                                   .buildWeatherLocationWithStartDate(locationStr,
                                           item1.getLong(WeatherConstants.COL_WEATHER_DATE)));
//                   intent.putExtra(Constants.WEATHER_DATA, item);
                   startActivity(intent);
               }
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForecastForLocationUri = WeatherContract.WeatherEntry
                .buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(),
                weatherForecastForLocationUri,
                WeatherConstants.FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged(){
        updateWeatherData();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

}
