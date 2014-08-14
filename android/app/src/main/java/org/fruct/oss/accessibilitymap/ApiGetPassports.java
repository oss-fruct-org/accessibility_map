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
public class ApiGetPassports extends AsyncTask<String, Void, Boolean> {

    private Context context;

    private String requestUrl;

    Uri.Builder b;

    public ApiGetPassports(Context context, double latitude, double longitude, int radius) {
        this.context = context;

        b = Uri.parse(Constants.API_SERVICE_URL+ "getPassports").buildUpon();

        b.appendQueryParameter("latitude", String.valueOf(latitude));
        b.appendQueryParameter("longitude", String.valueOf(longitude));
        b.appendQueryParameter("radius", String.valueOf(radius));
        b.appendQueryParameter("callback", "android");
        b.appendQueryParameter("onlyAgreed", "false"); // Some kind of walkaround

        requestUrl = b.build().toString();

        Log.d("accmap", requestUrl);
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
            strJson = strJson.substring(strJson.indexOf("(") + 1, strJson.lastIndexOf(")"));

            Log.d("accmap", strJson + " ");

            JSONArray json = new JSONArray(strJson);

            DBHelper dbHelper = new DBHelper(context);

            for (int i = 0; i < json.length(); i++) {
                JSONObject point = json.getJSONObject(i);

                int passportId = point.getInt("PassportAccessibilityId");
                String name = point.getString("Name");
                String objectName = point.getString("ObjectName");
                String address = point.getString("Address");
                Double latitude = point.getDouble("Latitude");
                Double longitude = point.getDouble("Longitude");
                String route = point.getString("Route");

                String phone = point.getString("Contacts");
                String site = point.getString("Site");

                JSONArray scopesArray = point.getJSONArray("Scopes");
                for (int m = 0; m < scopesArray.length(); m++) {

                    int scope = scopesArray.getJSONObject(m).getInt("Id");

                    JSONArray accessibilityArray = point.getJSONArray("Accessibility");

                    if (passportId == 344)
                        Log.d("accmap", "len:" + accessibilityArray.length() + " " + accessibilityArray.toString());

                    for (int j = 0; j < accessibilityArray.length(); j++) {

                        JSONObject accessibilityPoint = accessibilityArray.getJSONObject(j);
                        int disabilityId = accessibilityPoint.getJSONObject("Categorie").getInt("Id");
                        int maintenanceId = accessibilityPoint.getJSONObject("MaintenanceForm").getInt("Id");

                        JSONArray functionalAreaArray = accessibilityPoint.getJSONArray("FunctionalAreas");

                        if (passportId == 344)
                            Log.d("accmap", functionalAreaArray.toString() + " ");

                        for (int k = 0; k < functionalAreaArray.length(); k++) {
                            JSONObject functionalAreaPoint = functionalAreaArray.getJSONObject(k);
                            int functionalId = functionalAreaPoint.getJSONObject("FunctionalArea").getInt("Id");
                            int accessibilityId = functionalAreaPoint.getJSONObject("Type").getInt("Id");

                            if (passportId == 344) {
                                Log.d("accmap", "i=" + i + " m=" + m + " j=" + j + " k=" + k);
                                Log.d("acc getpass", disabilityId + " " + name + " " + functionalId + " " + accessibilityId + " " + functionalAreaArray.length());
                            }
                            dbHelper.addPoint(
                                    passportId,
                                    name,
                                    objectName,
                                    address,
                                    latitude,
                                    longitude,
                                    scope,
                                    disabilityId,
                                    maintenanceId,
                                    functionalId,
                                    accessibilityId,
                                    route,
                                    phone,
                                    site
                            );
                        }

                    }
                }

                //Log.d("accmap", " "  + json.getJSONObject(i).getString("Name"));
            }

            return true;

        } catch (Exception e) {
            Log.e("accmap getPassports cycle", e.toString() + " ");
            return false;
        }
    }


    // Parse JSON file
    @Override
    public void onPostExecute(Boolean isSuccess) {

    }

}
