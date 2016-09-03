package moaz.mysunshine;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import moaz.mysunshine.data.FetchWeatherTask;
import moaz.mysunshine.data.WeatherContract;

/**
 * Created by XKCL0301 on 6/8/2016.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String SELECTION_POS = "SELCTION_POS";
    public ForecastAdapter mForecastAdapter;
    public Context mContext;
    private static final int FORECAST_LOADER = 0;
    private int mPos;
    boolean mUseTodayLayout = false;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    ListView list;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mContext = getActivity();
//        String[] forcastArr = {"Today-Sunny-88/63", "Tomorrow-Foggy-70/46", "Weds-Cloudy-72/63", "Thurs-Rainy-64/51", "Fri-Foggy-70/46", "Sat-Sunny-76/68"};
//        List<String> forcastEntries = new ArrayList<>(Arrays.asList(forcastArr));
//        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());

        final String locationSetting = Utility.getPreferredLocation(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                FORECAST_COLUMNS, null, null, sortOrder);

        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        list = (ListView) rootView.findViewById(R.id.listview_forecast);
        list.setAdapter(mForecastAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                }
                mPos = position;
            }
        });
        setHasOptionsMenu(true);
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTION_POS)) {
            mPos = savedInstanceState.getInt(SELECTION_POS);
        }
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPos != ListView.INVALID_POSITION) {
            outState.putInt(SELECTION_POS, mPos);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeatherData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onLocationChange() {
        updateWeatherData();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateWeatherData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        String unit = sharedPreferences.getString(getString(R.string.pref_unit_key),
                getString(R.string.pref_unit_default));
        new FetchWeatherTask(mContext).execute(location, unit);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(mContext);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());
        return new CursorLoader(mContext, weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
        if (mPos != ListView.INVALID_POSITION) {
            list.setSelection(mPos);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mForecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
