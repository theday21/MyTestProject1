package com.starup.traven.travelkorea.XMLParser;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by moco-lab on 2015-09-05.
 */
public class VisitKoreaXmlParser {
    private static final String ns = null;
    private static final String Tag = "Debug_Parser";

    // We don't use namespaces
    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readResponse(parser);
        } finally {
            in.close();
        }
    }

    private List<Entry> readResponse(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();
        XMLHeader xmlHeader = null;

        parser.require(XmlPullParser.START_TAG, ns, "response");
        parser.next();

        String name = parser.getName();
        // Starts by looking for the entry tag
        if (name.equals("header")) {
            xmlHeader = readHeader(parser);
        }

        // Received properly the result xml from www.visitkorea.or.kr
        if(xmlHeader.resultMsg.equals("OK")) {
            ;
        } else {
            return null;
        }

        Log.i(Tag, parser.getName());   // resultMsg
        parser.nextTag();
        Log.i(Tag, parser.getName());   // </header>
        parser.nextTag();
        Log.i(Tag, parser.getName());   // <body>
        parser.nextTag();
        Log.i(Tag, parser.getName());   // <items>

        // item list
        parser.require(XmlPullParser.START_TAG, ns, "items");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            Log.i(Tag, "Start read item");   // <items>
            name = parser.getName();
            if(name.equals("item")) {
                entries.add(readItem(parser));
            }
        }

        return entries;
    }

    public static class XMLHeader {
        public final String resultCode;
        public final String resultMsg;

        private XMLHeader(String code, String msg) {
            this.resultCode = code;
            this.resultMsg = msg;
        }
    }

    private XMLHeader readHeader(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "header");
        String code = null;
        String msg = null;

        parser.next();
        String name = parser.getName();
        if (name.equals("resultCode")) {
            parser.require(XmlPullParser.START_TAG, ns, "resultCode");
            code = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "resultCode");
        }
        else {
            throw new XmlPullParserException("resultCode");
        }

        parser.next();
        name = parser.getName();
        if (name.equals("resultMsg")) {
            parser.require(XmlPullParser.START_TAG, ns, "resultMsg");
            msg = readText(parser);
            parser.require(XmlPullParser.END_TAG, ns, "resultMsg");
        }
        else {
            throw new XmlPullParserException("resultMsg");
        }
        /*else {
            skip(parser);
        }*/

        return new XMLHeader(code, msg);
    }

    // This class represents a single entry (post) in the XML feed.
    // It includes the data members "title," "link," and "summary."
    public static class Entry {
        public final String title;
        public final String addr1;
        public final String firstimage;
        public final String overview;
        public String phone = null;
        public double mapx = 0;
        public double mapy = 0;
        public double dist = 0;

        public final String contenttypeId;
        public final String contentid;


        public Entry(String title, String addr1, String firstimage, String overview,
                      double mapx, double mapy, double dist, String contenttypeId, String contentid) {
            this.title = title;
            this.addr1 = addr1;
            this.firstimage = firstimage;
            this.overview = overview;
            this.mapx = mapx;
            this.mapy = mapy;
            this.dist = dist;

            this.contenttypeId = contenttypeId;
            this.contentid = contentid;
        }
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Entry readItem(XmlPullParser parser) throws XmlPullParserException, IOException {
        Log.i(Tag, "In readItem");
        parser.require(XmlPullParser.START_TAG, ns, "item");

        String title = null;
        String addr1 = null;
        String firstImage = null;
        String overview = null;

        String contentTypeId = null;
        String contentId = null;

        String tell = null;

        double mapx = -1;
        double mapy = -1;
        double dist = -1;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("title")) {
                String temp = readTitle(parser);
                String[] temp2 = temp.split("\\(");
                title = temp2[0];
            } else if (name.equals("addr1")) {
                addr1 = readAddr1(parser);
            } else if (name.equals("firstimage")) {
                firstImage = readLink(parser);
            } else if (name.equals("overview")) {
                overview = readOverview(parser);
            } else if (name.equals("mapx")) {
                String temp = readText(parser);
                mapx = Double.parseDouble(temp);
            } else if (name.equals("mapy")) {
                String temp = readText(parser);
                mapy = Double.parseDouble(temp);
            } else if (name.equals("dist")) {
                String temp = readText(parser);
                dist = Double.parseDouble(temp);
            } else if (name.equals("contenttypeid")) {
                contentTypeId = readText(parser);
            } else if (name.equals("contentid")) {
                contentId = readText(parser);
            } else if (name.equals("tel")) {
                tell = readText(parser);
            }  else {
                skip(parser);
            }
        }

        Log.i(Tag, "out readItem");
        Entry entry = new Entry(title, addr1, firstImage, overview, mapx, mapy, dist, contentTypeId, contentId);
        entry.phone = tell;

        return entry;
    }

    // Processes title tags in the feed.
    private String readTitle(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "title");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "title");
        return title;
    }

    // Processes link tags in the feed.
    private String readLink(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "firstimage");
        String addr = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "firstimage");
        return addr;
    }

    // Processes link tags in the feed.
    private String readOverview(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "overview");
        String overview = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "overview");
        return overview;
    }

    // Processes summary tags in the feed.
    private String readAddr1(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "addr1");
        String summary = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "addr1");
        return summary;
    }

    // For the tags title and summary, extracts their text values.
    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}