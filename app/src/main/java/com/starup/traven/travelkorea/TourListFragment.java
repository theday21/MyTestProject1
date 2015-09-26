/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.starup.traven.travelkorea;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.starup.traven.travelkorea.GoogleMapFragment;
import com.starup.traven.travelkorea.ImageDetailActivity;
import com.starup.traven.travelkorea.R;
import com.starup.traven.travelkorea.provider.Images;
import com.starup.traven.travelkorea.util.ImageCache;
import com.starup.traven.travelkorea.util.ImageFetcher;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;

import com.starup.traven.travelkorea.XMLParser.VisitKoreaXmlParser;
import com.starup.traven.travelkorea.XMLParser.VisitKoreaXmlParser.Entry;
/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class TourListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private String contentID = "12";
    private static final String radius = "2000";
    private String mapX = "126.981106";
    private String mapY = "37.568477";

    private static final String service_key = "tcMsl%2FuAaldSmyvnY78FqTHtqWLrQUg%2FYLDcNX389OGxcA%2BpXV2ejk86zNsw1XJZXNwUiIw6F8e6BbToTVpblg%3D%3D";
    private static final String URL =
            "http://api.visitkorea.or.kr/openapi/service/rest/KorService/locationBasedList?ServiceKey="+
                    service_key +
                    "&contentTypeId=76&mapX=126.981106&mapY=37.568477&radius=2000&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=100&pageNo=1";
//            "http://api.visitkorea.or.kr/openapi/service/rest/EngService/areaBasedList?ServiceKey="+service_key+
//                    "&contentTypeId=76&areaCode=1&sigunguCode=&cat1=&cat2=&cat3=&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=100&pageNo=1";

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
                        "&listYN=Y&MobileOS=ETC&MobileApp=TourAPI3.0_Guide&arrange=A&numOfRows=100&pageNo=1";

        return visitURL;
    }
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private ListAdapter mAdapter;
    private ImageFetcher mImageFetcher;

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

    private List<Entry> mGridData = null;

    ImageButton[] imageButton = new ImageButton[5];
    TextView textview;

    GoogleMapFragment mParent = null;
    /**
     * Empty constructor as per the Fragment documentation
     */

    public TourListFragment() {
        mParent = null;
    }
    public void setParentFragment(GoogleMapFragment parentFragment) {
        mParent = parentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
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
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.list_view_layout, container, false);
        final RecyclerView mListView = (RecyclerView) v.findViewById(R.id.list_info);
        mListView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(llm);

        mAdapter = new ListAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        addListenerOnButton(v);
        return v;
    }

    public void addListenerOnButton(View v) {


        imageButton[0] = (ImageButton) v.findViewById(R.id.foodButton);
        imageButton[1] = (ImageButton) v.findViewById(R.id.hotelButton);
        imageButton[2] = (ImageButton) v.findViewById(R.id.shoppingButton);
        imageButton[3] = (ImageButton) v.findViewById(R.id.cultureButton);
        imageButton[4] = (ImageButton) v.findViewById(R.id.concertButton);

        for(int i=0 ; i<5 ; i++) {
            imageButton[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    String str = "empty";
                    if(arg0.getId() == R.id.foodButton) {
                        contentID = "39";
                    }
                    else if(arg0.getId() == R.id.hotelButton) {
                        contentID = "32";
                    }
                    else if(arg0.getId() == R.id.shoppingButton) {
                        contentID = "38";
                    }
                    else if(arg0.getId() == R.id.cultureButton) {
                        contentID = "14";
                    }
                    else if(arg0.getId() == R.id.concertButton) {
                        contentID = "15";
                    }

                    loadPage();
                }

            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN)
    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
//        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
//        if (Utils.hasJellyBean()) {
//            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
//            // show plus the thumbnail image in GridView is cropped. so using
//            // makeScaleUpAnimation() instead.
//            ActivityOptions options =
//                    ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
//            getActivity().startActivity(i, options.toBundle());
//        } else {
//            startActivity(i);
//        }
        startActivity(i);
    }

    // Checks the network connection and sets the wifiConnected and mobileConnected
    // variables accordingly.
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

    // Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    // This avoids UI lock up. To prevent network operations from
    // causing a delay that results in a poor user experience, always perform
    // network operations on a separate thread from the UI.
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
                mParent.changeGridDataSet(mGridData);
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


//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.main_menu, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.clear_cache:
//                mImageFetcher.clearCache();
//                Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
//                        Toast.LENGTH_SHORT).show();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ContactViewHolder> {
        private final Context mContext;
        private LayoutInflater mLayoutInflater;

        public ListAdapter(Context context) {
            mContext = context;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getItemCount() {
            if(mGridData != null)
                return mGridData.size();
            else
                return 0;
        }

        @Override
        public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
            Entry entry = mGridData.get(i);

            if(mGridData != null && i < getItemCount()) {
                String image_url = mGridData.get(i).firstimage;

                if (image_url != null)
                    mImageFetcher.loadImage(image_url, contactViewHolder.imageView);
            }
            contactViewHolder.titleView.setText(entry.title);
            contactViewHolder.addrView.setText(entry.addr1);
            contactViewHolder.distView.setText("거리: " + Double.toString(entry.dist));
//            Random rand = new Random();
//            int randomNum = rand.nextInt((5 - 1) + 1) + 1;
//
//            if(randomNum == 2) {
//                contactViewHolder.starImageView.setImageResource(R.drawable.star2);
//            } else if(randomNum == 3) {
//                contactViewHolder.starImageView.setImageResource(R.drawable.star3);
//            } else if(randomNum == 4) {
//                contactViewHolder.starImageView.setImageResource(R.drawable.star4);
//            } else if(randomNum == 5) {
//                contactViewHolder.starImageView.setImageResource(R.drawable.star5);
//            } else {
//                contactViewHolder.starImageView.setImageResource(R.drawable.star1);
//            }
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//            View itemView = LayoutInflater.
//                    from(viewGroup.getContext()).
//                    inflate(R.layout.cardview_item, viewGroup, false);
            View itemView = mLayoutInflater.inflate(R.layout.list_view_item, viewGroup, false);

            return new ContactViewHolder(itemView);
        }

        public class ContactViewHolder extends RecyclerView.ViewHolder {
            LinearLayout itemView = null;
            ImageView imageView = null;
            TextView titleView = null;
            TextView addrView = null;
            TextView distView = null;
            ImageView starImageView = null;

            public ContactViewHolder(View v) {
                super(v);
                imageView = (ImageView)v.findViewById(R.id.list_image);
                titleView = (TextView)v.findViewById(R.id.list_title);
                addrView = (TextView)v.findViewById(R.id.list_addrView);
                distView = (TextView)v.findViewById(R.id.list_dist);
                //starImageView = (ImageView)v.findViewById(R.id.list_star);
            }
        }

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
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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
            ImageView starImageView = null;

            if (convertView == null) {
                itemView = (LinearLayout) mLayoutInflater.inflate(
                        R.layout.list_view_item, container, false);
            } else {
                itemView = (LinearLayout)convertView;
            }

            imageView = (ImageView)itemView.findViewById(R.id.list_image);
            titleView = (TextView)itemView.findViewById(R.id.list_title);
            //starImageView = (ImageView)itemView.findViewById(R.id.list_star);
            //overviewView = (TextView)itemView.findViewById(R.id.list_overview);


            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setLayoutParams(mImageViewLayoutParams);

            if(mGridData != null && position < getCount()) {
                String image_url = mGridData.get(position).firstimage;

                if (image_url != null)
                    mImageFetcher.loadImage(image_url, imageView);
            }

            titleView.setText(mGridData.get(position).title);
            // overviewView.setText(mGridData.get(position).overview);

            Random rand = new Random();
            int randomNum = rand.nextInt((5 - 1) + 1) + 1;

            if(randomNum == 2) {
                starImageView.setImageResource(R.drawable.star2);
            } else if(randomNum == 3) {
                starImageView.setImageResource(R.drawable.star3);
            } else if(randomNum == 4) {
                starImageView.setImageResource(R.drawable.star4);
            } else if(randomNum == 5) {
                starImageView.setImageResource(R.drawable.star5);
            } else {
                starImageView.setImageResource(R.drawable.star1);
            }

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
                    new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
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
