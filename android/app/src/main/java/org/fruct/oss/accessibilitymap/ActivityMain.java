package org.fruct.oss.accessibilitymap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;

public class ActivityMain extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private ActionBarDrawerToggle mDrawerToggle;
    DrawerLayout mDrawerLayout;

    ProgressDialog dialog;

    ProgressBar mapProgressBar;

    Location location;

    EditText searchField;
    ImageButton searchButton;

    Thread markerAddingThread = null;

    SharedPreferences sharedPreferences;

    Spinner spinner = null;

    ApiGetPassports getPassports;
    ApiGetListAccessibilities getListAccessibilities;
    ApiGetListDisaibilities getListDisaibilities;
    ApiGetListFunctional getListFunctional;
    ApiGetListMaintenance getListMaintenance;
    ApiGetListScopes getListScopes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES, 0);
        int savedDisabilityId = sharedPreferences.getInt(Constants.SP_CHOOSED_DISABILITY, 1);
        setChoosedDisability(savedDisabilityId);

        //mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mapProgressBar = (ProgressBar) findViewById(R.id.map_progerssbar);

        searchField = (EditText) findViewById(R.id.drawer_search_edittext);
        searchButton = (ImageButton) findViewById(R.id.drawer_search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (!isSearchFieldEmpty())
                    isSearchnigNow = false;
                else*/
                if (!isSearchFieldEmpty())
                    search();
            }
        });
        findViewById(R.id.drawer_search_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
            }
        });


        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                toolbar,
                R.string.abc_action_bar_home_description,
                R.string.abc_action_mode_done) {

            // Called when a drawer has settled in a completely closed state
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                closeKeyboard();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            //** Called when a drawer has settled in a completely open state.
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                closeKeyboard();
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        prepareSpinner();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(Gravity.START);

        TextView refreshData = (TextView) findViewById(R.id.refresh_data);
        //final Context context = this;
        refreshData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatabaseWithDialog(ActivityMain.this);
            }
        });

        findViewById(R.id.drawer_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });

        SharedPreferences prefs = getSharedPreferences("ACC_MAP", 0);


        // Initialize about dialog
        final Context context = this;
        findViewById(R.id.drawer_help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.dialog_help, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptsView);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.about_dialog_button),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                }
                        );

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });



        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.cannot_obtain_location_data), Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mMap.moveCamera(cameraUpdate);

        onDisabilityChecked(null);

        Log.d("accmap", "first launch" + prefs.getBoolean("IS_FIRST_LAUNCH", true));
        if (prefs.getBoolean("IS_FIRST_LAUNCH", true)) {
            prefs.edit().putBoolean("IS_FIRST_LAUNCH", false).commit();
            updateDatabaseWithDialog(this);
        } else {
            mDrawerLayout.closeDrawer(Gravity.START);
        }


    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    public void updateDatabaseWithDialog(Context context) {
        dialog = new ProgressDialog(ActivityMain.this);
        dialog.setMessage(getString(R.string.refreshing));
        dialog.setCancelable(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                try {
                    getListScopes.cancel(true);
                    getListMaintenance.cancel(true);
                    getListFunctional.cancel(true);
                    getListDisaibilities.cancel(true);
                    getListAccessibilities.cancel(true);

                } catch (Exception e) {
                    Log.e("accmap", e.toString());
                }

                try {
                    getPassports.cancel(true);
                } catch (Exception e) {
                    Log.e("accmap", e.toString());
                }
            }
        });
        dialog.show();
        prepareDataBase();
    }

    public void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about_dialog_title))
                .setMessage(getString(R.string.about_dialog_body))
                .setIcon(R.drawable.ic_launcher)
                .setCancelable(false)
                .setNegativeButton(getString(R.string.about_dialog_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onDisabilityChecked(View view) {

        String where = "";

        where = addOrStatement(where, "disability = " + getSelectedDisability());

        // Check if where HERE is empty (nothing to show)
        if (where.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.choose_at_least_one_category), Toast.LENGTH_SHORT).show();
            return;
        }

        /**
         *
         * Check for 'availability' group of checkboxes
         *
         */
        String where2 = "";
        if (((CheckBox)findViewById(R.id.drawer_availability_full)).isChecked())
            where2 = addOrStatement(where2, "maintenance = 0");

        if (((CheckBox)findViewById(R.id.drawer_availability_middle)).isChecked())
            where2 = addOrStatement(where2, "maintenance = 1");

        if (((CheckBox)findViewById(R.id.drawer_availability_none)).isChecked())
            where2 = addOrStatement(where2, "maintenance = 2");

        if (((CheckBox)findViewById(R.id.drawer_availability_unknown)).isChecked())
            where2 = addOrStatement(where2, "maintenance = 3");

        // Check if where2 HERE is empty (nothing to show)
        if (where2.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.choose_at_least_one_accessibility), Toast.LENGTH_SHORT).show();
            return;
        }

        where = where + " and (" + where2 + ")";


        /**
         * Get spinner selections
         *
         */
        int selectedScope = -1;
        if (spinner != null) {
            try {
                DBHelper dbHelper = new DBHelper(getApplicationContext());
                selectedScope = dbHelper.getScopeIdByName(spinner.getSelectedItem().toString()); // Find id for selected item
            } catch (Exception e) {
                Log.e("accmap", e.toString());
            }
        }


        if (selectedScope != -1) {
            where = where + " and scope = " + selectedScope;
        }


        Log.d("accmap where2", where + " ");

        filterPointsOnMap(where);

    }

    private String addOrStatement(String where, String add) {
        if (where.equals(""))
            return add;

        return where + " or " + add;
    }


    private void filterPointsOnMap(final String whereClause) {

        mMap.clear();

        markerAddingThread = new Thread() {
            public void run() {

                DBHelper dbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                for (int i = 0; i < 1000; i++) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapProgressBar.setVisibility(View.VISIBLE);
                            mapProgressBar.setProgress(1);
                        }
                    });



                    String WHERE = "passid=" + i + " and " + whereClause;
                    Cursor cursor = db.query(true, "points", new String[]{}, WHERE, null, null, null, null, null);

                    if (cursor.moveToFirst()) {
                        int indexName = cursor.getColumnIndex("objectname");
                        int indexLat = cursor.getColumnIndex("latitude");
                        int indexLon = cursor.getColumnIndex("longitude");
                        int indexAccess = cursor.getColumnIndex("maintenance");

                        final String name = cursor.getString(indexName);
                        final LatLng position = new LatLng(cursor.getDouble(indexLat), cursor.getDouble(indexLon));

                        int accessibility = cursor.getInt(indexAccess);
                        BitmapDescriptor bitmapDescriptor = getMarkerBitmapForMaintenanceId(accessibility);

                        final int finalI = i;
                        final BitmapDescriptor finalBitmapDescriptor = bitmapDescriptor;

                        /**
                         * Adding points to map can be performed only in UI thread
                         */
                        Runnable run = new Runnable() {
                            @Override
                            public void run() {

                                MarkerOptions marker = new MarkerOptions()
                                        .title(name)
                                        .snippet("" + finalI)
                                        .position(position)
                                        .icon(finalBitmapDescriptor);
                                mMap.addMarker(marker);
                            }
                        };

                        runOnUiThread(run);

                        if (cursor.getCount() <= 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mapProgressBar.setVisibility(View.GONE);
                                }
                            });

                            return;
                        }
                    }

                    cursor.close();

                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapProgressBar.setVisibility(View.GONE);
                    }
                });


                db.close();
            }
        };

        mMap.clear();
        markerAddingThread.start();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void prepareSpinner() {
        spinner = (Spinner) findViewById(R.id.drawer_spinner_scope);

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(true, "scopes", new String[]{}, null, null, null, null, null, null);


        ArrayList<String> spinnerList = new ArrayList<String>();
        spinnerList.add(getString(R.string.all_scopes));

        if (cursor.moveToFirst()) {
            do {
                int indexName = cursor.getColumnIndex("name");
                String name = cursor.getString(indexName);

                spinnerList.add(name);

            } while (cursor.moveToNext());

            ArrayAdapter spinnerAdapter =  new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerList);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            spinner.setSelection(0);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    onDisabilityChecked(null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        cursor.close();
        db.close();
        dbHelper.close();

    }


    private void prepareDataBase() {
        // Cascade asynchronous loading
        final DBHelper dbHelper = new DBHelper(getApplicationContext());

        if (location == null)
            return;

        // 6
        getPassports = new ApiGetPassports(getApplicationContext(), location.getLatitude(), location.getLongitude(), Constants.POINTS_RADIUS) {

            @Override
            protected void onPreExecute() {
                dialog.show();
            }

            @Override
            protected void onProgressUpdate(Void... progress) {
                //dialog.setMessage(getCurrentOperation());
            }

            @Override
            public void onCancelled() {
                super.onCancelled();
                Log.d("accmap", "getPassports task cancelled");
                Toast.makeText(context, getString(R.string.downloading_cancelled), Toast.LENGTH_SHORT).show();
                dialog.hide();
            }


            @Override
            public void onPostExecute(Boolean isSuccess) {
                Log.d("accmap db", "GetPassports finished");
                dialog.hide();
                if (isSuccess) {
                    Toast.makeText(context, getString(R.string.downloading_complete), Toast.LENGTH_SHORT).show();
                    prepareSpinner();
                    onDisabilityChecked(null);
                    Log.d("accmap time end", System.currentTimeMillis() + "ms");
                } else {
                    Toast.makeText(context, "Ошибка во время получения данных", Toast.LENGTH_LONG).show();
                }
            }
        };

        // 5
        getListAccessibilities = new ApiGetListAccessibilities(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                Log.d("accmap db", "GetListAccessibilities finished. Running getPassports");
                getPassports.execute();
            }
        };

        // 4
        getListDisaibilities = new ApiGetListDisaibilities(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                Log.d("accmap db", "GetListDisabilities finished. Running getlistAccessibilities");
                getListAccessibilities.execute();
            }
        };

        // 3
        getListFunctional = new ApiGetListFunctional(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                Log.d("accmap db", "GetListFuntionals finished. Running getListDisabilities");
                getListDisaibilities.execute();
            }
        };

        // 2
        getListMaintenance = new ApiGetListMaintenance(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                Log.d("accmap db", "GetListMaintenance finished. Running getListFunctional");
                getListFunctional.execute();
            }
        };

        // 1
        getListScopes = new ApiGetListScopes(getApplicationContext()) {

            @Override
            public void onPreExecute() {
                super.onPreExecute();
                dbHelper.deleteAll();
                Log.d("accmap time start", System.currentTimeMillis() + "ms");
            }

            @Override
            public void onPostExecute(Boolean isSuccess) {
                Log.d("accmap db", "GetListScopes finished. Running getListMaintenance");
                getListMaintenance.execute();
            }
        };

        Log.d("accmap db", "Starting GetListScopes");
        getListScopes.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent i = new Intent(getApplicationContext(), ActivityPointInfo.class);
                i.putExtra("id", marker.getSnippet());
                i.putExtra("disabilityId", getSelectedDisability());
                i.putExtra("isMapLinkShowing", true);
                startActivityForResult(i, Constants.ACTIVITY_RESULT_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Constants.ACTIVITY_RESULT_CODE: {

                // If user clicked 'show on map' button
                if (resultCode == Activity.RESULT_OK) {

                    mDrawerLayout.closeDrawer(Gravity.START);

                    String newText = data.getStringExtra(Constants.RESULT_INTENT_VALUE);
                    //newText = newText.replace("(", "").replace(")", "");
                    Log.d("accmap", " " + newText);
                    String[] geo = newText.split(",");

                    LatLng latLng = new LatLng(Double.parseDouble(geo[0]), Double.parseDouble(geo[1]));
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
                    mMap.moveCamera(cameraUpdate);
                }
                break;
            }
        }
    }

    public ArrayList<Integer> getSelectedMaintenances() {

        ArrayList<Integer> items = new ArrayList<Integer>();

        if (((CheckBox)findViewById(R.id.drawer_availability_full)).isChecked())
            items.add(0);

        if (((CheckBox)findViewById(R.id.drawer_availability_middle)).isChecked())
            items.add(1);

        if (((CheckBox)findViewById(R.id.drawer_availability_none)).isChecked())
            items.add(2);

        if (((CheckBox)findViewById(R.id.drawer_availability_unknown)).isChecked())
            items.add(3);

        return items;
    }

    public void setChoosedDisability(int disabilityId) {
        switch (disabilityId) {
            case 5:
                ((RadioButton)findViewById(R.id.drawer_disability_mental)).setChecked(true);
                break;

            case 2:
                ((RadioButton)findViewById(R.id.drawer_disability_blind)).setChecked(true);
                break;

            case 3:
                ((RadioButton)findViewById(R.id.drawer_disability_muscular)).setChecked(true);
                break;

            case 4:
                ((RadioButton)findViewById(R.id.drawer_disability_deaf)).setChecked(true);
                break;

            default:
                ((RadioButton)findViewById(R.id.drawer_disability_wheelchair)).setChecked(true);
                break;
        }
    }

    public int getSelectedDisability() {
        int selectedDisability = 5;

        if (((RadioButton)findViewById(R.id.drawer_disability_wheelchair)).isChecked())
            selectedDisability = 1;

        // Blindness
        if (((RadioButton)findViewById(R.id.drawer_disability_blind)).isChecked())
            selectedDisability = 3;

        // Check if
        if (((RadioButton)findViewById(R.id.drawer_disability_muscular)).isChecked())
            selectedDisability = 2;

        // Check if
        if (((RadioButton)findViewById(R.id.drawer_disability_deaf)).isChecked())
            selectedDisability = 4;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(Constants.SP_CHOOSED_DISABILITY, selectedDisability);
        editor.commit();

        return selectedDisability;
    }

    public class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        int passportId;

        public MarkerInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.map_marker, null);

        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView title = (TextView) myContentsView.findViewById(R.id.marker_title);
            TextView subtitle = (TextView) myContentsView.findViewById(R.id.marker_subtitle);

            title.setText(marker.getTitle());
            subtitle.setText(marker.getSnippet());

            passportId = Integer.parseInt(marker.getSnippet());

            return myContentsView;
        }


    }

    public static BitmapDescriptor getMarkerBitmapForMaintenanceId(int maintenanceId) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_gray);

        if (maintenanceId == 0) // Full access.
            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_green);
        if (maintenanceId == 1) /// Middle access.
            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_yellow);
        if (maintenanceId == 2) // No access.
            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_red);

        return bitmapDescriptor;
    }




    /**
     *
     * Search things
     */

    public boolean isSearchFieldEmpty() {
        String searchText = getSearchFieldString();

        if (searchText.equals("") || searchText.length() <= 2 || searchText.isEmpty())
            return true;

        return false;
    }

    public String getSearchFieldString() {
        return searchField.getText().toString().trim();
    }

    public void search() {
        Intent i = new Intent(this, ActivitySearchResults.class);
        i.putExtra("query", getSearchFieldString());
        i.putExtra("disabilityId", getSelectedDisability());
        startActivityForResult(i, Constants.ACTIVITY_RESULT_CODE);

    }


}


