package moviestreamer.ggg.com.moviestreamer.helpers;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import moviestreamer.ggg.com.moviestreamer.R;

/**
 * Created by relfenbein on 5/03/2016.
 */
public class ServerSync {
    public static final String TAG = ServerSync.class.getSimpleName();
    public enum Method {POST, GET};
    public enum Endpoint {REVIEWS, TRAILERS, DISCOVER};


    public static String httpRequest(Context context, Method method, Endpoint endpoint, String... params){
        ServerCallback mListener;
        try {
            mListener = (ServerCallback) context;
        } catch(ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ServerCallback interface");
        }

        int responseCode;

    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String responseString = null;

    try{
        String urlString = "http://api.themoviedb.org/3";
        switch(endpoint){
            case TRAILERS:
                urlString += "/movie/"+params[0]+"/videos?";
                break;
            case REVIEWS:
                urlString += "/movie/"+params[0]+"/reviews?";
                break;
            case DISCOVER:
                urlString += "/discover/movie?sort_by=" +params[0]
                        +".desc&";
        }
        urlString += "api_key="+context.getString(R.string.themoviedb_apikey);
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        switch (method){
            case GET:
                urlConnection.setRequestMethod("GET");
                break;
            case POST:
                urlConnection.setRequestMethod("POST");
                break;
        }
        urlConnection.connect();
        responseCode = urlConnection.getResponseCode();
        InputStream inputStream;
        try {
            inputStream = urlConnection.getInputStream();
        } catch (IOException e){
            e.printStackTrace();
            inputStream = urlConnection.getErrorStream();
        }
        StringBuffer buffer = new StringBuffer();
        if(inputStream == null){
            //do nothing
            return null;
        }

        reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while((line = reader.readLine()) != null){
            buffer.append(line + '\n');
        }
        if(buffer.length() == 0){
            //stream empty, do nothing
            return null;
        }
        responseString = buffer.toString();
    } catch (IOException e){
        Log.e(TAG, "Error", e);
        responseCode = -1;
        return null;
    } finally {
        if (urlConnection != null) {
            urlConnection.disconnect();
        }
        if (reader != null) {
            try {
                reader.close();
            } catch (final IOException e) {
                Log.e("MainActivity", "Error closing stream", e);
            }
        }
    }
        Log.d(TAG, responseString);
        mListener.onServerResponse(endpoint,responseCode, responseString);
        return responseString;
}

    public interface ServerCallback{
        public void onServerResponse(Endpoint endpoint, int responseCode, String response);
    }
}
