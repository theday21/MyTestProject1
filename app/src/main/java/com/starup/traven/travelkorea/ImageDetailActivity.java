package com.starup.traven.travelkorea;


import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.starup.traven.travelkorea.R;
import com.starup.traven.travelkorea.XMLParser.VisitKoreaXmlParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class ImageDetailActivity extends AppCompatActivity {
    private List<VisitKoreaXmlParser.Entry> mGridData = null;

    private static final String service_key = "tcMsl%2FuAaldSmyvnY78FqTHtqWLrQUg%2FYLDcNX389OGxcA%2BpXV2ejk86zNsw1XJZXNwUiIw6F8e6BbToTVpblg%3D%3D";
    public String langService = "KorService";
    public String contenttypeId = null;
    public String contentid = null;
    public  Bitmap bitmap = null;
    public  Bitmap firstImg = null;

    public String URL1 = "http://api.visitkorea.or.kr/openapi/service/rest/"; //+LangService
    public String URL2 = "/detailCommon?ServiceKey="; // +ServiceKey
    public String URL3 = "&contentTypeId="; // +contenttypeid
    public String URL4 = "&contentId="; // +contentid
    public String URL5 = "&MobileOS=AND&MobileApp=TourAPI2.0_Guide&defaultYN=Y&firstImageYN=Y&areacodeYN=Y&catcodeYN=Y&addrinfoYN=Y&mapinfoYN=Y&overviewYN=Y&transGuideYN=Y";

    final String DB_URL = "http://travelkore.maru.net";     //http://143.248.6.251
    final String url_create_user = DB_URL + "/create_row.php";
    final String url_get_row = DB_URL + "/get_row.php";
    final String url_get_row2 = DB_URL + "/get_row2.php";
    final String TAG_SUCCESS = "success";

    public int num_up = 0;
    public int num_down = 0;


    /**
     * VisitKorea Open API connection.
     */
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;

    // The user's current network preference setting.
    public static String sPref = null;

    private int thumb_state = 1;
    public int age = 0;
    public int lang = 0;
    public int gender = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        Intent intent = getIntent();
        langService = intent.getStringExtra("langService");
        contenttypeId = intent.getStringExtra("contenttypeId");
        contentid = intent.getStringExtra("contentid");

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_actionbar);
        Drawable d = new BitmapDrawable(getResources(), bitmap);
        bar.setBackgroundDrawable(d);   //new ColorDrawable(Color.CYAN));

        bar.setDisplayHomeAsUpEnabled(false);

        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        sPref = sharedPrefs.getString("listPref", "Wi-Fi");


        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    String date = new SimpleDateFormat("yyyy-MM-dd:hh:mm:ss").format(new Date());
                    TourItem tourItem = new TourItem(langService, contenttypeId, contentid, date, mGridData.get(0).firstimage, mGridData.get(0).addr1);

                    tourItem.title = mGridData.get(0).title;
                    tourItem.phone = mGridData.get(0).phone;

                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    calendar.clear();
                    calendar.set(2011, Calendar.OCTOBER, 1);
                    long secondsSinceEpoch = calendar.getTimeInMillis() / 1000L;

                    tourItem.date = System.currentTimeMillis();

                    saveObject(tourItem);


                    return true;
                }
                return true;
            }
        });


        ImageView image = (ImageView) findViewById(R.id.thumb_up);
        image.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                thumb_state = 1;
                new CreateNewUser().execute();

//                Context context = getApplicationContext();
//                CharSequence text = "Good place!";
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();

                updateReview();
            }
        });

        ImageView image2 = (ImageView) findViewById(R.id.thumb_down);
        image2.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                thumb_state = 0;
                new CreateNewUser().execute();
//
//                Context context = getApplicationContext();
//                CharSequence text = "Bad place...";
//                int duration = Toast.LENGTH_SHORT;
//
//                Toast toast = Toast.makeText(context, text, duration);
//                toast.show();

                updateReview();
            }
        });


        updateConnectedFlags();
        loadPage();
    }


    public void saveObject(TourItem item){
        try
        {
            String folder_name = "TravelKorea";

            File f = new File(Environment.getExternalStorageDirectory(), folder_name);
            if (!f.exists()) {
                f.mkdirs();
            }

            //String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String filename = item.contentid + ".dat";
            String filePath = Environment.getExternalStorageDirectory().toString() + "/TravelKorea/" + filename;

            File file = new File(filePath);

            if (!file.exists()) {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file)); //Select where you wish to save the file...
                oos.writeObject(item); // write the class as an 'object'

                {
                    oos.writeInt(firstImg.getWidth());
                    oos.writeInt(firstImg.getHeight());


                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    firstImg.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    TourItemBitmap bitmapDataObject = new TourItemBitmap();
                    bitmapDataObject.imageByteArray = stream.toByteArray();

                    oos.writeObject(bitmapDataObject);
                }

                oos.flush(); // flush the stream to insure all of the information was written to 'save_object.bin'
                oos.close();// close the stream


            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally {
            Context context = getApplicationContext();
            CharSequence text = "Tour information saved.";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateReview();
    }

    void updateReview() {
        new getReviewUp().execute();
        new getReviewDown().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public String getURL() {
        return URL1 + langService + URL2 + service_key + URL3 + contenttypeId + URL4 + contentid + URL5;
    }

    public void updateItem() {
        if (mGridData.size() > 0) {
            VisitKoreaXmlParser.Entry entry = mGridData.get(0);
            TextView tv = (TextView) findViewById(R.id.detail_title);
            tv.setText(entry.title);

            /// Overview
            tv = (TextView) findViewById(R.id.detail_overview);
            tv.setText(Html.fromHtml(entry.overview));

            new LoadImage().execute(entry.firstimage);


        }

    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }

    private void loadPage() {
        new DownloadXmlTask().execute(getURL());

//        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
//                || ((sPref.equals(WIFI)) && (wifiConnected))) {
//            // AsyncTask subclass
//            new DownloadXmlTask().execute(getURL());
//        } else {
//            //showErrorPage();
//        }
    }

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

        static final String Tag = "DownloadTask";

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //setContentView(R.layout.main);
            // Displays the HTML string in the UI via a WebView
            //WebView myWebView = (WebView) findViewById(R.id.webview);
            //myWebView.loadData(result, "text/html", null);

            android.util.Log.i(Tag, "onPoseExecute");

            if (result.equals("OK")) {
                updateItem();
            }
        }

        private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
            InputStream stream = null;
            VisitKoreaXmlParser stackOverflowXmlParser = new VisitKoreaXmlParser();

            try {
                stream = downloadUrl(urlString);

                mGridData = stackOverflowXmlParser.parse(stream);

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            return "OK";
        }

        private InputStream downloadUrl(String urlString) throws IOException {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            InputStream stream = conn.getInputStream();
            return stream;
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... args) {
            Bitmap bitmap = null;

            try {
                bitmap = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            ImageView imgView = (ImageView) findViewById(R.id.detail_image1);
            imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgView.setImageBitmap(image);

            firstImg = image;
        }
    }

    public void readObject() {
        try
        {
            String filePath = Environment.getExternalStorageDirectory().toString() + "/TravelKorea/info.ini";
            File file = new File(filePath);

            if(file.exists()) {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));

                lang = ois.readInt();
                age = ois.readInt();
                gender = ois.readInt();

                ois.close();
            }

        }catch (Exception e) {

        }
    }

    class CreateNewUser extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String tmDevice = tm.getDeviceId();

            Long tmp = Long.parseLong(tmDevice);
            String userId = Long.toString(tmp/100000L);
            String contentId = contentid;
            String review = "0";
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());


            // Building Parameters
            ContentValues values=new ContentValues();
            values.put("userID", userId);
            values.put("contentD", contentId);
            values.put("review", review);
            values.put("date", date);

            readObject();
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("userID", userId));
            nameValuePairs.add(new BasicNameValuePair("contentID", contentId));
            nameValuePairs.add(new BasicNameValuePair("review", Integer.toString(thumb_state)));
            nameValuePairs.add(new BasicNameValuePair("date", date));
            nameValuePairs.add(new BasicNameValuePair("age", Integer.toString(age)));
            nameValuePairs.add(new BasicNameValuePair("gender", Integer.toString(gender)));
            nameValuePairs.add(new BasicNameValuePair("lang", Integer.toString(lang)));

            InputStream is = null;
            String result = null;

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url_create_user);

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();

            } catch (ClientProtocolException e) {
                ;
            } catch ( UnsupportedEncodingException e) {
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result;
        }
    }


    class getReviewUp extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String tmDevice = tm.getDeviceId();

            Long tmp = Long.parseLong(tmDevice);
            String userId = Long.toString(tmp/100000L);
            String contentId = contentid;
            String review = "0";
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());


            // Building Parameters
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("contentID", contentId));

            InputStream is = null;
            String result = null;

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url_get_row);

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();

            } catch (ClientProtocolException e) {
                ;
            } catch ( UnsupportedEncodingException e) {
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONObject reader = new JSONObject(result);
                num_up = Integer.parseInt(reader.optString("success").toString());
            } catch (Exception e) {

            }
            return result;
        }

        protected void onPostExecute(String file_url) {
            TextView tv1 = (TextView)findViewById(R.id.thumb_up_num);
            tv1.setText(Integer.toString(num_up));
        }
    }

    class getReviewDown extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            String tmDevice = tm.getDeviceId();

            Long tmp = Long.parseLong(tmDevice);
            String userId = Long.toString(tmp/100000L);
            String contentId = contentid;
            String review = "0";
            String date = new SimpleDateFormat("yyyyMMdd").format(new Date());


            // Building Parameters
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("contentID", contentId));

            InputStream is = null;
            String result = null;

            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url_get_row2);

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity entity = response.getEntity();

                is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();

            } catch (ClientProtocolException e) {
                ;
            } catch ( UnsupportedEncodingException e) {
                ;
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                JSONObject reader = new JSONObject(result);
                num_down = Integer.parseInt(reader.optString("success").toString());

            } catch (Exception e) {

            }
            return result;
        }

        protected void onPostExecute(String file_url) {
            TextView tv2 = (TextView)findViewById(R.id.thumb_down_num);
            tv2.setText(Integer.toString(num_down));
        }
    }
}
