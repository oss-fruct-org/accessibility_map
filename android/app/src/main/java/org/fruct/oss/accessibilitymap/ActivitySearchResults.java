package org.fruct.oss.accessibilitymap;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

public class ActivitySearchResults extends ListActivity {

    ListView listView;

    String searchQuery;
    int selectedDisabilityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchresults);

        Intent intent = getIntent();
        searchQuery = intent.getStringExtra("query");
        selectedDisabilityId = intent.getIntExtra("disabilityId", 0);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            if (searchQuery.length() > 36)
                actionBar.setTitle(searchQuery.substring(0, 35));
            else
                actionBar.setTitle(searchQuery);
        }


        Log.d("accmap", "passid: " + searchQuery + " disability:" + selectedDisabilityId);

        listView = getListView();


        /**
         * Should use uppercase for search purposes. SQLite doesn't support
         * UPPER function for non-english languages
         */
        String searchTextUppercase = searchQuery.toUpperCase();

        String whereQuery = "disability = " + selectedDisabilityId
                + " and (name_uppercase like '%" + searchTextUppercase + "%'"
                + " or objectname_uppercase like '%" + searchTextUppercase + "%'"
                + ")";

        DBHelper dbHelper = new DBHelper(getApplicationContext());
        Cursor cursor = dbHelper.getReadableDatabase().query(true, "points", new String[]{"name", "objectname", "maintenance", "passid"}, whereQuery, null, null, null, null, null);

        Log.d("accmap", "pointInfo cursor: " + cursor.getCount() + " ");

        ArrayList<ListItem> items = new ArrayList<ListItem>();

        if (cursor.moveToFirst()) {

            do {
                ListItem item = new ListItem();

                int indexName = cursor.getColumnIndex("name");
                int indexObjectName = cursor.getColumnIndex("objectname");
                //int indexScope = cursor.getColumnIndex("scope");
                int indexAccess = cursor.getColumnIndex("maintenance");
                int indexId = cursor.getColumnIndex("passid");

                int accessibility = cursor.getInt(indexAccess);

                item.name = cursor.getString(indexName);
                item.objectName = cursor.getString(indexObjectName);
                item.passId = cursor.getInt(indexId);
                //item.scope = dbHelper.getScopeNameById(cursor.getInt(indexScope));
                item.accessibilityDrawableId = getDrawableForMaintenanceId(cursor.getInt(indexAccess));

                items.add(item);

            } while (cursor.moveToNext());


            cursor.close();
            dbHelper.close();
        }

        AccMapSearchListAdapter adapter = new AccMapSearchListAdapter(getApplicationContext(), items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListItem item = (ListItem) parent.getItemAtPosition(position);

                Intent i = new Intent(getApplicationContext(), ActivityPointInfo.class);
                i.putExtra("id", Integer.toString(item.passId)); // Walk around for Google Map
                i.putExtra("disabilityId", selectedDisabilityId);
                //i.putExtra("isMapLinkShowing", false);
                startActivityForResult(i, Constants.ACTIVITY_RESULT_CODE);
            }
        });

    }


    private class ListItem {
        String name;
        String objectName;
        int passId;
        //String scope;
        Drawable accessibilityDrawableId;
    }

    static class ViewHolder {
        public TextView name;
        public TextView objectName;
        //public TextView scope;
        public ImageView accessibility;
    }

    public Drawable getDrawableForMaintenanceId(int maintenanceId) {
        Drawable bitmapDescriptor = getResources().getDrawable(R.drawable.ic_availability_gray);

        if (maintenanceId == 0) // Full access.
            bitmapDescriptor = getResources().getDrawable(R.drawable.ic_availability_green);
        if (maintenanceId == 1) /// Middle access.
            bitmapDescriptor = getResources().getDrawable(R.drawable.ic_availability_yellow);
        if (maintenanceId == 2) // No access.
            bitmapDescriptor = getResources().getDrawable(R.drawable.ic_availability_red);

        return bitmapDescriptor;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Constants.ACTIVITY_RESULT_CODE: {

                // If user clicked 'show on map' button
                if (resultCode == Activity.RESULT_OK) {

                    String newText = data.getStringExtra(Constants.RESULT_INTENT_VALUE);

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Constants.RESULT_INTENT_VALUE, newText);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();

                    //Log.d("accmap", " " + newText);
                    //String[] geo = newText.split(",");

                    //LatLng latLng = new LatLng(Double.parseDouble(geo[0]), Double.parseDouble(geo[1]));
                    //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
                    //mMap.moveCamera(cameraUpdate);
                }
                break;
            }
        }
    }

    private class AccMapSearchListAdapter extends BaseAdapter {

        private ArrayList<ListItem> items;
        private Context context;

        public AccMapSearchListAdapter(Context _context, ArrayList<ListItem> _items) {
            context = _context;
            items = _items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.searchresults_list_item, parent, false);

                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(R.id.searchresults_name);
                holder.objectName = (TextView) convertView.findViewById(R.id.searchresults_objectname);
                //holder.scope = (TextView) convertView.findViewById(R.id.searchresults_scope);
                holder.accessibility = (ImageView) convertView.findViewById(R.id.searchresults_icon);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListItem item = items.get(position);

            holder.name.setText(item.name);
            holder.objectName.setText(item.objectName);
            //holder.scope.setText(item.scope);
            holder.accessibility.setImageDrawable(item.accessibilityDrawableId);

            return convertView;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.activity_pointinfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }


}
