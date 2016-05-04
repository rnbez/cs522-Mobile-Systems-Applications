package com.chat_maps.chatmaps.activities;

import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.chat_maps.chatmaps.R;
import com.chat_maps.chatmaps.contracts.PeerContract;
import com.chat_maps.chatmaps.entities.Peer;
import com.chat_maps.chatmaps.managers.IQueryListener;
import com.chat_maps.chatmaps.managers.QueryBuilder;
import com.chat_maps.chatmaps.managers.TypedCursor;
import com.chat_maps.chatmaps.utils.App;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class ChatMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    final static public String TAG = ChatMapsActivity.class.getCanonicalName();

    final static public String EXTRA_DATABASE_KEY = App.APP_NAMESPACE + ".EXTRA_DATABASE_KEY";
    final static public String EXTRA_USERID = App.APP_NAMESPACE + ".EXTRA_USERID";
    final static public String EXTRA_LATITUDE = App.APP_NAMESPACE + ".EXTRA_LATITUDE";
    final static public String EXTRA_LONGITUDE = App.APP_NAMESPACE + ".EXTRA_LONGITUDE";

    private GoogleMap map;
    private char[] databaseKey;
    private ArrayList<Peer> peers;
    private long userId;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        userId = -1;
        latitude = Double.MAX_VALUE;
        longitude = Double.MAX_VALUE;

        Intent callingIntent = this.getIntent();
        if (callingIntent != null) {
            Bundle bundle = callingIntent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey(EXTRA_DATABASE_KEY))
                    databaseKey = bundle.getCharArray(EXTRA_DATABASE_KEY);
                if (bundle.containsKey(EXTRA_USERID))
                    userId = bundle.getLong(EXTRA_USERID);
                if (bundle.containsKey(EXTRA_LATITUDE))
                    latitude = bundle.getDouble(EXTRA_LATITUDE);
                if (bundle.containsKey(EXTRA_LONGITUDE))
                    longitude = bundle.getDouble(EXTRA_LONGITUDE);
                getPeers();
            } else
                Toast.makeText(getApplicationContext(), "You need to launch this app from the ChatApp.", Toast.LENGTH_LONG).show();
//            Uri uri  = callingIntent.getData();
//            Log.d(TAG, uri.toString());
        } else
            Toast.makeText(getApplicationContext(), "You need to launch this app from the ChatApp.", Toast.LENGTH_LONG).show();


//        getPeers();
    }

    private void getPeers() {
        QueryBuilder.executeQuery(TAG,
                this,
                PeerContract.withDatabaseKeyUri(databaseKey),
                PeerContract.CURSOR_LOADER_ID,
                PeerContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Peer>() {
                    @Override
                    public void handleResults(TypedCursor<Peer> typedCursor) {
                        Log.d(TAG, "QueryBuilder returned");
                        peers = new ArrayList<>();
                        Cursor cursor = typedCursor.getCursor();
                        if (cursor.moveToFirst()) {
                            do {
                                peers.add(new Peer(cursor));
                            } while (cursor.moveToNext());
                        }

                        updateMap();
                    }

                    @Override
                    public void closeResults() {
                        Log.d(TAG, "QueryBuilder closed");
                    }
                });
    }

    private void updateMap() {
        map.clear();
        for (Peer p :
                peers) {
//            LatLng hoboken = new LatLng(40.7447, -74.0299);
            String name = p.getName();
            if (userId == p.getId()){
                name += " (me)";
            }
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getLatitute(), p.getLongitude()))
                    .title(name)
                    .snippet(p.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        if (!peers.isEmpty() && this.latitude != Double.MAX_VALUE && this.longitude != Double.MAX_VALUE)
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(this.latitude, this.longitude)));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));

//        LatLng hoboken = new LatLng(40.7447, -74.0299);
//        map.addMarker(new MarkerOptions()
//                .position(hoboken)
//                .title("Rafael (me)")
//                .snippet("Hoboken, New Jersey")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        map.addMarker(new MarkerOptions()
//                .position(new LatLng(40.7661837, -73.9793213))
//                .title("Rafael (me)")
//                .snippet("Hoboken, New Jersey")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//        map.moveCamera(CameraUpdateFactory.newLatLng(hoboken));
    }
}
