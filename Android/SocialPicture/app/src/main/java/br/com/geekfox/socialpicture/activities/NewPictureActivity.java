package br.com.geekfox.socialpicture.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import br.com.geekfox.socialpicture.R;
import br.com.geekfox.socialpicture.webservice.PictureDataService;

public class NewPictureActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1000;
    private static final int CAMERA_REQUEST = 1001;

    private static final int RETURN_OK = 20;
    private static final int RETURN_ERROR = 10;

    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_picture);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (image != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] data = outputStream.toByteArray();
            String image64 = Base64.encodeToString(data, Base64.DEFAULT);
            outState.putString("Image", image64);
        }


        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String encodedImage = savedInstanceState.getString("Image");

        if (encodedImage != null) {
            byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            ImageView imgPreview = (ImageView) findViewById(R.id.imgPreview);
            imgPreview.setImageBitmap(decodedByte);
        }

    }

    public void onClickNewGallery(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(intent, PICK_IMAGE);
    }

    public void onClickNewCamera(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    public void onClickSave(View v) {
        if (image != null) {
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
                        Toast.makeText(NewPictureActivity.this, "Image Saved.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(NewPictureActivity.this, Main2Activity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if (result == RETURN_ERROR) {
                        Toast.makeText(NewPictureActivity.this, "Error while saving image.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    result = PictureDataService.savePicture(image);
                    return null;
                }
            }.execute();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            ImageView imgPreview = (ImageView) findViewById(R.id.imgPreview);
            //imgPreview.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            //imgPreview.setImageURI(selectedImage);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.imgPreview);
                imageView.setImageBitmap(bitmap);

                image = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ImageView imgPreview = (ImageView) findViewById(R.id.imgPreview);
            imgPreview.setImageBitmap(photo);

            image = photo;
        }
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
}
