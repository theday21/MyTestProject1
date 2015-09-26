package com.starup.traven.travelkorea;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by moco-lab on 2015-09-12.
 */
public class ScheduleFragment extends Fragment {

    private LayoutInflater mLayoutInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.

        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.schedule_fragment, container, false);

        // Set the title view to show the page number.
        RecyclerView recList =  ((RecyclerView) rootView.findViewById(R.id.cardList));
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        List<ScheduleEntry> contactList = new ArrayList<>();

        {
            ScheduleEntry temp = new ScheduleEntry();
            temp.imgId = R.drawable.gn;
            temp.title = "강남 페스티벌 2015";
            temp.addr = "서울특별시 강남구 영동대로 513 (삼성동)";
            temp.phone = "02-3423-5543";
            contactList.add(temp);
        }

        ContactAdapter ca = new ContactAdapter(contactList);
        recList.setAdapter(ca);

        return rootView;
    }

    public class ScheduleEntry {
        public String date = null;
        public String title = null;
        public String Overview = null;
        public String imgLink = null;
        public String addr = null;
        public String phone = null;
        public int imgId = -1;
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
            contactViewHolder.vphone.setText(ci.phone);
            contactViewHolder.imageView.setImageResource(ci.imgId);
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
            protected TextView vTitle;
            protected TextView vaddr;
            protected TextView vphone;
            protected ImageView imageView;

            public ContactViewHolder(View v) {
                super(v);
                vTitle =  (TextView) v.findViewById(R.id.title);
                vaddr =  (TextView) v.findViewById(R.id.address);
                vphone =  (TextView) v.findViewById(R.id.schedule_phone);
                imageView =  (ImageView) v.findViewById(R.id.schedule_img);
            }
        }

    }


}
