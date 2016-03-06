package moviestreamer.ggg.com.moviestreamer;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by relfenbein on 4/10/2015.
 */
public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private String[] posterPathArray = new String[1];

    public ImageAdapter(Context c){
        mContext = c;
    }

    @Override
    public int getCount() {
        return posterPathArray.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null){
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams((int)parent.getResources().getDimension(R.dimen.imageView_width),
                    (int)parent.getResources().getDimension(R.dimen.imageView_height)));
            imageView.setPadding(8,8,8,8);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(R.drawable.ic_hourglass128);
        Picasso.with(mContext)
                .load(posterPathArray[position])
                .placeholder(R.drawable.ic_hourglass128)
                .error(R.drawable.ic_404)
                .into(imageView);
       // Log.w("Picasso called", "Let's see if it was called");
        return imageView;
    }

    public void updateURLs(JSONArray movieArray){
        if(movieArray != null && movieArray.length() != 0) {
            String[] tempPosterPaths = new String[movieArray.length()];

            for (int i = 0; i < movieArray.length(); i++) {
                try {
                    JSONObject tempObj = movieArray.getJSONObject(i);
                    tempPosterPaths[i] = "http://image.tmdb.org/t/p/w185" +
                            tempObj.getString("poster_path");
                } catch (JSONException e) {
                    Log.e("JSON EXCEPTION ERROR", e.getMessage());
                    e.printStackTrace();
                }
            }
            this.posterPathArray = tempPosterPaths;
        } else {
            this.posterPathArray = new String[]{};
        }

        this.notifyDataSetChanged();
    }
}
