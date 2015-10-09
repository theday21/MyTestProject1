package com.starup.traven.travelkorea;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.starup.traven.travelkorea.common.logger.Log;
import com.starup.traven.travelkorea.util.ImageCache;
import com.starup.traven.travelkorea.util.ImageFetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by moco-lab on 2015-09-12.
 */
public class ScheduleFragment extends Fragment {

    private int mLanguage;
    private String langService = "EngService";
    private String contentID = "82";

    private RecyclerView recList;
    private LayoutInflater mLayoutInflater;
    ArrayList<TourItem> items;
    ArrayList<Bitmap> bitmaps;

    private ImageFetcher mImageFetcher;
    private static final String IMAGE_CACHE_DIR = "schedules";

    private ViewGroup rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        items = new ArrayList<>();
        bitmaps = new ArrayList<>();

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        mImageFetcher = new ImageFetcher(getActivity(), getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size2));
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.

        rootView = (ViewGroup) inflater
                .inflate(R.layout.schedule_fragment, container, false);

        // Set the title view to show the page number.
        recList = ((RecyclerView) rootView.findViewById(R.id.cardList));
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        items.clear();
        bitmaps.clear();
        readFiles();

        List<ScheduleEntry> contactList = new ArrayList<>();

        for(int i=0 ; i<items.size() ; ++i)
        {
            ScheduleEntry temp = new ScheduleEntry();
            temp.imgId = R.drawable.gn;
            temp.title = items.get(i).title;
            temp.addr =  items.get(i).addr;
            temp.phone = items.get(i).phone;
            temp.contentTypeId = items.get(i).contenttypeId;
            temp.contentId = items.get(i).contentid;
            temp.date = items.get(i).date;

            temp.bitmap = bitmaps.get(i);

            contactList.add(temp);

            //mImageFetcher.loadImage(image_url, imageView);
        }

        Collections.sort(contactList, new Comparator<ScheduleEntry>() {
            @Override
            public int compare(ScheduleEntry lhs, ScheduleEntry rhs) {
                return lhs.date < rhs.date ? 0 : 1;
            }
        });

        ContactAdapter ca = new ContactAdapter(contactList);
        recList.setAdapter(ca);

        if(contactList.size() == 0)
            rootView.setBackground(getResources().getDrawable(R.drawable.schedule_bg));
    }

    public class ScheduleEntry {
        public Long date = null;
        public String title = null;
        public String Overview = null;
        public String imgLink = null;
        public String addr = null;
        public String phone = null;
        public int imgId = -1;
        public Bitmap bitmap = null;

        public String contentId = null;
        public String contentTypeId = null;
    }

    public void readFiles() {
        items.clear();

        String path = Environment.getExternalStorageDirectory().toString() + "/TravelKorea";
        File f = new File(path);
        if(f != null) {
            File file[] = f.listFiles();
            if(file != null) {
                for (int i = 0; i < file.length; i++) {
                    String filename = file[i].getName();

                    String[] separated = filename.split("\\.");
                    if(separated[1].equals("dat")) {

                        String filePath = path + "/" + filename;

                        Bitmap image = null;
                        try {
                            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));
                            TourItem item = (TourItem)ois.readObject();
                            items.add(item);

                            int width = ois.readInt();
                            int height = ois.readInt();

                            TourItemBitmap bitmapDataObject = (TourItemBitmap)ois.readObject();
                            image = BitmapFactory.decodeByteArray(bitmapDataObject.imageByteArray, 0, bitmapDataObject.imageByteArray.length);

                            bitmaps.add(image);
                            ois.close();

                        } catch(Exception e) {
                        }
                        finally {
                            if(image == null) {
                                image = BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
                                bitmaps.add(image);
                            }
                        }
                    }
                }
            }
        }
    }



    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

        private List<ScheduleEntry> contactList;

        public ContactAdapter(List<ScheduleEntry> contactList) {
            this.contactList = contactList;
        }

        @Override
        public int getItemCount() {
            return contactList.size();
        }

        @Override
        public void onBindViewHolder(ContactViewHolder contactViewHolder, int i) {
            ScheduleEntry ci = contactList.get(i);
            contactViewHolder.vTitle.setText(ci.title);
            contactViewHolder.vaddr.setText(ci.addr);
//            contactViewHolder.vphone.setText(ci.phone);
//            contactViewHolder.imageView.setImageResource(ci.imgId);
            contactViewHolder.imageView.setImageBitmap(ci.bitmap);
            contactViewHolder.mainView.setId(Integer.parseInt(items.get(i).contentid));


            contactViewHolder.mainView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        String contentId = Integer.toString(v.getId());

                        String typeId = null;
                        for(int i=0 ; i<contactList.size() ; ++i) {
                            if(contactList.get(i).contentId.equals(contentId)){
                                typeId = contactList.get(i).contentTypeId;
                            }
                        }


                        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
                        i.putExtra("langService", langService);
                        i.putExtra("contenttypeId", typeId);
                        i.putExtra("contentid", contentId);
                        startActivity(i);
                    }

                    return true;
                }
            });



            try {

                FloatingActionButton fab = (FloatingActionButton) contactViewHolder.mainView.findViewById(R.id.schedule_cancel_button);
                fab.setId(Integer.parseInt(ci.contentId)); //items.get(i).contentid));
                fab.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            String contentId = Integer.toString(v.getId());
                            String filename = contentId + ".dat";
                            String filePath = Environment.getExternalStorageDirectory().toString() + "/TravelKorea/" + filename;

                            File file = new File(filePath);

                            if (file.delete()) {
                                Context context = getActivity();
                                CharSequence text = "Tour information deleted.";
                                int duration = Toast.LENGTH_SHORT;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                                Iterator iterator = items.iterator();
                                Iterator iter = bitmaps.iterator();

                                int index = -1;
                                while (iterator.hasNext()) {

                                    ++index;
                                    TourItem item = (TourItem) iterator.next();
                                    iter.hasNext();
                                    iter.next();

                                    if (item.contentid.equals(contentId)) {
                                        iterator.remove();
                                        iter.remove();
                                        break;
                                    }
                                }

                                if (index > -1 && index < items.size() + 1) {
                                    contactList.remove(index);
                                    notifyItemRemoved(index);
                                    //notifyItemRangeChanged(index, items.size()-index);

                                }
                            }
                        }
                        return true;

                    }
                });
            } catch (Exception e) {
            }
        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
//            View itemView = LayoutInflater.
//                    from(viewGroup.getContext()).
//                    inflate(R.layout.cardview_item, viewGroup, false);
            View itemView = mLayoutInflater.inflate(R.layout.cardview_item, viewGroup, false);

            return new ContactViewHolder(itemView);
        }

        public class ContactViewHolder extends RecyclerView.ViewHolder {
            public View mainView = null;
            protected TextView vTitle;
            protected TextView vaddr;
            protected TextView vphone;
            protected ImageView imageView;

            public ContactViewHolder(View v) {
                super(v);

                mainView = v;
                vTitle =  (TextView) v.findViewById(R.id.title);
                vaddr =  (TextView) v.findViewById(R.id.address);
//                vphone =  (TextView) v.findViewById(R.id.schedule_phone);
                imageView =  (ImageView) v.findViewById(R.id.schedule_img);
            }
        }

    }


}
