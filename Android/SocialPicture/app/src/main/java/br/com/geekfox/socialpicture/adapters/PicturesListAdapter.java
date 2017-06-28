package br.com.geekfox.socialpicture.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.geekfox.socialpicture.R;
import br.com.geekfox.socialpicture.json.ImageData;
import br.com.geekfox.socialpicture.webservice.PictureDataService;

/**
 * Created by Rafael Brasileiro on 21/06/2017.
 */

public class PicturesListAdapter extends ArrayAdapter<ImageData> {

    int resourceId;
    List<ImageData> list;

    public PicturesListAdapter(Context context, int resource, List<ImageData> objects) {
        super(context, resource, objects);
        list = objects;
        resourceId = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(resourceId, null);
        final ImageData image = list.get(position);

        final ImageView imgView = (ImageView) v.findViewById(R.id.imgPicture);
        TextView txtView = (TextView) v.findViewById(R.id.txtDescription);

        if (!image.getDescription().isEmpty())
            txtView.setText(image.getDescription());

        new AsyncTask<Void, Void, Bitmap>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                imgView.setImageBitmap(result);
            }

            @Override
            protected Bitmap doInBackground(Void... voids) {
                return PictureDataService.retrieveImage(image.getId(), image.getExtension());
            }
        }.execute();

        return v;

    }
}
