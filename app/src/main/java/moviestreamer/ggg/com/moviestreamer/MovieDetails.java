package moviestreamer.ggg.com.moviestreamer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import moviestreamer.ggg.com.moviestreamer.helpers.DataPersistence;
import moviestreamer.ggg.com.moviestreamer.helpers.ServerSync;

public class MovieDetails extends AppCompatActivity implements ServerSync.ServerCallback{
    public static final String TAG = MovieDetails.class.getSimpleName();

    private TextView year, length, title, synopsis, rating;
    private ImageView poster;
    private String posterURL, backdropPath;
//    public ArrayAdapter<String> aaReviews;
//    public ListView lvReviews;
    private Button btnTrailer;
    private CheckBox cbStar;
    private String trailerKey = "";
    private boolean isSelected = false;

    public LinearLayout llReviewLoading, llTrailerLoading, llReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        year = (TextView)findViewById(R.id.year);
        title = (TextView)findViewById(R.id.title);
        synopsis = (TextView)findViewById(R.id.synopsis);
        rating = (TextView)findViewById(R.id.rating);
        poster = (ImageView)findViewById(R.id.poster);

        cbStar = (CheckBox) findViewById(R.id.cbStar);

        Intent intent = getIntent();
        String jsonString = getIntent().getStringExtra("movieJsonString");
        JSONObject ob;
        try {
            ob = new JSONObject(jsonString);
            cbStar.setChecked(DataPersistence.isItInFavourites(this, ob));
        } catch (JSONException e) {
            e.printStackTrace();
            ob = null;
        }
        final JSONObject finalMovieObject = ob;
        Log.d("JasonString from Intent", jsonString);
        Button button = new Button(this);

        try{
            JSONObject movie = new JSONObject(jsonString);
            Log.d("Grab yearJSON OBject", movie.getString("release_date"));
            if(movie.getString("release_date") == "null") {
                year.setText("TBA");
            } else {
                try {
                    year.setText(movie.getString("release_date").substring(0, 4));
                } catch (StringIndexOutOfBoundsException e){
                    e.printStackTrace();
                    year.setText(movie.getString("release_date"));
                }
            }

            final String movieId = movie.getString("id");
            final Context context = this;
            new AsyncTask<Void, Void, Void>(){
                protected Void doInBackground(Void... params) {
                    ServerSync.httpRequest(context, ServerSync.Method.GET, ServerSync.Endpoint.TRAILERS, movieId);
                    return null;
                }
            }.execute();
            new AsyncTask<Void, Void, Void>(){
                protected Void doInBackground(Void... params) {
                    ServerSync.httpRequest(context, ServerSync.Method.GET, ServerSync.Endpoint.REVIEWS, movieId);
                    return null;
                }
            }.execute();

            title.setText(movie.getString("original_title"));
            if(movie.getString("overview") == "null"){
                synopsis.setText("Synopsis: TBA");
            } else {
                synopsis.setText(movie.getString("overview"));
            }
            if(movie.getString("vote_average") == "null") {
                rating.setText("Rating: TBA");
            } else {
                rating.setText(movie.getString("vote_average") + "/10");
            }
            posterURL = "http://image.tmdb.org/t/p/w185" + movie.getString("poster_path");
            backdropPath = "http://image.tmdb.org/t/p/w300" + movie.getString("backdrop_path");
            poster = (ImageView) findViewById(R.id.poster);
            Picasso.with(poster.getContext())
                    .load(posterURL)
                    .placeholder(R.drawable.ic_hourglass128)
                    .error(R.drawable.ic_404)
                    .into(poster);
        } catch (JSONException e){
            e.printStackTrace();
        }

        llReviews = (LinearLayout) findViewById(R.id.llReviews);

        btnTrailer = (Button) findViewById(R.id.btnTrailer);
        btnTrailer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerKey)));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailerKey)));
                }
            }
        });
        btnTrailer.setEnabled(false);
        btnTrailer.setVisibility(View.GONE);
        llReviewLoading = (LinearLayout) findViewById(R.id.llReviewsLoadingIcon);
        llTrailerLoading = (LinearLayout) findViewById(R.id.llTrailerLoadingIcon);

        final Context context = this;
        cbStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataPersistence.toggleFavouriteToSharedPreferences(context, finalMovieObject);
            }
        });

    }

    @Override
    public void onServerResponse(ServerSync.Endpoint endpoint, int responseCode, String responseString) {
        final Context context = this;
        switch (endpoint){
            case TRAILERS:
                try {
                    JSONObject response = new JSONObject(responseString);
                    JSONArray ja = response.getJSONArray("results");
                    JSONObject ob = ja.getJSONObject(0);
                    trailerKey = ob.getString("key");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            llTrailerLoading.setVisibility(View.GONE);
                            btnTrailer.setVisibility(View.VISIBLE);
                            btnTrailer.setEnabled(true);
                            btnTrailer.setText("Play Trailer");
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            llTrailerLoading.setVisibility(View.GONE);
                            btnTrailer.setVisibility(View.VISIBLE);
                            btnTrailer.setText("Trailer unavailable...");
                        }
                    });
                }
                break;
            case REVIEWS:
                try {
                    JSONObject response = new JSONObject(responseString);
                    final JSONArray ja = response.getJSONArray("results");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for(int i = 0; i < ja.length(); i++){
                                try {
                                    JSONObject ob = ja.getJSONObject(i);
                                    TextView tv = new TextView(context);
                                    tv.setText(ob.getString("author") + ": \n" + ob.getString("content"));
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    params.setMargins(0,32,0,0);
                                    tv.setLayoutParams(params);
                                    llReviews.addView(tv);
                                    Log.d(TAG, ob.getString("author")+": \n"+ob.getString("content"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(ja.length() == 0){
                                TextView tv = new TextView(context);
                                tv.setText("No reviews available.");
                                llReviews.addView(tv);
                            } else {
                                Log.d(TAG, "Reviews found: "+ja.length());
                            }
                            llReviewLoading.setVisibility(View.GONE);
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tv = new TextView(context);
                            tv.setText("No reviews available.");
                            llReviews.addView(tv);
                            llReviewLoading.setVisibility(View.GONE);
                        }
                    });
                }
                break;
        }

    }
}
