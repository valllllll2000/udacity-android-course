package com.example.android.sunshine.app;

import android.app.Activity;
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
    public static final String SELECTED_ITEM_POSITION = "selected_item_position";
    private ForecastAdapter mForecastAdapter;
    private static final int LOADER_ID = 0;
    private Callback callback;
    private int mPosition;
    private ListView listView;
    private int selectedItemPosition = ListView.INVALID_POSITION;
    private boolean mUseTodayLayout;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof  Callback){
            callback = (Callback)activity;
        } else {
            throw new IllegalArgumentException("Activity must implement Callback");
        }
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
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        listView = (ListView) rootView.findViewById(R.id.listView_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedItemPosition = position;
                Cursor item1 = (Cursor) parent.getItemAtPosition(position);
                if (item1 != null) {
                    String locationStr = Utility.getPreferredLocation(getActivity());
                    Uri data = WeatherContract.WeatherEntry
                            .buildWeatherLocationWithStartDate(locationStr,
                                    item1.getLong(WeatherConstants.COL_WEATHER_DATE));
                    callback.onItemSelected(data);
                    mPosition = position;
                }
            }
        });

        selectedItemPosition = savedInstanceState == null || !savedInstanceState.containsKey(SELECTED_ITEM_POSITION) ? ListView.INVALID_POSITION : savedInstanceState.getInt(SELECTED_ITEM_POSITION);
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
        if(selectedItemPosition  != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(selectedItemPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged(){
        updateWeatherData();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;

        if(mForecastAdapter != null){
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(selectedItemPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_ITEM_POSITION, selectedItemPosition);
        }
        super.onSaveInstanceState(outState);
    }
}
