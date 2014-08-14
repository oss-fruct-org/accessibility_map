package org.fruct.oss.accessibilitymap;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by alexander on 06.05.14.
 *
 */
public class ApiGetListFunctional extends AsyncTask<String, Void, Boolean> {

    private Context context;

    private String requestUrl;

    Uri.Builder b;

    public ApiGetListFunctional(Context context) {
        this.context = context;

        b = Uri.parse(Constants.API_SERVICE_URL+ "getListFunctionalAreas").buildUpon();

        requestUrl = b.build().toString();

        Log.d("accmap getListFunctionalAreas", requestUrl);
    }

    @Override
    protected Boolean doInBackground(String... urls) {

        if (isCancelled()) {
            return null;
        }

        try {
            URL url = new URL(requestUrl);
            InputStream input = new BufferedInputStream(url.openStream());

            StringBuilder sb = new StringBuilder();
            String line;
            String strJson;

            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            while ((line = reader.readLine()) != null)
                sb.append(line);

            strJson = sb.toString();
            //strJson = strJson.substring(strJson.indexOf("(") + 1, strJson.lastIndexOf(")"));

            //Log.d("accmap", strJson + " ");

            JSONArray json = new JSONArray(strJson);

            DBHelper dbHelper = new DBHelper(context);

            for (int i = 0; i < json.length(); i++) {
                JSONObject item = json.getJSONObject(i);
                dbHelper.addFunctional(
                        item.getInt("Id"),
                        item.getString("Name")
                );

                //Log.d("accmap", " "  + json.getJSONObject(i).getString("Name"));
            }
            //json.getJSONObject("success"); // If "success" found (No exception), then return true

            return true;

        } catch (Exception e) {
            Log.e("accmap", e.toString() + " ");
            return false;
        }
    }


    // Parse JSON file
    @Override
    public void onPostExecute(Boolean isSuccess) {

    }

}
