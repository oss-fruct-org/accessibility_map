package org.fruct.oss.accessibilitymap;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.google.android.gms.common.Scopes;

import org.fruct.oss.accessibilitymap.Model.Accessibility;
import org.fruct.oss.accessibilitymap.Model.AccessibilityType;
import org.fruct.oss.accessibilitymap.Model.Category;
import org.fruct.oss.accessibilitymap.Model.FunctionalArea;
import org.fruct.oss.accessibilitymap.Model.FunctionalAreas;
import org.fruct.oss.accessibilitymap.Model.MaintenanceForm;
import org.fruct.oss.accessibilitymap.Model.Passport;
import org.fruct.oss.accessibilitymap.Model.Scope;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

        b = Uri.parse(Constants.API_SERVICE_URL + "getPassports").buildUpon();

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

        int currentItemId = 0;


        try {

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
            InputStream stream = new ByteArrayInputStream(strJson.getBytes(StandardCharsets.UTF_8)); // FIXME

            List<Passport> passports = readJsonStream(stream);

            DBHelper dbHelper = new DBHelper(context);
            for (Passport passport : passports)
                dbHelper.addPassport(passport);

            return true;

        } catch (IOException e) {
            Log.e("accmap", e.toString());
        }



     /*   try {

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

                        *//*if (passportId == 344)
                            Log.d("accmap", functionalAreaArray.toString() + " ");*//*

                        for (int k = 0; k < functionalAreaArray.length(); k++) {

                            if (isCancelled()) {
                                return null;
                            }

                            JSONObject functionalAreaPoint = functionalAreaArray.getJSONObject(k);
                            int functionalId = functionalAreaPoint.getJSONObject("FunctionalArea").getInt("Id");
                            int accessibilityId = functionalAreaPoint.getJSONObject("Type").getInt("Id");

                            *//*if (passportId == 344) {
                                Log.d("accmap", "i=" + i + " m=" + m + " j=" + j + " k=" + k);
                                Log.d("acc site is ", site);
                            }*//*
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
        }*/

        return false;
    }








    public List<Passport> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readPassportsArray(reader);
        } finally {
                reader.close();
            }
        }

    public List<Passport> readPassportsArray(JsonReader reader) throws IOException {
        List<Passport> passports = new ArrayList<Passport>();

        reader.beginArray();
        while (reader.hasNext()) {
            passports.add(readPassport(reader));
        }
        reader.endArray();
        return passports;
    }

    public Passport readPassport(JsonReader reader) throws IOException {
        String name = null;
        String objectName = null;
        String address = null;
        double latitude = -1;
        double longitude = -1;
        String route = null;
        String adaptiveRoute = null;
        String site = null;
        String contacts = null;
        long id = -1;

        List<Scope> scopes = new ArrayList<Scope>();
        List<Accessibility> accessibilities = new ArrayList<Accessibility>();

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            if (field.equals("Name")) {
                name = reader.nextString();
                Log.d("accmap name", name + "-");
            } else if (field.equals("ObjectName")) {
                objectName = reader.nextString();
            } else if (field.equals("Address")) {
                address = reader.nextString();
            } else if (field.equals("Latitude")) {
                latitude = reader.nextDouble(); // TODO: string to long autoconversion
            } else if (field.equals("Longitude")) {
                longitude = reader.nextDouble();
            } else if (field.equals("Scopes")) {
                scopes = readScopes(reader);
            } else if (field.equals("Route") && reader.peek() != JsonToken.NULL) {
                route = reader.nextString();
            } else if (field.equals("AdaptiveRoute") && reader.peek() != JsonToken.NULL) {
                adaptiveRoute = reader.nextString();
            } else if (field.equals("Accessibility")) {
                accessibilities = readAccessibilities(reader);
            } else if (field.equals("Site") && reader.peek() != JsonToken.NULL) {
                site = reader.nextString();
            } else if (field.equals("Contacts") && reader.peek() != JsonToken.NULL) {
                contacts = reader.nextString();
            } else if (field.equals("Id")) {
                id = reader.nextLong();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Passport(name, objectName, address, latitude, longitude, scopes, route, adaptiveRoute, accessibilities, site, contacts, id);
    }

    public List<Scope> readScopes(JsonReader reader) throws IOException {
        List<Scope> scopes = new ArrayList<Scope>();

        reader.beginArray();
        while (reader.hasNext()) {
            Scope scope = readScope(reader);
            scopes.add(scope);
        }
        reader.endArray();

        return scopes;
    }


    public List<Accessibility> readAccessibilities(JsonReader reader) throws IOException {
        List<Accessibility> accessibilities = new ArrayList<Accessibility>();

        reader.beginArray();
        while (reader.hasNext()) {
            accessibilities.add(readAccessibility(reader));
        }
        reader.endArray();

        return accessibilities;
    }

    public Accessibility readAccessibility(JsonReader reader) throws IOException {

        Category category = null;
        MaintenanceForm maintenanceForm = null;
        List<FunctionalAreas> functionalAreases = null;


        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();

            if (field.equals("Categorie")) {
                category = readCategory(reader);
            } else if (field.equals("MaintenanceForm") && reader.peek() != JsonToken.NULL) {
                maintenanceForm = readMaintenanceForm(reader);
            } else if (field.equals("FunctionalAreas")) {
                functionalAreases = readFunctionalAreas(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Accessibility(category, maintenanceForm, functionalAreases);
    }


    public Scope readScope(JsonReader reader) throws IOException {
        String name = null;
        long id = -1;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            if (field.equals("Name"))
                name = reader.nextString();
            else if (field.equals("Id"))
                id = reader.nextLong();
            else
                reader.skipValue();
        }
        reader.endObject();

        return new Scope(name, id);
    }


    public Category readCategory(JsonReader reader) throws IOException {
        String name = null;
        String sign = null;
        long id = -1;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            if (field.equals("Name"))
                name = reader.nextString();
            else if (field.equals("Id"))
                id = reader.nextLong();
            else if(field.equals("Sign"))
                sign = reader.nextString();
            else
                reader.skipValue();
        }
        reader.endObject();

        return new Category(name, sign, id);
    }


    public MaintenanceForm readMaintenanceForm(JsonReader reader) throws IOException {
        return new MaintenanceForm(readCategory(reader));
    }

    public List<FunctionalAreas> readFunctionalAreas(JsonReader reader) throws IOException {
        List<FunctionalAreas> functionalAreas = new ArrayList<FunctionalAreas>();

        reader.beginArray();
        while (reader.hasNext()) {
            FunctionalAreas functionalArea = readFunctionalAreases(reader);
            functionalAreas.add(functionalArea);
        }
        reader.endArray();

        return functionalAreas;
    }

    public FunctionalAreas readFunctionalAreases(JsonReader reader) throws IOException {
        FunctionalArea functionalArea = null;
        AccessibilityType accessibilityType = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();
            if (field.equals("FunctionalArea")) {
                functionalArea = readFunctionalArea(reader);
            } else if (field.equals("Type")) {
                accessibilityType = readAccessibilityType(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new FunctionalAreas(functionalArea, accessibilityType);
    }

    public FunctionalArea readFunctionalArea(JsonReader reader) throws IOException {
        return new FunctionalArea(readCategory(reader));
    }

     public AccessibilityType readAccessibilityType(JsonReader reader) throws IOException {
        return new AccessibilityType(readCategory(reader));
    }







    // Parse JSON file
    @Override
    public void onPostExecute(Boolean isSuccess) {

    }

}
