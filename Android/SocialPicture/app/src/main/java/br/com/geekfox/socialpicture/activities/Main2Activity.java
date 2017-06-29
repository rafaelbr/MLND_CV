package br.com.geekfox.socialpicture.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.geekfox.socialpicture.R;
import br.com.geekfox.socialpicture.adapters.PicturesListAdapter;
import br.com.geekfox.socialpicture.json.ImageData;
import br.com.geekfox.socialpicture.webservice.PictureDataService;

public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                Intent intent = new Intent(Main2Activity.this, NewPictureActivity.class);
                startActivity(intent);
            }
        });

        loadPictures();
    }

    public void loadPictures() {
        final List<ImageData> list = new ArrayList<>();
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (list == null) {
                    Toast.makeText(Main2Activity.this, "Error while retrieving image list.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected Void doInBackground(Void... voids) {
                List<ImageData> listImages = PictureDataService.retrieveImages();
                if (listImages != null) {
                    list.addAll(listImages);

                }
                return null;
            }
        }.execute();

        PicturesListAdapter adapter = new PicturesListAdapter(this, R.layout.imagelist_layout, list);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
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
        else if (id == R.id.action_refresh) {
            final List<ImageData> list = new ArrayList<>();
            new AsyncTask<Void, Void, Void>(){

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    if (list == null) {
                        Toast.makeText(Main2Activity.this, "Error while retrieving image list.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    List<ImageData> listImages = PictureDataService.retrieveImages();
                    if (listImages != null) {
                        list.addAll(listImages);

                    }
                    return null;
                }
            }.execute();

            PicturesListAdapter adapter = new PicturesListAdapter(this, R.layout.imagelist_layout, list);
            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
