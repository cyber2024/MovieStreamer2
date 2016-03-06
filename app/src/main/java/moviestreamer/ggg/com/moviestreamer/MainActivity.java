package moviestreamer.ggg.com.moviestreamer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import moviestreamer.ggg.com.moviestreamer.fragments.DetailFragment;
import moviestreamer.ggg.com.moviestreamer.helpers.DataPersistence;
import moviestreamer.ggg.com.moviestreamer.helpers.ServerSync;

public class MainActivity extends AppCompatActivity implements ServerSync.ServerCallback, DetailFragment.OnFragmentInteractionListener{

    private Spinner spinnerOrderBy;
    private GridView gridViewThumnails;
    public ArrayAdapter<String> spinnerAdapter;
    private String spinnerOrderByArray[];
    private ImageAdapter imageAdapter;
    private boolean mTwoPane;

    private JSONArray movieDataJsonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        spinnerOrderByArray = new String[]{ "Order By Popularity", "Order By Rating", "Favourites"};
        spinnerOrderBy = (Spinner) findViewById(R.id.spinnerOrderBy);
        gridViewThumnails = (GridView) findViewById(R.id.gridViewThumbnails);
        gridViewThumnails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("GridView OnItemClick", "Selected: " + position);
                if (movieDataJsonArray != null) {
                    Log.d("GridView", "MovieDataJsonArray not null");
                    try {
                        JSONObject tmpObj = movieDataJsonArray.getJSONObject(position);
                        if (tmpObj != null) {

                            if(mTwoPane){
                                FragmentManager fm = getSupportFragmentManager();
                                fm.beginTransaction()
                                        .replace(R.id.detailContainer, DetailFragment.newInstance("","", tmpObj.toString()), DetailFragment.TAG)
                                        .commit();
                            } else {
                                Intent intent = new Intent(getApplicationContext(), MovieDetails.class);
                                intent.putExtra("movieJsonString", tmpObj.toString());
                                Log.d("GridView", tmpObj.toString());
                                startActivity(intent);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, spinnerOrderByArray);
        spinnerOrderBy.setAdapter(spinnerAdapter);
        gridViewThumnails.setAdapter(imageAdapter = new ImageAdapter(this));

            Spinner spinner = (Spinner) findViewById(R.id.spinnerOrderBy);
            String spinnerValue = null;
            if(spinner.getSelectedItem().toString() == "Order By Rating"){
                spinnerValue = "rating";
            }else if(spinner.getSelectedItem().toString() == "Order By Popularity"){
                spinnerValue = "popularity";
            } else {
                spinnerValue = "favourites";
            }

        spinnerOrderBy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) findViewById(R.id.spinnerOrderBy);
                if (spinner.getSelectedItem().toString() == "Order By Rating") {
                    getMovieData("vote_average");
                } else if(spinner.getSelectedItem().toString() == "Order By Popularity"){
                    getMovieData("popularity");
                } else {
                    getMovieData("favourites");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if(findViewById(R.id.detailContainer) == null){
            mTwoPane = false;
        } else {
            mTwoPane = true;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getMovieData(final String orderBy){
        final Context context = this;
        if(orderBy == "favourites"){
            movieDataJsonArray = DataPersistence.getSavedArray(this);
            imageAdapter.updateURLs(movieDataJsonArray);

        } else {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    ServerSync.httpRequest(context, ServerSync.Method.GET, ServerSync.Endpoint.DISCOVER, orderBy);
                    return null;
                }
            }.execute();
        }
    }

    @Override
    public void onServerResponse(final ServerSync.Endpoint endpoint, final int responseCode, final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch(endpoint){
                    case DISCOVER:{

                        try {
                            movieDataJsonArray = new JSONObject(response).getJSONArray("results");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        imageAdapter.updateURLs(movieDataJsonArray);
                        break;
                    }
                    default:
                        FragmentManager fm = getSupportFragmentManager();
                        DetailFragment frag = (DetailFragment) fm.findFragmentByTag(DetailFragment.TAG);
                        frag.onServerResponse(endpoint, responseCode, response);
                        break;
                    }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
