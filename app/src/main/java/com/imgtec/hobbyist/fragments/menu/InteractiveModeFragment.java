/*
 * <b>Copyright 2014 by Imagination Technologies Limited
 * and/or its affiliated group companies.</b>\n
 * All rights reserved.  No part of this software, either
 * material or conceptual may be copied or distributed,
 * transmitted, transcribed, stored in a retrieval system
 * or translated into any human or computer language in any
 * form by any means, electronic, mechanical, manual or
 * other-wise, or disclosed to the third parties without the
 * express written permission of Imagination Technologies
 * Limited, Home Park Estate, Kings Langley, Hertfordshire,
 * WD4 8LZ, U.K.
 */

package com.imgtec.hobbyist.fragments.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.imgtec.flow.MessagingEvent;
import com.imgtec.flow.client.core.Core;
import com.imgtec.flow.client.core.FlowException;
import com.imgtec.flow.client.users.DataStore;
import com.imgtec.flow.client.users.DataStoreItem;
import com.imgtec.flow.client.users.DataStoreItems;
import com.imgtec.flow.client.users.Device;
import com.imgtec.flow.client.users.User;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.flow.AsyncMessage;
import com.imgtec.hobbyist.flow.AsyncMessageListener;
import com.imgtec.hobbyist.flow.Command;
import com.imgtec.hobbyist.flow.DevicePresenceListener;
import com.imgtec.hobbyist.flow.FlowEntities;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.flow.GPSReading;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.BroadcastReceiverWithRegistrationState;
import com.imgtec.hobbyist.utils.DebugLogger;
import com.imgtec.hobbyist.utils.ErrorHtmlLogger;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fragment used to interact with Board through Flow.
 * There are two modes: Commands and Messages.
 * Commands mode allows interacting with board by command messages.
 * Messages mode allows sending text messages to other users.
 *
 * NOTE: Messages feature is currently not available in the app. Logic is here, but ui is hidden.
 */
public class InteractiveModeFragment extends NDListeningFragment implements
        BackgroundExecutor.Callbacks<List<GPSReading>>, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,  LocationListener, AsyncMessageListener,
        DevicePresenceListener {

    public static final String TAG = "InteractiveModeFragment";

    private ConnectivityReceiver connectionReceiver;
    private TextView deviceName;

    private FlowHelper flowHelper;
    private Handler handler = new Handler();

    private static final int INITIAL_LOCATIONS_LOAD = 0;

    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final float MY_LOCATION_COLOR = BitmapDescriptorFactory.HUE_AZURE;
    private static final long MILLIS_AGO_HUE_CEIL = 7 * 24 * 60 * 60 * 1000; // 7 days


    private GoogleMap googleMap;
    private LocationClient locationClient;
    private LocationRequest locationRequest;

    private MarkerOptions phoneLocation;
    private TreeMap<Date, Marker> positionMarkers;
    private Polyline trackingLine;
    
    private TextView sataliteCount;
    private ImageView gpsIcon;
    private View rootView;

    public static InteractiveModeFragment newInstance() {
        return new InteractiveModeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.frag_interactive_mode, container, false);

            deviceName = (TextView) rootView.findViewById(R.id.deviceName);
            sataliteCount = (TextView) rootView.findViewById(R.id.satelliteCount);
            gpsIcon = (ImageView) rootView.findViewById(R.id.satellite);

            googleMap = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.mapFragment)).getMap();
            if (googleMap == null) {
                Toast.makeText(getActivity().getApplicationContext(),
                        "Error creating map", Toast.LENGTH_SHORT).show();
            }

            positionMarkers = new TreeMap<>();
            trackingLine = googleMap.addPolyline(new PolylineOptions()
                            .color(Color.DKGRAY)
                            .width(2)
            );

     /*   ((Button)rootView.findViewById(R.id.playHistory)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

            locationClient = new LocationClient(getActivity(), this, this);
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(UPDATE_INTERVAL_IN_SECONDS * 1000);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.onSelectionAndTitleChange(NDMenuItem.InteractiveMode);
        initFlowHelper();
        initUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        flowHelper.addDevicePresenceListener(this);
        connectionReceiver = new ConnectivityReceiver(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        connectionReceiver.register(appContext);
    }

    @Override
    public void onPause() {
        flowHelper.removeDevicePresenceListener(this);
        connectionReceiver.unregister(appContext);
        super.onPause();
    }

    /**
     * ---------------------------------- Initialization functions -----------------------------------*
     */

    private void initFlowHelper() {
        flowHelper = FlowHelper.getInstance(getActivity());
        flowHelper.setAsyncMessageListener(this);
    }

    public void initUI() {
        clearUI();
        setDeviceName();
    }

    private void setDeviceName() {
        deviceName.setText(flowHelper.getCurrentDevice().getName());
    }

    private void clearUI() {
    }

    @Override
    public void onStart() {
        super.onStart();
        locationClient.connect();
        fetchLocations();
    }

    @Override
    public void onStop() {
        locationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    /**
     * ------------------------------------------------- Map UI --------------------------------------------------*
     */



    private void fetchLocations() {
        final Activity context = getActivity();
        BackgroundExecutor.submit(new ExternalCall<List<GPSReading>>() {
            @Override
            public List<GPSReading> submit() {
                try {
                    User user = Core.getDefaultClient().getLoggedInUser();
                    Device device = FlowEntities.getInstance(context).getCurrentDevice().getDevice();
                    DataStore gpsReadingDatastore = device.getDataStore("GPSReading");

                    DataStoreItems gpsReadingDatastoreItems = gpsReadingDatastore.getItems();
                    List<GPSReading> gpsReadings = new ArrayList<>(gpsReadingDatastoreItems.size());
                    for (DataStoreItem dataStoreItem : gpsReadingDatastoreItems) {
                        gpsReadings.add(new GPSReading(dataStoreItem.getContent()));
                    }

                    return gpsReadings;

                } catch (Exception exception) {
                    Log.e("MapActivity", exception.toString());
                    exception.printStackTrace();
                    return null;
                }
            }
        }, this, INITIAL_LOCATIONS_LOAD, context);
    }

    @Override
    public void onBackgroundExecutionResult(final List<GPSReading> gpsReadings, int taskCode) {
        if (gpsReadings != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateMap(gpsReadings);
                }
            });
        }
    }

    private void updateMap(List<GPSReading> gpsReadings) {
        if (googleMap != null) {

            // create new markers
            for (GPSReading gpsReading : gpsReadings) {
                Date date = gpsReading.getUTCDateTime();
                if (!positionMarkers.containsKey(date)) {
                    MarkerOptions markerOptions = (new MarkerOptions()
                            .position(new LatLng(
                                    gpsReading.getLat(),
                                    gpsReading.getLng()))
                            .title(date.toString())
                            .draggable(false));
                    positionMarkers.put(date, googleMap.addMarker(markerOptions));
                }
            }

            ArrayList<LatLng> points = new ArrayList<>();

            if (!positionMarkers.isEmpty()) {
                // update the hues of each of the markers to show age
                Date oldestDate = positionMarkers.firstKey();
                Date newestDate = positionMarkers.lastKey();//new Date();
                for (Map.Entry<Date, Marker> kv : positionMarkers.entrySet()) {
                    // iterate the markers in order
                    Date date = kv.getKey();
                    Marker marker = kv.getValue();
                    marker.setIcon(
                            BitmapDescriptorFactory.defaultMarker(
                                    getHueFromTimestamp(date, newestDate, oldestDate)
                            )
                    );
                    points.add(marker.getPosition());
                }
            }
            trackingLine.setPoints(points);
        }
    }

    /** Interpolate the time difference between the timestamp and now to a hue going from red to
     * green as the timestamp becomes newer
     *
     * @return the hue value
     */
    private float getHueFromTimestamp(Date utcDateTime, Date newestDate, Date oldestDate) {
        long millisAgo = newestDate.getTime() - utcDateTime.getTime();
        long maxMillisAgo = Math.min(MILLIS_AGO_HUE_CEIL, newestDate.getTime() - oldestDate.getTime());
        float lerp = Math.min(1, (float)millisAgo / (float)maxMillisAgo);
        return (1-lerp) * 100;
    }

    @Override
    public void onLocationChanged(final Location location) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setMyLocation(location);
            }
        });
    }

    private void setMyLocation(Location myLocation) {
        if (googleMap != null) {
            LatLng latlng = new LatLng(
                    myLocation.getLatitude(),
                    myLocation.getLongitude()
            );
            if (phoneLocation == null) {
                phoneLocation = new MarkerOptions()
                        .position(latlng)
                        .title("My Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(MY_LOCATION_COLOR))
                        .draggable(false);
                googleMap.addMarker(phoneLocation);
            } else {
                phoneLocation.position(latlng);
            }
        }
    }



    /**---------------------------------- Start of command message functions -----------------------------------**/

    /**
     * Send AsyncMessage command to Flow.
     */
    private void sendCommandMessage(final String input) {
        BackgroundExecutor.execute(new ExternalRun() {
            @Override
            public void execute() {
                try {
                    String command = Command.prepareCommand(input);
                    AsyncMessage asyncMsg = flowHelper.createAsyncCommandMessage(command);
                    boolean isAsyncMessageSent = flowHelper.postAsyncMessage(asyncMsg);
                    if (!isAsyncMessageSent) {
                        ActivitiesAndFragmentsHelper.showToast(appContext, R.string.message_send_other_problem, handler);
                    }
                } catch (FlowException e) {
                    DebugLogger.log(getClass().getSimpleName(), e);
                    ActivitiesAndFragmentsHelper.showToast(appContext,
                            ErrorHtmlLogger.log(FlowEntities.getInstance(appContext).getLastError()),
                            handler);
                }
            }
        });
    }

    /**
     * Shows Flow's response to sent async command.
     */
    @Override
    public void onAsyncMessageResponse(MessagingEvent.AsyncMessageResponse response) {
        String responseText;
        if (getActivity() != null) {
            switch (response) {
                case SEND_SUCCESS:
                    responseText = getString(R.string.response_success);
                    break;
                case SEND_BUFFER_FULL:
                    responseText = getString(R.string.response_buffer_full);
                    break;
                case SENT_BUT_NOT_DELIVERED:
                    responseText = getString(R.string.response_offline);
                    break;
                case SEND_FAILED:
                    responseText = getString(R.string.response_failed);
                    break;
                default:
                    responseText = "";
                    break;
            }
            // TODO
        }
    }

    @Override
    public void onTextMessageReceived(AsyncMessage msg) {

    }

    @Override
    public void onCommandMessageReceived(AsyncMessage msg) {

    }

    /**
     * Asynchronously shows board's response to command.
     * If command was REBOOT or REBOOT SOFTAP, it will also show a dialog
     * forcing user to go back to connected devices screen.
     *
     * @param msg AsyncMessage object which can be parsed to get additional data.
     */
    @Override
    public void onCommandRXMessageReceived(final AsyncMessage msg) {
        //showCommandRXMessage(msg);
        handler.post(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    /**
     * --------------------------------- Dialog functions ----------------------------------------*
     */

    @Override
    public void onDevicePresenceChangeListener(boolean isConnected) {
        if (!isConnected) {
            showConnectedDevicesFragmentDialog(SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
    }

    private void showConnectedDevicesFragmentDialog(Fragment fragment) {
        ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
                R.string.device_is_offline,
                R.string.back_to_connected_devices,
                (FlowActivity) activity,
                fragment);
    }

    public class ConnectivityReceiver extends BroadcastReceiverWithRegistrationState {

        public ConnectivityReceiver(IntentFilter intentFilter) {
            super(intentFilter);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            connectionReceiver.unregister(appContext);
            if (!new WifiUtil(appContext).isInternetConnected()) {
                ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
                        R.string.connectivity_problems,
                        R.string.back_to_connected_devices,
                        (FlowActivity) activity,
                        SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
            }
        }
    }
}
