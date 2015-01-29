package com.imgtec.hobbyist.flow;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by simon.pinfold on 26/01/2015.
 */
public abstract class LocationReading {

    // no namespace
    protected static final String ns = null;

    protected LatLng readLocation(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "location");
        double lat = 0, lng = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("latitude")){
                lat = readLatitude(parser);
            } else if (name.equals("longitude")){
                lng = readLongitude(parser);
            }
        }
        parser.require(XmlPullParser.END_TAG, ns, "location");
        return new LatLng(lat, lng);
    }

    protected double readLatitude(XmlPullParser parser) throws XmlPullParserException, IOException {
        double lat;
        parser.require(XmlPullParser.START_TAG, ns, "latitude");
        lat = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "latitude");
        return lat;
    }

    protected double readLongitude(XmlPullParser parser) throws XmlPullParserException, IOException {
        double lng;
        parser.require(XmlPullParser.START_TAG, ns, "longitude");
        lng = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "longitude");
        return lng;
    }

    protected String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.next();
        String result = parser.getText();
        parser.nextTag();
        return result;
    }
}
