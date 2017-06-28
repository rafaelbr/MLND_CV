package br.com.geekfox.socialpicture.webservice;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import br.com.geekfox.socialpicture.json.ImageData;

/**
 * Created by rafae on 21/06/2017.
 */

public class PictureDataService {
    private static final String SERVICE_URL = "http://104.197.163.59:5000/";
    private static final String SERVICE_ENDPOINT = "socialpicture/api/v1.0/images";

    public static List<ImageData> retrieveImages() {
        Gson gson = new Gson();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String resultJson = null;

        try {
            URL url = new URL(SERVICE_URL + SERVICE_ENDPOINT);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0)
                    return null;
                resultJson = buffer.toString();

                Type type = new TypeToken<List<ImageData>>(){}.getType();
                List<ImageData> list = gson.fromJson(resultJson, type);

                return list;
            }

            return null;
        }
        catch (IOException e) {
            Log.e("WebService", "Error fetching images", e);
            return null;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (final IOException e){
                    Log.e("WebService", "Error closing stream", e);
                }
            }
        }
    }

    public static Bitmap retrieveImage(int id, String extension) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(SERVICE_URL + "images/" + id + "." + extension);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            int responseCode = urlConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = urlConnection.getInputStream();

                if (inputStream == null) {
                    return null;
                }
                Bitmap bp = BitmapFactory.decodeStream(inputStream);

                return bp;
            }

            return null;
        }
        catch (IOException e) {
            Log.e("WebService", "Error fetching image", e);
            return null;
        }
        finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (final IOException e){
                    Log.e("WebService", "Error closing stream", e);
                }
            }
        }
    }
}
