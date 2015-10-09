package com.starup.traven.travelkorea;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.starup.traven.travelkorea.XMLParser.VisitKoreaXmlParser;

import java.util.List;

/**
 * Created by moco-lab on 2015-09-12.
 */
public class GoogleMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener {
    private int mAge;
    private int mGender;
    private int mLanguage;

    private static View view;
    private Fragment child = null;
    SupportMapFragment mMapFragment = null;
    private GoogleMap mMap;
    static LatLng currentLoc = new LatLng( 37.56, 126.97);
    private List<VisitKoreaXmlParser.Entry> mGridData = null;


    //private TourAPI mTourAPIConnecter = null;

    public void changeGridDataSet(List<VisitKoreaXmlParser.Entry> gridData) {
        mGridData = gridData;
        mMap.clear();
        mMap.setOnMapLongClickListener(this);

//        List<Double> xList = new List<Double>();
//        List<Double> yList = new List<Double>();

        for(int i=0 ; i<mGridData.size() ; i++) {
            VisitKoreaXmlParser.Entry entry = mGridData.get(i);

            if(entry.mapx < 0)
                continue;

            double x = entry.mapx;
            double y = entry.mapy;

//            String s = String.format("%.2f", x);
//            x = Double.parseDouble(s);
//
//            s = String.format("%.2f", y);
//            y = Double.parseDouble(s);

            LatLng locate = new LatLng(y, x);

            mMap.addMarker(new MarkerOptions().position(locate)
                    .title(entry.title));
        }

        try {
            Thread.sleep(500);
        } catch (Exception e) {
            ;
        }

        mMap.addMarker(new MarkerOptions()
                    .position(currentLoc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("현재위치"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));
    }
    public void notifyDataSetChanged() {
        Location loc = ((MainActivity)getActivity()).mLastLocation;

        if(loc != null){
            currentLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(currentLoc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("현재위치"));
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 13));
    }

    @Override
    public void onCreate(Bundle savedInstanceState)   {
        super.onCreate(savedInstanceState);

    /*    mTourAPIConnecter = new TourAPI(this);

        mTourAPIConnecter.updateConnectedFlags();
        mTourAPIConnecter.loadPage();
        */

        mAge = ((MainActivity)getActivity()).myinfo.age;
        mGender = ((MainActivity)getActivity()).myinfo.gender;
        mLanguage = ((MainActivity)getActivity()).myinfo.language;

        Location loc = ((MainActivity)getActivity()).mLastLocation;

        if(loc != null){
            currentLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if( view != null) {
            ViewGroup parent = (ViewGroup)view.getParent();
            if(parent != null)
                parent.removeView(view);
        }
        try {
            // Inflate the layout containing a title and body text.
            view = (ViewGroup) inflater
                    .inflate(R.layout.fragment_map_layout, container, false);

            mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mMapFragment.getMapAsync(this);

            if (child == null) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

                child = new TourListFragment();
                transaction.add(R.id.information_list_fragment, child).commit();

                TourListFragment tourFragment = (TourListFragment) child;
                tourFragment.setParentFragment(this);
            }
        } catch (InflateException e) {

        }

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Map is ready to be used.
        mMap = googleMap;

        // Set the long click listener as a way to exit the map.
        mMap.setOnMapLongClickListener(this);
        // Add a marker with a title that is shown in its info window.


        if(mGridData != null) {
            for (int i = 0; i < mGridData.size(); i++) {
                VisitKoreaXmlParser.Entry entry = mGridData.get(i);

                LatLng locate = new LatLng(entry.mapx, entry.mapy);

                mMap.addMarker(new MarkerOptions().position(locate)
                        .title(entry.title));
            }
        }

        Location loc = ((MainActivity)getActivity()).mLastLocation;

        if(loc != null) {
            currentLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(currentLoc)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title("현재위치"));
        }

        // Move the camera to show the marker.
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 3));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // Display the dismiss overlay with a button to exit this activity.
        //mDismissOverlay.show();
    }


}
