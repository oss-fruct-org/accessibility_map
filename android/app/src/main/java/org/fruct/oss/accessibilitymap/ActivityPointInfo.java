package org.fruct.oss.accessibilitymap;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.*;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ActivityPointInfo extends FragmentActivity {

    Location location;

    int passportId;
    int selectedDisabilityId;

    ListView listView;

    LatLng objectLocation = null;

    boolean isMapLinkShowing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointinfo);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        Intent intent = getIntent();
        passportId = Integer.parseInt(intent.getStringExtra("id"));
        selectedDisabilityId = intent.getIntExtra("disabilityId", 0);
        isMapLinkShowing = true;//!intent.getBooleanExtra("isMapLinkShowing", true);

        Log.d("accmap", "passid: " + passportId + " disability:" + selectedDisabilityId);

        listView = (ListView) findViewById(R.id.pointinfo_listview);

        ArrayList<ListItem> items = new ArrayList<ListItem>();

        DBHelper dbHelper = new DBHelper(getApplicationContext());

        String name = null;
        String objectName = null;
        String address = null;
        String getBy = null;
        String phone = null;
        String site = null;


        Cursor cursor = dbHelper.getReadableDatabase().query(true, "points", new String[]{}, "passid=" + passportId + " and " + "disability=" + selectedDisabilityId, null, null, null, null, null);

        Log.d("accmap", "pointInfo cursor: " + cursor.getCount() + " ");

        if (cursor.moveToFirst()) {
            int indexDisability = cursor.getColumnIndex("disability");
            int indexTotalAccessibility = cursor.getColumnIndex("maintenance");
            int indexAreaTitle = cursor.getColumnIndex("functional");
            int indexAreaAccessibility = cursor.getColumnIndex("accessibility");

            int indexName = cursor.getColumnIndex("name");
            int indexObjectName = cursor.getColumnIndex("objectname");
            int indexAddress = cursor.getColumnIndex("address");
            int indexGetBy = cursor.getColumnIndex("route");
            int indexPhone = cursor.getColumnIndex("phone");
            int indexSite = cursor.getColumnIndex("site");

            int indexLatitude = cursor.getColumnIndex("latitude");
            int indexLongitude = cursor.getColumnIndex("longitude");
            objectLocation = new LatLng(cursor.getDouble(indexLatitude), cursor.getDouble(indexLongitude));

            name = cursor.getString(indexName);
            objectName = cursor.getString(indexObjectName);
            address = cursor.getString(indexAddress);
            getBy = cursor.getString(indexGetBy);

            phone = cursor.getString(indexPhone);
            site = cursor.getString(indexSite);

            ListItem item = new ListItem();
            item.disabilityTitle = dbHelper.getDisabilityNameById(cursor.getInt(indexDisability));
            item.availabilityTitle = dbHelper.getMaintentanceNamByid(cursor.getInt(indexTotalAccessibility));

            String description = "";


            do {
                description = description + "\n\n" + dbHelper.getFunctionalNamByid(cursor.getInt(indexAreaTitle))
                + " — " + dbHelper.getAccessibilityNameById(cursor.getInt(indexAreaAccessibility));

                Log.d("accmap", dbHelper.getFunctionalNamByid(cursor.getInt(indexAreaTitle)) + " " + dbHelper.getAccessibilityNameById(cursor.getInt(indexAreaAccessibility)));

            } while (cursor.moveToNext());

            item.availabilityInfo = description.trim();

            items.add(item);
        }
        //}


        AccMapListAdapter adapter = new AccMapListAdapter(getApplicationContext(), items);
        listView.setAdapter(adapter);
        listView.addHeaderView(createHeader(name, objectName, address, getBy, phone, site));

        cursor.close();
        dbHelper.close();

    }

    private View createHeader(String name, String objectName, String address, String getby, final String phone, final String site) {
        View v = getLayoutInflater().inflate(R.layout.pointinfo_header, null);
        ((TextView) v.findViewById(R.id.pointinfo_header_name)).setText(name);
        ((TextView) v.findViewById(R.id.pointinfo_header_objectname)).setText(objectName);
        ((TextView) v.findViewById(R.id.pointinfo_header_address)).setText(address);

        if (getby != null && !getby.equals("null")) {
            ((TextView) v.findViewById(R.id.pointinfo_header_getby)).setText(getString(R.string.how_to_get) + getby);
            v.findViewById(R.id.pointinfo_header_getby).setVisibility(View.VISIBLE);
        }

        if (phone != null && !phone.equals("null")) {
            Log.d("accmap", "phone is " + phone);
            ((TextView) v.findViewById(R.id.pointinfo_header_phone)).setText(getString(R.string.telephone) + phone);
            v.findViewById(R.id.pointinfo_header_phone).setVisibility(View.VISIBLE);
            v.findViewById(R.id.pointinfo_header_phone).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_DIAL);
                    i.setData(Uri.parse("tel:" + phone.replace("-", "")));
                    startActivity(i);
                }
            });
        }

        if (site != null && !site.equals("null")) {

            SpannableString content = new SpannableString(site);
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);

            ((TextView) v.findViewById(R.id.pointinfo_header_site)).setText(content);

            v.findViewById(R.id.pointinfo_header_web).setVisibility(View.VISIBLE);
            v.findViewById(R.id.pointinfo_header_site).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String url1 = site;
                    if (!site.startsWith("http://") && !site.startsWith("https://")) {
                        url1 = "http://" + site;
                    }

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url1));
                    startActivity(i);
                }
            });
        }


        return v;
    }

    private class ListItem {
        String disabilityTitle;
        String availabilityTitle;
        String availabilityInfo;
    }

    private class ListItemAreas {
        String functionalAreaTitle;
        String areaAccessibility;
    }

    static class ViewHolder {
        public TextView disabilityTitle;
        public TextView availabilityTitle;
        public TextView availabilityInfo;
    }

    private class AccMapListAdapter extends BaseAdapter {

        private ArrayList<ListItem> items;
        private Context context;

        public AccMapListAdapter(Context _context, ArrayList<ListItem> _items) {
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
                convertView = inflater.inflate(R.layout.pointinfo_list_item, parent, false);

                holder = new ViewHolder();
                holder.disabilityTitle = (TextView) convertView.findViewById(R.id.list_item_disability_name);
                holder.availabilityTitle = (TextView) convertView.findViewById(R.id.list_item_availability_name);
                holder.availabilityInfo = (TextView) convertView.findViewById(R.id.list_item_availability_description);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListItem item = items.get(position);

            holder.disabilityTitle.setText(item.disabilityTitle);
            holder.availabilityTitle.setText(item.availabilityTitle);
            holder.availabilityInfo.setText(item.availabilityInfo);


            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //if (!isMapLinkShowing) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.activity_pointinfo, menu);
        //}

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_onmap:
                if (objectLocation == null)
                    return false;

                Intent resultIntent = new Intent();
                resultIntent.putExtra(Constants.RESULT_INTENT_VALUE, objectLocation.latitude + "," + objectLocation.longitude);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                break;
        }

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

}
