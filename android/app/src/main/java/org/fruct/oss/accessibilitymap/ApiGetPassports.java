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

    protected Context context;

    private String requestUrl;

    protected String currentOperation;

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

    protected String getCurrentOperation() {
        return currentOperation;
    }


    @Override
    protected Boolean doInBackground(String... urls) {

        if (isCancelled()) {
            return null;
        }

        int currentItemId = 0;

        try {

            Log.d("accmap getpass", "Preparing");

            URL url = new URL(requestUrl);
            InputStream input = new BufferedInputStream(url.openStream());

            StringBuilder sb = new StringBuilder();
            String line;
            String strJson;

            BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

            Log.d("accmap getpass", "Downloading");

            while ((line = reader.readLine()) != null)
                sb.append(line);

            Log.d("accmap getpass", "Downloading finished");

            strJson = sb.toString();
            strJson = strJson.substring(strJson.indexOf("(") + 1, strJson.lastIndexOf(")"));

            Log.d("accmap getpass", "Loading to memory");

            JSONArray json = new JSONArray(strJson);

            Log.d("accmap getpass", "Loading to memory finished");

            DBHelper dbHelper = new DBHelper(context);

            Log.d("accmap getpass", "parsing");

            for (int i = 0; i < json.length(); i++) {

                JSONObject point = json.getJSONObject(i);

                int passportId = currentItemId = point.getInt("Id");
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

                    //if (passportId == 344)
                    //    Log.d("accmap", "len:" + accessibilityArray.length() + " " + accessibilityArray.toString());

                    for (int j = 0; j < accessibilityArray.length(); j++) {

                        JSONObject accessibilityPoint = accessibilityArray.getJSONObject(j);
                        int disabilityId = accessibilityPoint.getJSONObject("Categorie").getInt("Id");

                        int maintenanceId;
                        if (!accessibilityPoint.isNull("MaintenanceForm"))
                            maintenanceId = accessibilityPoint.getJSONObject("MaintenanceForm").getInt("Id");
                        else
                            break; // Maintenance can be null, so go to next iteration

                        JSONArray functionalAreaArray = accessibilityPoint.getJSONArray("FunctionalAreas");

                        /*if (passportId == 344)
                            Log.d("accmap", functionalAreaArray.toString() + " ");*/

                        for (int k = 0; k < functionalAreaArray.length(); k++) {

                            if (isCancelled()) {
                                return null;
                            }

                            JSONObject functionalAreaPoint = functionalAreaArray.getJSONObject(k);
                            int functionalId = functionalAreaPoint.getJSONObject("FunctionalArea").getInt("Id");
                            int accessibilityId = functionalAreaPoint.getJSONObject("Type").getInt("Id");

                            /*if (passportId == 344) {
                                Log.d("accmap", "i=" + i + " m=" + m + " j=" + j + " k=" + k);
                                Log.d("acc site is ", site);
                            }*/
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

            }
            Log.d("accmap getpass", "Parsing finished");

            return true;

        } catch (Exception e) {
            Log.e("accmap getPassports cycle", e.toString() + " " + currentItemId);
            return false;
        }
    }


    // Parse JSON file
    @Override
    public void onPostExecute(Boolean isSuccess) {

    }

}
