package com.starup.traven.travelkorea;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by moco-lab on 2015-09-27.
 */

public class TourItem  implements Serializable {

    public String langService = null;
    public String contenttypeId = null;
    public String contentid = null;
    public String writtenTime = null;

    public String title = null;
    public String phone = null;

    public String imgURL = null;
    public String addr = null;
    public Long date = null;

    public TourItem(String lang, String contype, String conId, String time, String img, String addr) {
        this.langService = lang;
        this.contenttypeId = contype;
        this.contentid = conId;
        this.writtenTime = time;
        this.imgURL = img;
        this.addr = addr;
    }
}
