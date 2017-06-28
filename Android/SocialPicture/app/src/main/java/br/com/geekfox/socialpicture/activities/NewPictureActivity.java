package br.com.geekfox.socialpicture.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import br.com.geekfox.socialpicture.R;

public class NewPictureActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1000;
    private static final int CAMERA_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_picture);


    }

    public void onClickNewGallery(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");

        startActivityForResult(intent, PICK_IMAGE);
    }

    public void onClickNewCamera(View v) {

    }

    public void onClickSave(View v) {

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
            imgPreview.setImageURI(selectedImage);
        }
    }
}
