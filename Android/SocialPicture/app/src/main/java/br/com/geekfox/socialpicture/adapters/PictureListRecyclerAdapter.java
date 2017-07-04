package br.com.geekfox.socialpicture.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.geekfox.socialpicture.R;
import br.com.geekfox.socialpicture.activities.NewDescriptionActivity;
import br.com.geekfox.socialpicture.json.ImageData;
import br.com.geekfox.socialpicture.webservice.PictureDataService;

/**
 * Created by rafae on 30/06/2017.
 */

public class PictureListRecyclerAdapter extends RecyclerView.Adapter<PictureListRecyclerAdapter.ViewHolder> {

    private static List<ImageData> list;


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imageView;
        private TextView descView;
        private ImageData item;

        public ViewHolder(View v) {
            super(v);

            imageView = (ImageView) v.findViewById(R.id.imgPicture);
            descView = (TextView) v.findViewById(R.id.txtDescription);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), NewDescriptionActivity.class);
            intent.putExtra("IMAGE_DATA", item);
            view.getContext().startActivity(intent);
        }

        public void bindImage(ImageData image) {
            item = image;
            new AsyncTask<Void, Void, Bitmap>(){
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    imageView.setImageBitmap(result);
                }

                @Override
                protected Bitmap doInBackground(Void... voids) {
                    return PictureDataService.retrieveImage(item.getId(), item.getExtension());
                }
            }.execute();

            if (item.getDescription().isEmpty()) {
                descView.setText("No description yet.");
            }
            else {
                descView.setText(item.getDescription());
            }

        }
    }

    public PictureListRecyclerAdapter(List<ImageData> list) {
        this.list = list;
    }


    @Override
    public PictureListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagelist_layout, parent, false);
        return new ViewHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(PictureListRecyclerAdapter.ViewHolder holder, int position) {
        final ImageData image = list.get(position);
        holder.bindImage(image);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
