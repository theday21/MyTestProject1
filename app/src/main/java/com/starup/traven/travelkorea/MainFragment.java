package com.starup.traven.travelkorea;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.starup.traven.travelkorea.XMLParser.VisitKoreaXmlParser;
import com.starup.traven.travelkorea.provider.Images;
import com.starup.traven.travelkorea.util.ImageCache;
import com.starup.traven.travelkorea.util.ImageFetcher;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by moco-lab on 2015-09-15.
 */
public class MainFragment extends Fragment {
    private int mAge;
    private int mGender;
    private int mLanguage;


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

    private List<VisitKoreaXmlParser.Entry> mGridData = null;

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

    String getURL()
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
                        "&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=10&pageNo=1";

        return visitURL;
    }
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;

    public Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAge = ((MainActivity)getActivity()).myinfo.age;
        mGender = ((MainActivity)getActivity()).myinfo.gender;
        mLanguage = ((MainActivity)getActivity()).myinfo.language;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.


        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.main_fragment_layout, container, false);

        mAdapter = new ImageAdapter(getActivity());
        ListView listView = (ListView)rootView.findViewById(R.id.main_listview);
        listView.setAdapter(mAdapter);

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_max_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        //mAdapter = new ImageAdapter(getActivity());

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        sPref = sharedPrefs.getString("listPref", "Wi-Fi");

        updateConnectedFlags();
        loadPage();

        return rootView;
    }

    private void updateConnectedFlags() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

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
        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
                || ((sPref.equals(WIFI)) && (wifiConnected))) {
            // AsyncTask subclass
            new DownloadXmlTask().execute(getURL());
        } else {
            showErrorPage();
        }
    }

    private void showErrorPage() {
        ;
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

            if(result.equals("OK")) {
                //mGridAdapter.setGridData(mGridData);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    // Uploads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        VisitKoreaXmlParser stackOverflowXmlParser = new VisitKoreaXmlParser();

/*         String title = null;
        String url = null;
        String summary = null;
        Calendar rightNow = Calendar.getInstance();
        DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

        // Checks whether the user set the preference to include summary text
       SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean pref = sharedPrefs.getBoolean("summaryPref", false);

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
                formatter.format(rightNow.getTime()) + "</em>");
    */
        try {
            stream = downloadUrl(urlString);

            if(mGridData != null)
                mGridData.clear();

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


    /**
     * The main adapter that backs the GridView. This is fairly standard except the number of
     * columns in the GridView is used to create a fake top row of empty views as we use a
     * transparent ActionBar and don't want the real top row of images to start off covered by it.
     */
    private class ImageAdapter extends BaseAdapter {

        private final Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private int mActionBarHeight = 0;
        private ListView.LayoutParams mImageViewLayoutParams;
        private LayoutInflater mLayoutInflater;

        public ImageAdapter(Context context) {
            super();
            mContext = context;
            mImageViewLayoutParams = new ListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            // Calculate ActionBar height
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(
                    android.R.attr.actionBarSize, tv, true)) {
                mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                        tv.data, context.getResources().getDisplayMetrics());
            }


            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            if(mGridData != null)
                return mGridData.size();
            else
                return 0;
        }

        @Override
        public Object getItem(int position) {
            return position < getCount() ?
                    Images.imageThumbUrls[position]:null;
        }

        @Override
        public long getItemId(int position) {
            return position < getCount() ? position : 0;
        }

        /*      @Override
              public int getViewTypeCount() {
                  // Two types of views, the normal ImageView and the top row of empty views
                  return 2;
              }

              @Override
              public int getItemViewType(int position) {
                  return (position < mNumColumns) ? 1 : 0;
              }
      */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            LinearLayout itemView = null;
            ImageView imageView = null;
            TextView titleView = null;
            TextView overviewView = null;
            TextView addrView = null;
            ImageView starImageView = null;

            if (convertView == null) {
                itemView = (LinearLayout) mLayoutInflater.inflate(
                        R.layout.main_fragment_item, container, false);
            } else {
                itemView = (LinearLayout)convertView;
            }

            imageView = (ImageView)itemView.findViewById(R.id.main_list_img);
            titleView = (TextView)itemView.findViewById(R.id.main_list_title);
            addrView = (TextView)itemView.findViewById(R.id.main_list_addr);
            overviewView = (TextView)itemView.findViewById(R.id.main_list_overview);
            //overviewView = (TextView)itemView.findViewById(R.id.list_overview);


            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setLayoutParams(mImageViewLayoutParams);

            if(mGridData != null && position < getCount()) {
                String image_url = mGridData.get(position).firstimage;

                if (image_url != null)
                    mImageFetcher.loadImage(image_url, imageView);
            }

            titleView.setText(mGridData.get(position).title);
            addrView.setText(mGridData.get(position).addr1);
            overviewView.setText(mGridData.get(position).overview);
            // overviewView.setText(mGridData.get(position).overview);


            return itemView;
        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams =
                    new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }
    }
}
