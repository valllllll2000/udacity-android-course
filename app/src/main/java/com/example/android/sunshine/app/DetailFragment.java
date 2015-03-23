package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherConstants;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    public static final String FORECAST_SHARE_HASHTAG = "#SunshineApp";
    private static final String TAG = DetailFragment.class.getSimpleName();
    public static final String DATE_URI = "DATE_URI";
    private String weatherData;
    private static final int LOADER_ID = 1;
    private TextView detailTextview;
    private ShareActionProvider mShareActionProvider;
    private TextView dateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView pressureTextView;
    private TextView mFriendlyDateView;
    private Uri mUri;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Constants.WEATHER_DATA)) {
            weatherData = intent.getStringExtra(Constants.WEATHER_DATA);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_fragment, menu);

        MenuItem shareItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        if (mShareActionProvider != null) {
            if (weatherData != null) {
                mShareActionProvider.setShareIntent(getWeatherIntent());
            }
        } else {
            Log.w(TAG, "Share action provider is " + mShareActionProvider);
        }
    }

    private Intent getWeatherIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, weatherData + FORECAST_SHARE_HASHTAG);
        return intent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        dateView = (TextView) rootView.findViewById(R.id.list_item_date_textview);
        mFriendlyDateView = (TextView) rootView.findViewById(R.id.list_item_day_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.list_item_forecast_textview);
        highTempView = (TextView) rootView.findViewById(R.id.list_item_high_textview);
        lowTempView = (TextView) rootView.findViewById(R.id.list_item_low_textview);
        humidityTextView = (TextView) rootView.findViewById(R.id.list_item_humidity);
        windTextView = (TextView) rootView.findViewById(R.id.list_item_wind);
        pressureTextView = (TextView) rootView.findViewById(R.id.list_item_pressure);
        Bundle b = getArguments();
        if(b != null){
            mUri = b.getParcelable(DATE_URI);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if(null == mUri){
            return null;
        }
        return new CursorLoader(
                getActivity(),
                mUri,
                WeatherConstants.FORECAST_COLUMNS_DETAIL,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            View view = getView();
            ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            iconView.setImageResource(Utility.getResourseFromWeatherId(cursor.getInt(WeatherConstants.COL_WEATHER_CONDITION_ID), 0));
            Context context = getActivity();
            long date = cursor.getLong(WeatherConstants.COL_WEATHER_DATE);
            String friendlyDateText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            mFriendlyDateView.setText(friendlyDateText);
            dateView.setText(dateText);
            descriptionView.
                    setText(cursor.getString(WeatherConstants.COL_WEATHER_DESC));
            boolean metric = Utility.isMetric(context);
            highTempView.
                    setText(Utility.formatTemperature(context,
                            cursor.getDouble(WeatherConstants.COL_WEATHER_MAX_TEMP), metric));
            lowTempView.
                    setText(Utility.formatTemperature(context,
                            cursor.getDouble(WeatherConstants.COL_WEATHER_MIN_TEMP), metric));
            humidityTextView.setText(Utility.formatHumidity(context, cursor.getDouble(WeatherConstants.COL_HUMIDITY)));
            windTextView.setText(Utility.getFormattedWind(context, cursor.getFloat(WeatherConstants.COL_WIND),
                    cursor.getFloat(WeatherConstants.COL_DEGREES)));
            pressureTextView.setText(Utility.formatPressure(context, cursor.getDouble(WeatherConstants.COL_PRESSURE)));
            weatherData = Utility.convertCursorRowToUXFormat(cursor, getActivity());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
    }

    public static DetailFragment newInstance(Uri dateUri) {
        DetailFragment df =  new DetailFragment();
        Bundle bundle =  new Bundle();
        bundle.putParcelable(DATE_URI, dateUri);
        df.setArguments(bundle);
        return df;
    }

//    public void updateUri(Uri dateUri) {
//        mUri = dateUri;
//        getLoaderManager().restartLoader(LOADER_ID, null, this);
//    }
}
