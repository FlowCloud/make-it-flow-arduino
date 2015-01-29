package com.imgtec.hobbyist.utils;

import android.graphics.Point;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by simon.pinfold on 23/01/2015.
 */
public class CatmullRomSpline implements  Iterable<LatLng>{

    private final LatLng[] controlPoints;
    private final float divisions;
    private final int size;

    public CatmullRomSpline(List<LatLng> points, float divisions){
        this.divisions = divisions;
        this.size = points.size();

        controlPoints = new LatLng[points.size()];
        int i = 0;
        for (LatLng p : points){
            controlPoints[i++] = p;
        }
    }

    @Override
    public Iterator<LatLng> iterator() {
        return new Iterator<LatLng>() {

            double position = 0;

            @Override
            public boolean hasNext() {
                return position < size - 1;
            }

            @Override
            public LatLng next() {
                int index = (int)(position);
                double t = position-index;

                position += 1./divisions;

                return solve(index, t);
            }

            public void remove() {}
        };
    }

    private LatLng solve(int index, double t) {
        LatLng v0;
        LatLng v1 = controlPoints[index+0];
        LatLng v2 = controlPoints[index+1];
        LatLng v3;

        if (index == 0){
            v0 = new LatLng(
                    v1.latitude + (v1.latitude - v2.latitude),
                    v1.longitude + (v1.longitude - v2.longitude)
            );
        } else {
            v0 = controlPoints[index-1];
        }

        if (index == controlPoints.length-2){
            v3 = new LatLng(
                    v2.latitude + (v2.latitude - v1.latitude),
                    v2.longitude + (v2.longitude - v1.longitude)
            );
        } else {
            v3 = controlPoints[index+2];
        }

        double t2 = t*t;
        double t3 = t2*t;

        double tp0 = 0.5*(v2.latitude-v0.latitude);
        double tp1 = 0.5*(v3.latitude-v1.latitude);

        double x = v1.latitude + tp0*t + (-3*v1.latitude + 3*v2.latitude - 2*tp0 - tp1)*t2 + (2*v1.latitude - 2*v2.latitude + tp0 + tp1)*t3;

        tp0 = 0.5*(v2.longitude-v0.longitude);
        tp1 = 0.5*(v3.longitude-v1.longitude);
        double y = v1.longitude + tp0*t + (-3*v1.longitude + 3*v2.longitude - 2*tp0 - tp1)*t2 + (2*v1.longitude - 2*v2.longitude + tp0 + tp1)*t3;

        return new LatLng(x, y);
    }
}
