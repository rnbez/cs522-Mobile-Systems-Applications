package com.chat_maps.chatmaps.activities;

import android.database.Cursor;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.chat_maps.chatmaps.R;
import com.chat_maps.chatmaps.contracts.ChatroomContract;
import com.chat_maps.chatmaps.contracts.PeerContract;
import com.chat_maps.chatmaps.entities.Chatroom;
import com.chat_maps.chatmaps.entities.Peer;
import com.chat_maps.chatmaps.managers.IQueryListener;
import com.chat_maps.chatmaps.managers.QueryBuilder;
import com.chat_maps.chatmaps.managers.TypedCursor;
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

    private GoogleMap map;
    private ArrayList<Peer> peers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        QueryBuilder.executeQuery(TAG,
                this,
                PeerContract.CONTENT_URI,
                PeerContract.CURSOR_LOADER_ID,
                PeerContract.DEFAULT_ENTITY_CREATOR,
                new IQueryListener<Peer>() {
                    @Override
                    public void handleResults(TypedCursor<Peer> typedCursor) {
                        Log.d(TAG, "QueryBuilder returned");
                        peers = new ArrayList<>();
                        Cursor cursor = typedCursor.getCursor();
                        if (cursor.moveToFirst()){
                            do{
                                peers.add(new Peer(cursor));
                            }while(cursor.moveToNext());
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
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getLatitute(), p.getLongitude()))
                    .title(p.getName())
                    .snippet(p.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }
        if (!peers.isEmpty())
            map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(peers.get(0).getLatitute(), peers.get(0).getLongitude())));
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
