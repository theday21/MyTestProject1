package com.starup.traven.travelkorea;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by moco-lab on 2015-09-15.
 */
public class MainFragment extends Fragment {

    public class MainItem {
        public String contentID = null;
        public String contentTypeID = null;
    };

    public class MainTourItem {
        public String title = null;
        public String address = null;
        public String overview = null;
        public String imgaddr = null;
        public Bitmap bitmap = null;

        public String contentID = null;
        public String contentTypeId = null;
    }

    public List<MainItem> mainItems = null;
    public List<MainTourItem> mainTourItems = null;

    private int mAge;
    private int mGender;
    private int mLanguage;

    private String langService = "EngService";
    public String contenttypeId = null;
    public String contentid = null;

    private static final String service_key = "tcMsl%2FuAaldSmyvnY78FqTHtqWLrQUg%2FYLDcNX389OGxcA%2BpXV2ejk86zNsw1XJZXNwUiIw6F8e6BbToTVpblg%3D%3D";
    public String URL1 = "http://api.visitkorea.or.kr/openapi/service/rest/"; //+LangService
    public String URL2 = "/detailCommon?ServiceKey="; // +ServiceKey
    public String URL3 = "&contentTypeId="; // +contenttypeid
    public String URL4 = "&contentId="; // +contentid
    public String URL5 = "&MobileOS=AND&MobileApp=TourAPI2.0_Guide&defaultYN=Y&firstImageYN=Y&areacodeYN=Y&catcodeYN=Y&addrinfoYN=Y&mapinfoYN=Y&overviewYN=Y&transGuideYN=Y";


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


    String getURL(String ctId, String cID)
    {
        return URL1 + langService + URL2 + service_key + URL3 + ctId + URL4 + cID + URL5;
    }

    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    final String url_get_main_rows = "http://travelkore.maru.net/main_get_row.php"; //"http://143.248.6.251/main_get_row.php";

    ViewGroup rootView = null;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ImageAdapter mAdapter;
    private ImageFetcher mImageFetcher;

    public Context mContext;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateOption();

        mainItems = new ArrayList<>();
        mainTourItems = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.


        rootView = (ViewGroup) inflater
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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        updateOption();

        mainItems.clear();
        mainTourItems.clear();

        new RecommendsDB().execute();
    }

    public void updateOption() {
        mAge = ((MainActivity)getActivity()).myinfo.age;
        mGender = ((MainActivity)getActivity()).myinfo.gender;
        mLanguage = ((MainActivity)getActivity()).myinfo.language;

        if(mLanguage == 1) {
            langService = "KorService";
        } else if(mLanguage == 2) {
            langService = "ChsService";
        } else if(mLanguage == 3) {
            langService = "JpnService";
        } else {
            langService = "EngService";
        }
    }

    class RecommendsDB extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("LangServ", langService));

            InputStream is = null;
            String result = null;
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url_get_main_rows);

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
                JSONObject jsonRootObject = new JSONObject(result);
                JSONArray jsonArray = jsonRootObject.optJSONArray("TourList");

                for(int i=0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String contentID = jsonObject.optString("contentID").toString();
                    String contentTypeID = jsonObject.optString("contentTypeID").toString();

                    MainItem item = new MainItem();
                    item.contentID = contentID;
                    item.contentTypeID = contentTypeID;

                    mainItems.add(item);
                }

            } catch (Exception e) {

            }

            return result;
        }

        @Override
        protected void onPostExecute(String file_url) {
            for(int i=0 ; i<mainItems.size() ; ++i) {
                MainItem mi = mainItems.get(i);
                new DownloadXmlTask().execute(getURL(mi.contentTypeID, mi.contentID));

//                if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
//                        || ((sPref.equals(WIFI)) && (wifiConnected))) {
//                    // AsyncTask subclass
//
//                    MainItem mi = mainItems.get(i);
//                    new DownloadXmlTask().execute(getURL(mi.contentTypeID, mi.contentID));
//                } else {
//                    showErrorPage();
//                }
            }
        }
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

//    private void loadPage() {
//        if (((sPref.equals(ANY)) && (wifiConnected || mobileConnected))
//                || ((sPref.equals(WIFI)) && (wifiConnected))) {
//            // AsyncTask subclass
//            new DownloadXmlTask().execute(getURL());
//        } else {
//            showErrorPage();
//        }
//    }

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
            android.util.Log.i(Tag, "onPoseExecute");

            if(result.equals("OK")) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        public String title = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... args) {
            title = args[1];
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

            for(int i=0 ; i<mainTourItems.size() ; ++i) {
                if(mainTourItems.get(i).title.equals(title)) {
                    mainTourItems.get(i).bitmap = image;
                    break;
                }
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

            List<VisitKoreaXmlParser.Entry> entires = stackOverflowXmlParser.parse(stream);
            VisitKoreaXmlParser.Entry entry = entires.get(0);

            MainTourItem mti = new MainTourItem();
            mti.title = entry.title;
            mti.address = entry.addr1;
            mti.overview = entry.overview;
            mti.imgaddr = entry.firstimage;
            mti.contentID = entry.contentid;
            mti.contentTypeId = entry.contenttypeId;

            mainTourItems.add(mti);
            //new LoadImage().execute(entry.firstimage, entry.title);

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
            if(mainTourItems != null)
                return mainTourItems.size();
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

            if(mainTourItems != null && position < getCount()) {
                String image_url = mainTourItems.get(position).imgaddr;

                if (image_url != null)
                    mImageFetcher.loadImage(image_url, imageView);
            }


            titleView.setText(mainTourItems.get(position).title);//      mGridData.get(position).title);
            addrView.setText(mainTourItems.get(position).address);
            overviewView.setText(Html.fromHtml(mainTourItems.get(position).overview));
            //imageView.setImageBitmap(mainTourItems.get(position).bitmap);
            // overviewView.setText(mGridData.get(position).overview);

            final MainTourItem mti = mainTourItems.get(position);

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
                        i.putExtra("langService", langService);
                        i.putExtra("contenttypeId", mti.contentTypeId);
                        i.putExtra("contentid", mti.contentID);
                        startActivity(i);
                    }

                    return true;
                }
            });

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
