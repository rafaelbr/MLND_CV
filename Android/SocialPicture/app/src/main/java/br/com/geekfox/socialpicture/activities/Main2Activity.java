package br.com.geekfox.socialpicture.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.geekfox.socialpicture.R;
import br.com.geekfox.socialpicture.adapters.PictureListRecyclerAdapter;
import br.com.geekfox.socialpicture.adapters.PicturesListAdapter;
import br.com.geekfox.socialpicture.json.ImageData;
import br.com.geekfox.socialpicture.webservice.PictureDataService;

public class Main2Activity extends AppCompatActivity {

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (RecyclerView) findViewById(R.id.listView);
        linearLayoutManager = new LinearLayoutManager(Main2Activity.this);
        listView.setLayoutManager(linearLayoutManager);

        PictureListRecyclerAdapter adapter = new PictureListRecyclerAdapter(new ArrayList<ImageData>());
        listView = (RecyclerView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

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
                else {

                    PictureListRecyclerAdapter adapter = new PictureListRecyclerAdapter(list);
                    listView = (RecyclerView) findViewById(R.id.listView);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        listView.setNestedScrollingEnabled(true);
                    }
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
            loadPictures();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
