package moaz.mysunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import moaz.mysunshine.data.FetchWeatherTask;

/**
 * Created by XKCL0301 on 6/8/2016.
 */
public class ForecastFragment extends Fragment {

    public ArrayAdapter<String> adapter;
    public Context mContext;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mContext = getActivity();
//        String[] forcastArr = {"Today-Sunny-88/63", "Tomorrow-Foggy-70/46", "Weds-Cloudy-72/63", "Thurs-Rainy-64/51", "Fri-Foggy-70/46", "Sat-Sunny-76/68"};
//        List<String> forcastEntries = new ArrayList<>(Arrays.asList(forcastArr));
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item, R.id.list_item_forecast_textview, new ArrayList<String>());

        ListView list = (ListView) rootView.findViewById(R.id.listview_forecast);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(getActivity(),mAdapter.getItem(i),Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getActivity(), DetialActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, adapter.getItem(i));
                startActivity(intent);
            }
        });
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
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

    @Override
    public void onStart() {
        super.onStart();
        updateWeatherData();
    }

    private void updateWeatherData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        String unit = sharedPreferences.getString(getString(R.string.pref_unit_key),
                getString(R.string.pref_unit_default));
        new FetchWeatherTask(mContext,adapter).execute(location,unit);
    }

    }
