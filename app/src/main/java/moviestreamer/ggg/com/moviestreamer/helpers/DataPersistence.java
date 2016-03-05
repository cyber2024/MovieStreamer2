package moviestreamer.ggg.com.moviestreamer.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Russell Elfenbein on 3/5/2016.
 */
public class DataPersistence {
    public static final String TAG = DataPersistence.class.getSimpleName(),
        prefsTag = "com.ggg.moviestreamer.sharedPrefs",
        savedFavouritesJsonArray = "savedFavouritesJsonArray";
    public static void toggleFavouriteToSharedPreferences(Context context, JSONObject movieJsonObject){
        int existingIndex = -1;
        try {
            JSONArray ar = getSavedArray(context);
            if(ar == null)
                ar = new JSONArray();
            for (int i = 0; i < ar.length(); i++) {
                JSONObject ob = ar.getJSONObject(i);
                if (ob.has("id")) {
                    if (ob.getString("id").equals(movieJsonObject.getString("id"))) {
                        existingIndex = i;
                        Log.d(TAG, ob.getString("title")+" removed from favourites");

                    }
                }
            }

            if (existingIndex > -1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ar.remove(existingIndex);
                } else {
                    JSONArray output = new JSONArray();
                    for (int i = 0; i < ar.length(); i++) {
                        if (i != existingIndex)
                            output.put(ar.get(i));
                    }
                    ar = output;
                }
            } else {
                ar.put(movieJsonObject);
                Log.d(TAG, movieJsonObject.getString("title") + " saved to favourites");
            }
            saveArray(context, ar);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static boolean isItInFavourites(Context context, JSONObject ob){
        JSONArray ar = getSavedArray(context);
        if(ar == null)
            ar = new JSONArray();
        for (int i = 0; i < ar.length(); i++) {
            try {
                if (ar.getJSONObject(i).getString("id").equals(ob.getString("id"))) {

                    Log.d(TAG, "Movie found in favourites");
                    return true;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.d(TAG, "Movie not found in favourites");
        return false;
    }

    public static JSONArray getSavedArray(Context context){
        SharedPreferences prefs = context.getSharedPreferences(prefsTag, Context.MODE_PRIVATE);
        try {
            String string = prefs.getString(savedFavouritesJsonArray, "");
            JSONArray ar = new JSONArray(string);
            Log.d(TAG, "Loaded jsonArray from favourites");
            return ar;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Could not load array, nothing found.");
        return null;
    }

    private static void saveArray(Context context, JSONArray ar){
        SharedPreferences prefs = context.getSharedPreferences(prefsTag, Context.MODE_PRIVATE);
        prefs.edit().putString(savedFavouritesJsonArray, ar.toString()).commit();
        Log.d(TAG, "Saved jsonArray to favourites");
    }
}
