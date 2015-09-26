package com.starup.traven.travelkorea;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.starup.traven.travelkorea.R;


public class ImageDetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        TextView tv = (TextView)findViewById(R.id.detail_overview);

        String temp = "고궁 명동점은 1999년 7월 개점하여 전주 본점의 향토색을 지켜나가면서 또한 명동의 특성상 외국인 손님들도 많이 찾고 있으므로 외국인의 입맛을 고려, 맛과 분위기를 조성중에 있다.  전주에 직접 가지 않고도 서울 중심가에서 전주의 맛을 느낄 수 있다는 점이 명동점의 자랑이다. " +
                "메뉴로는 전주 전통비빔밥 외 6가지 비빔밥과 고궁 명동점의 모든 음식을 맛 볼 수 있는 <골동반 정식>이 있다. " +
                "<골동반 정식>이란 평양의 냉면, 개성의 탕반과 더불어 조선 3대 음식중의 하나인 비빔밥 중 특히 궁중의 임금님 수라에 오른 비빔밥인 골동반을 현대감각에 맞게 재현한 비빔밥이다.";

        tv.setText(temp);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_actionbar);
        Drawable d = new BitmapDrawable(getResources(), bitmap);
        bar.setBackgroundDrawable(d);   //new ColorDrawable(Color.CYAN));

        bar.setDisplayHomeAsUpEnabled(false);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
