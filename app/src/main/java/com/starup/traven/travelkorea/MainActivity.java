package com.starup.traven.travelkorea;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    public class MyInfo {
        public int language = 0;    //0(en) 1(kor) 2(ch) 3(jp)
        public int gender = 0;      //0(man) 1(woman)
        public int age = 0;
    };

    static public MyInfo myinfo;
    private static final int NUM_ITEMS = 4;
    private static Fragment fragments[];
    private static final String Tag = "MainActivity";

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private Menu mMenu = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myinfo = new MyInfo();
        fragments = new Fragment[4];

        readObject();

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_actionbar);
        Drawable d = new BitmapDrawable(getResources(), bitmap);
        bar.setBackgroundDrawable(d);   //new ColorDrawable(Color.CYAN));

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new MainAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        changeMenu();
        return true;
    }

    public void readObject() {
        try
        {
            String filePath = Environment.getExternalStorageDirectory().toString() + "/TravelKorea/info.ini";
            File file = new File(filePath);

            if(file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));

                if (ois != null) {
                    myinfo.language = ois.readInt();
                    myinfo.age = ois.readInt();
                    myinfo.gender = ois.readInt();

                    ois.close();
                }
            }
        }catch (Exception e) {

        }
    }

    public void changeMenu() {
        if(mMenu != null) {
            MenuItem mapitem = mMenu.findItem(R.id.go_map);
            MenuItem scheduleitem = mMenu.findItem(R.id.go_schedule);
            MenuItem optionitem = mMenu.findItem(R.id.my_account);

            int lang = myinfo.language;
            if (lang == 1) {
                mapitem.setTitle("지도가기");
                scheduleitem.setTitle("내 스케쥴");
                optionitem.setTitle("설정");
            } else if (lang == 2) {
                mapitem.setTitle("地图");
                scheduleitem.setTitle("日程");
                optionitem.setTitle("设定");
            } else if (lang == 3) {
                mapitem.setTitle("ちず");
                scheduleitem.setTitle("スケジュール");
                optionitem.setTitle("せってい");
            } else {
                mapitem.setTitle("Map");
                scheduleitem.setTitle("Scehdule");
                optionitem.setTitle("Setting");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.go_map) {
            mPager.setCurrentItem(2);
        } else if(id == R.id.go_schedule) {
            mPager.setCurrentItem(3);
        } else if(id == R.id.my_account) {
            final Intent intent = new Intent(this, MyAccount.class);

            intent.putExtra("lang", myinfo.language);
            intent.putExtra("age", myinfo.age);
            intent.putExtra("gender", myinfo.gender);

            startActivityForResult(intent, 2);
        }
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        changeMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // check if the request code is same as what is passed  here it is 2
        if(resultCode==RESULT_OK)
        {
            int lang = data.getIntExtra("lang", 0);
            int age = data.getIntExtra("age", 0);
            int gender = data.getIntExtra("gender", 0);

            myinfo.age = age;
            myinfo.language = lang;
            myinfo.gender = gender;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            ;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
//            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }


    public static class MainAdapter extends FragmentPagerAdapter {
        public MainAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments[position];

            if(fragment == null) {
                if (position == 0) {
                    fragment = fragments[position] = new MainFragment();
                } else if (position == 1) {
                    fragment = fragments[position] = new ImageGridFragment();
                } else if (position == 2) {
                    fragment = fragments[position] = new GoogleMapFragment();
                } else {
                    fragment = fragments[position] = new ScheduleFragment();
                }
            }

            return fragment;
        }
    }

}

