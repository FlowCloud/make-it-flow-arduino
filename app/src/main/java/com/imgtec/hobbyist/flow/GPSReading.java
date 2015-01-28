package com.imgtec.hobbyist.flow;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

/**
 * Created by simon.pinfold on 9/12/2014.
 */
public class GPSReading {

    // no namespace
    private static final String ns = null;
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    private double lat;
    private double lng;
    private Date dateTime;


    public GPSReading(String xml) throws XmlPullParserException, IOException, ParseException {
        final XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        parser.setInput(new StringReader(xml));
        parser.nextTag();

        parser.require(XmlPullParser.START_TAG, ns, "GPSReading");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("GPSReadingTime")) {
                readDateTime(parser);
            } else if (name.equals("Location")) {
                readLocation(parser);
            }
        }

    }

    private void readDateTime(XmlPullParser parser) throws IOException, XmlPullParserException, ParseException {
        parser.require(XmlPullParser.START_TAG, ns, "GPSReadingTime");
        String dateTimeString = readText(parser);
        this.dateTime = dateFormatter.parse(dateTimeString);
        parser.require(XmlPullParser.END_TAG, ns, "GPSReadingTime");
    }

    private void readLocation(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "Location");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("Latitude")){
                readLatitude(parser);
            } else if (name.equals("Longitude")){
                readLongitude(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "Location");
    }

    private void readLatitude(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Latitude");
        lat = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "Latitude");
    }

    private void readLongitude(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "Longitude");
        lng = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "Longitude");
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.next();
        String result = parser.getText();
        parser.nextTag();
        return result;
    }


    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public Date getUTCDateTime() {
        return dateTime;
    }
}
