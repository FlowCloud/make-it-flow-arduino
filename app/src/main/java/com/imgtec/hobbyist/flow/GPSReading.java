package com.imgtec.hobbyist.flow;

import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

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
public class GPSReading extends LocationReading{


    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());

    private double lat;
    private double lng;
    private Date dateTime;
    private double hdop;
    private double course;
    private double speed;
    private int satellites;
    private double altitude;


    public GPSReading(String xml) throws XmlPullParserException, IOException, ParseException {
        final XmlPullParser parser = Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        parser.setInput(new StringReader(xml));
        parser.nextTag();

        parser.require(XmlPullParser.START_TAG, ns, "gpsreading");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("gpsreadingtime")) {
                this.dateTime = readDateTime(parser);
            } else if (name.equals("location")) {
                LatLng location = readLocation(parser);
                this.lat = location.latitude;
                this.lng = location.longitude;
            } else if (name.equals("satellites")){
                this.satellites = readSatellites(parser);
            } else if (name.equals("altitude")){
                this.altitude = readAltitude(parser);
            } else if (name.equals("speed")){
                this.speed = readSpeed(parser);
            } else if (name.equals("course")){
                this.course = readCourse(parser);
            } else if (name.equals("hdop")){
                this.hdop = readHDOP(parser);
            } else {
                // ignore
                readText(parser);
            }
        }

    }

    private double readHDOP(XmlPullParser parser) throws IOException, XmlPullParserException {
        double hdop;
        parser.require(XmlPullParser.START_TAG, ns, "hdop");
        hdop = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "hdop");
        return hdop;
    }

    private double readCourse(XmlPullParser parser) throws IOException, XmlPullParserException {
        double course;
        parser.require(XmlPullParser.START_TAG, ns, "course");
        course = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "course");
        return course;
    }

    private double readSpeed(XmlPullParser parser) throws IOException, XmlPullParserException {
        double speed;
        parser.require(XmlPullParser.START_TAG, ns, "speed");
        speed = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "speed");
        return speed;
    }

    private int readSatellites(XmlPullParser parser) throws IOException, XmlPullParserException {
        int satellites;
        parser.require(XmlPullParser.START_TAG, ns, "satellites");
        satellites = Integer.parseInt(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "satellites");
        return satellites;
    }

    private double readAltitude(XmlPullParser parser) throws IOException, XmlPullParserException {
        double altitude;
        parser.require(XmlPullParser.START_TAG, ns, "altitude");
        altitude = Double.parseDouble(readText(parser));
        parser.require(XmlPullParser.END_TAG, ns, "altitude");
        return altitude;
    }


    private Date readDateTime(XmlPullParser parser) throws IOException, XmlPullParserException, ParseException {
        Date dateTime;
        parser.require(XmlPullParser.START_TAG, ns, "gpsreadingtime");
        String dateTimeString = readText(parser);
        dateTime = dateFormatter.parse(dateTimeString);
        parser.require(XmlPullParser.END_TAG, ns, "gpsreadingtime");
        return dateTime;
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

    public double getAltitude() {
        return altitude;
    }

    public int getSatellites() {
        return satellites;
    }

    public double getSpeed() {
        return speed;
    }

    public double getCourse() {
        return course;
    }

    public double getHDOP() {
        return hdop;
    }
}
