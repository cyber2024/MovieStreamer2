package moviestreamer.ggg.com.moviestreamer.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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

import moviestreamer.ggg.com.moviestreamer.R;
import moviestreamer.ggg.com.moviestreamer.helpers.DataPersistence;
import moviestreamer.ggg.com.moviestreamer.helpers.ServerSync;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static final String TAG = DetailFragment.class.getSimpleName();

    private TextView year, length, title, synopsis, rating;
    private ImageView poster;
    private String posterURL, backdropPath;
    //    public ArrayAdapter<String> aaReviews;
//    public ListView lvReviews;
    private Button btnTrailer;
    private CheckBox cbStar;
    private String trailerKey = "";
    private boolean isSelected = false;
    private String movieJsonString;
    private View rootView;

    public LinearLayout llReviewLoading, llTrailerLoading, llReviews;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String param1, String param2, String movieJsonString) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString("movieJsonString", movieJsonString);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            movieJsonString = getArguments().getString("movieJsonString");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        year = (TextView)rootView.findViewById(R.id.year);
        title = (TextView)rootView.findViewById(R.id.title);
        synopsis = (TextView)rootView.findViewById(R.id.synopsis);
        rating = (TextView)rootView.findViewById(R.id.rating);
        poster = (ImageView)rootView.findViewById(R.id.poster);

        cbStar = (CheckBox) rootView.findViewById(R.id.cbStar);

        updateData(movieJsonString);

        // Inflate the layout for this fragment
        return rootView;
    }

    public void updateData(String jsonString){
        JSONObject ob;
        try {
            ob = new JSONObject(jsonString);
            cbStar.setChecked(DataPersistence.isItInFavourites(getContext(), ob));
        } catch (JSONException e) {
            e.printStackTrace();
            ob = null;
        }
        final JSONObject finalMovieObject = ob;
        Log.d("JasonString from Intent", jsonString);
        Button button = new Button(getContext());

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
            new AsyncTask<Void, Void, Void>(){
                protected Void doInBackground(Void... params) {
                    ServerSync.httpRequest(getContext(), ServerSync.Method.GET, ServerSync.Endpoint.TRAILERS, movieId);
                    return null;
                }
            }.execute();
            new AsyncTask<Void, Void, Void>(){
                protected Void doInBackground(Void... params) {
                    ServerSync.httpRequest(getContext(), ServerSync.Method.GET, ServerSync.Endpoint.REVIEWS, movieId);
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
            poster = (ImageView) rootView.findViewById(R.id.poster);
            Picasso.with(poster.getContext())
                    .load(posterURL)
                    .placeholder(R.drawable.ic_hourglass128)
                    .error(R.drawable.ic_404)
                    .into(poster);
        } catch (JSONException e){
            e.printStackTrace();
        }

        llReviews = (LinearLayout) rootView.findViewById(R.id.llReviews);

        btnTrailer = (Button) rootView.findViewById(R.id.btnTrailer);
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
        llReviewLoading = (LinearLayout) rootView.findViewById(R.id.llReviewsLoadingIcon);
        llTrailerLoading = (LinearLayout) rootView.findViewById(R.id.llTrailerLoadingIcon);

        cbStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataPersistence.toggleFavouriteToSharedPreferences(getContext(), finalMovieObject);
            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void onServerResponse(ServerSync.Endpoint endpoint, int responseCode, String responseString) {
        switch (endpoint){
            case TRAILERS:
                try {
                    JSONObject response = new JSONObject(responseString);
                    JSONArray ja = response.getJSONArray("results");
                    JSONObject ob = ja.getJSONObject(0);
                    trailerKey = ob.getString("key");

                            llTrailerLoading.setVisibility(View.GONE);
                            btnTrailer.setVisibility(View.VISIBLE);
                            btnTrailer.setEnabled(true);
                            btnTrailer.setText("Play Trailer");



                } catch (JSONException e) {
                    e.printStackTrace();
                            llTrailerLoading.setVisibility(View.GONE);
                            btnTrailer.setVisibility(View.VISIBLE);
                            btnTrailer.setText("Trailer unavailable...");
                }
                break;
            case REVIEWS:
                try {
                    JSONObject response = new JSONObject(responseString);
                    final JSONArray ja = response.getJSONArray("results");
                            for(int i = 0; i < ja.length(); i++){
                                try {
                                    JSONObject ob = ja.getJSONObject(i);
                                    TextView tv = new TextView(getContext());
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
                                TextView tv = new TextView(getContext());
                                tv.setText("No reviews available.");
                                llReviews.addView(tv);
                            } else {
                                Log.d(TAG, "Reviews found: "+ja.length());
                            }
                            llReviewLoading.setVisibility(View.GONE);

                } catch (JSONException e) {
                    e.printStackTrace();
                            TextView tv = new TextView(getContext());
                            tv.setText("No reviews available.");
                            llReviews.addView(tv);
                            llReviewLoading.setVisibility(View.GONE);
                }
                break;
        }

    }
}
