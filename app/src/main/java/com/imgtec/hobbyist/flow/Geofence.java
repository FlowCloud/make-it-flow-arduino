package com.imgtec.hobbyist.flow;

import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * Created by simon.pinfold on 26/01/2015.
 */
public class Geofence extends LocationReading{


    private LatLng location;
    private double radius;

    public Geofence(String xml) throws XmlPullParserException, IOException {
        final XmlPullParser parser = Xml.newPullParser();

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);


        parser.setInput(new StringReader(xml));
        parser.nextTag();

        parser.require(XmlPullParser.START_TAG, ns, "geofence");
        LatLng location = null;
        double radius = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("location")) {
                this.location = readLocation(parser);
            } else if (name.equals("radius")) {
                this.radius = readRadius(parser);
            }
        }
    }

    private double readRadius(XmlPullParser parser) throws IOException, XmlPullParserException {
        double radius=0;
        parser.require(XmlPullParser.START_TAG, ns, "radius");
        radius = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "radius");
        return radius;
    }


    public LatLng getLocation() {
        return location;
    }

    public double getRadius() {
        return radius;
    }
}
