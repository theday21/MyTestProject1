package com.starup.traven.travelkorea;

import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.BaseAdapter;

import com.starup.traven.travelkorea.XMLParser.VisitKoreaXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by moco-lab on 2015-09-12.
 */
public class TourAPI {
    /**
     *  VisitKorea Open API connection.
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

    private List<VisitKoreaXmlParser.Entry> mGridData = null;
    private BaseAdapter mAdapter = null;

    private String contentID = "12";
    private static final String radius = "2000";
    private String mapX = "126.981106";
    private String mapY = "37.568477";

    private static final String service_key = "tcMsl%2FuAaldSmyvnY78FqTHtqWLrQUg%2FYLDcNX389OGxcA%2BpXV2ejk86zNsw1XJZXNwUiIw6F8e6BbToTVpblg%3D%3D";
    private static final String URL =
            "http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?ServiceKey="+
                    service_key +
                    "&contentTypeId=12&mapX=126.981106&mapY=37.568477&radius=2000&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=100&pageNo=1";
//            "http://api.visitkorea.or.kr/openapi/service/rest/EngService/areaBasedList?ServiceKey="+service_key+
//                    "&contentTypeId=76&areaCode=1&sigunguCode=&cat1=&cat2=&cat3=&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=100&pageNo=1";


    GoogleMapFragment mContext = null;

    public TourAPI(GoogleMapFragment context) {
        mContext = context;
    }

    void setAdapter(BaseAdapter adapter) {
        mAdapter = adapter;
    }

    private String getURL()
    {
        String visitURL =
                /*"http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?ServiceKey="+
                        service_key +
                        "&contentTypeId=12&mapX=126.981106&mapY=37.568477&radius=2000&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=100&pageNo=1";
        */
                "http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?ServiceKey="+
                        service_key +
                        "&contentTypeId=" + contentID +
                        "&mapX=" + mapX + "&mapY=" + mapY +
                        "&radius=" + radius +
                        "&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=100&pageNo=1";

        return visitURL;
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
    public void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) mContext.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeInfo = connMgr.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()) {
            wifiConnected = activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = activeInfo.getType() == ConnectivityManager.TYPE_MOBILE;
        } else {
            wifiConnected = false;
            mobileConnected = false;
        }
    }

    // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    // This avoids UI lock up. To prevent network operations from
    // causing a delay that results in a poor user experience, always perform
    // network operations on a separate thread from the UI.
    public void loadPage() {
        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                || ((sPref.equals(WIFI)) && (wifiConnected))) {
            // AsyncTask subclass
            new DownloadXmlTask().execute(getURL());
        } else {
            showErrorPage();
        }
    }

    public void showErrorPage() {
        ;
    }

    // Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    public class DownloadXmlTask extends AsyncTask<String, Void, String> {

        static final String Tag = "DownloadTask";

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return mContext.getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return mContext.getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //setContentView(R.layout.main);
            // Displays the HTML string in the UI via a WebView
            //WebView myWebView = (WebView) findViewById(R.id.webview);
            //myWebView.loadData(result, "text/html", null);

            android.util.Log.i(Tag, "onPoseExecute");

            if(result.equals("OK")) {
                //mGridAdapter.setGridData(mGridData);
                //mAdapter.notifyDataSetChanged();
                mContext.notifyDataSetChanged();
            }
        }
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
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

    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        java.net.URL url = new URL(urlString);
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
