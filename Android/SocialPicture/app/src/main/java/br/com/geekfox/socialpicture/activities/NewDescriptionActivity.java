package br.com.geekfox.socialpicture.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import br.com.geekfox.socialpicture.R;
import br.com.geekfox.socialpicture.json.ImageData;
import br.com.geekfox.socialpicture.webservice.PictureDataService;

public class NewDescriptionActivity extends AppCompatActivity {

    private static final int RETURN_OK = 20;
    private static final int RETURN_ERROR = 10;

    private ImageData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_description);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        data = (ImageData) intent.getSerializableExtra("IMAGE_DATA");

        loadImage();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadImage() {
        final ImageView imageView = (ImageView) findViewById(R.id.imgDesc);

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
                return PictureDataService.retrieveImage(data.getId(), data.getExtension());
            }
        }.execute();
    }

    public void onSave(View view) {
        EditText txtDesc = (EditText) findViewById(R.id.editDesc);
        final String description = txtDesc.getText().toString();

        new AsyncTask<Void, Void, Void>() {
            int result;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (result == RETURN_OK) {
                    Toast.makeText(NewDescriptionActivity.this, "Description Saved.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(NewDescriptionActivity.this, Main2Activity.class);
                    startActivity(intent);
                    finish();
                }
                else if (result == RETURN_ERROR) {
                    Toast.makeText(NewDescriptionActivity.this, "Error while saving description.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                result = PictureDataService.saveDescription(data.getId(), description);
                return null;
            }
        }.execute();
    }
}
