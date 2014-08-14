package org.fruct.oss.accessibilitymap;

import android.app.ActionBar;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;

public class ActivityMain extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private ActionBarDrawerToggle mDrawerToggle;

    ProgressDialog dialog;

    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();

        //mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.abc_action_bar_home_description, R.string.abc_action_mode_done) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle("1");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle("2");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

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

        ((TextView) findViewById(R.id.drawer_about)).setOnClickListener(new View.OnClickListener() {
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

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 1000, locationListener); // 30 sec, 1 km

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 12);
        mMap.moveCamera(cameraUpdate);

        onDisabilityChecked(null);

        Log.d("accmap", "first launch" + prefs.getBoolean("IS_FIRST_LAUNCH", true));
        if (prefs.getBoolean("IS_FIRST_LAUNCH", true)) {
            prefs.edit().putBoolean("IS_FIRST_LAUNCH", false).commit();
            updateDatabaseWithDialog(this);
        }

    }

    public void updateDatabaseWithDialog(Context context) {
        dialog = new ProgressDialog(context);
        dialog.setMessage(getString(R.string.refreshing));
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

    private void makeUseOfNewLocation(Location location) {
        this.location = location;
    }

    public void onDisabilityChecked(View view) {

        String where = "";

        // Check if wheelchair is checked and add condition for SQL query
        if (((RadioButton)findViewById(R.id.drawer_disability_wheelchair)).isChecked())
            where = addOrStatement(where, "disability = 1");

        // Blindness
        if (((RadioButton)findViewById(R.id.drawer_disability_blind)).isChecked())
            where = addOrStatement(where, "disability = 3");

        // Check if
        if (((RadioButton)findViewById(R.id.drawer_disability_muscular)).isChecked())
            where = addOrStatement(where, "disability = 2");

        // Check if
        if (((RadioButton)findViewById(R.id.drawer_disability_deaf)).isChecked())
            where = addOrStatement(where, "disability = 4");

        // Check if
        if (((RadioButton)findViewById(R.id.drawer_disability_mental)).isChecked())
            where = addOrStatement(where, "disability = 5");

        //Log.e("accmap where", where + " ");

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
            where2 = addOrStatement(where2, "maintenance = 1");

        if (((CheckBox)findViewById(R.id.drawer_availability_middle)).isChecked())
            where2 = addOrStatement(where2, "maintenance = 2");

        if (((CheckBox)findViewById(R.id.drawer_availability_none)).isChecked())
            where2 = addOrStatement(where2, "maintenance = 3");

        if (((CheckBox)findViewById(R.id.drawer_availability_unknown)).isChecked())
            where2 = addOrStatement(where2, "maintenance = 4");

        // Check if where2 HERE is empty (nothing to show)
        if (where2.equals("")) {
            Toast.makeText(getApplicationContext(), getString(R.string.choose_at_least_one_accessibility), Toast.LENGTH_SHORT).show();
            return;
        }

        where = where + " and (" + where2 + ")";

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

        new Thread() {
            public void run() {

                DBHelper dbHelper = new DBHelper(getApplicationContext());
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                for (int i = 0; i < 1000; i++) {
                    String WHERE = "passid=" + i + " and " + whereClause;
                    Cursor cursor = db.query(true, "points", new String[]{}, WHERE, null, null, null, null, null);

                    if (cursor.moveToFirst()) {
                        int indexName = cursor.getColumnIndex("objectname");
                        int indexLat = cursor.getColumnIndex("latitude");
                        int indexLon = cursor.getColumnIndex("longitude");
                        int indexAccess = cursor.getColumnIndex("accessibility");
                        int indexScope = cursor.getColumnIndex("scope");

                        final String name = cursor.getString(indexName);
                        String scope = dbHelper.getScopeNameById(cursor.getInt(indexScope)); //FIXME: it is only one; can be more
                        final LatLng position = new LatLng(cursor.getDouble(indexLat), cursor.getDouble(indexLon));

                        int accessibility = cursor.getInt(indexAccess);
                        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_gray);

                        if (accessibility == 1) // Full access.
                            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_green);
                        if (accessibility == 2) /// Middle access.
                            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_yellow);
                        if (accessibility == 3) // No access.
                            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_red);

                        //Log.d("accmap cursor ", i + " WHERE " + cursor.toString());

                        //do {
                        final int finalI = i;
                        final BitmapDescriptor finalBitmapDescriptor = bitmapDescriptor;

                        /**
                         * Adding points to map can be performed only in UI thread
                         */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MarkerOptions marker = new MarkerOptions()
                                        .title(name)
                                        .snippet("" + finalI)
                                        .position(position)
                                        .icon(finalBitmapDescriptor);
                                mMap.addMarker(marker);
                            }
                        });


                        //} while (cursor.moveToNext());
                        if (cursor.getCount() <= 0)
                            return;
                    }

                    cursor.close();

                }

                db.close();
            }
        }.start();


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

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
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    private void prepareDataBase() {
        // Cascade asynchronous loading
        final DBHelper dbHelper = new DBHelper(getApplicationContext());

        if (location == null)
            return;


        // 6
        final ApiGetPassports getPassports = new ApiGetPassports(getApplicationContext(), location.getLatitude(), location.getLongitude(), Constants.POINTS_RADIUS) {

            @Override
            protected void onProgressUpdate(Void... progress) {
            }

            @Override
            public void onCancelled() {
                super.onCancelled();
                dialog.hide();
            }


            @Override
            public void onPostExecute(Boolean isSuccess) {
                Log.d("accmap db", "Downloading db finished");
                dialog.hide();
                onDisabilityChecked(null);
            }
        };

        // 5
        final ApiGetListAccessibilities getListAccessibilities = new ApiGetListAccessibilities(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                getPassports.execute();
            }
        };

        // 4
        final ApiGetListDisaibilities getListDisaibilities = new ApiGetListDisaibilities(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                getListAccessibilities.execute();
            }
        };

        // 3
        final ApiGetListFunctional getListFunctional = new ApiGetListFunctional(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                getListDisaibilities.execute();
            }
        };

        // 2
        final ApiGetListMaintenance getListMaintenance = new ApiGetListMaintenance(getApplicationContext()) {
            @Override
            public void onPostExecute(Boolean isSuccess) {
                getListFunctional.execute();
            }
        };

        // 1
        final ApiGetListScopes getListScopes = new ApiGetListScopes(getApplicationContext()) {

            @Override
            public void onPreExecute() {
                super.onPreExecute();
                dbHelper.deleteAll();
            }

            @Override
            public void onPostExecute(Boolean isSuccess) {
                getListMaintenance.execute();
            }
        };

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
                startActivity(i);
            }
        });
    }

    public int getSelectedDisability() {
        if (((RadioButton)findViewById(R.id.drawer_disability_wheelchair)).isChecked())
            return 1;

        // Blindness
        if (((RadioButton)findViewById(R.id.drawer_disability_blind)).isChecked())
            return 3;

        // Check if
        if (((RadioButton)findViewById(R.id.drawer_disability_muscular)).isChecked())
            return 2;

        // Check if
        if (((RadioButton)findViewById(R.id.drawer_disability_deaf)).isChecked())
            return 4;

        return 5;
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

}
