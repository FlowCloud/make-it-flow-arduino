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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.imgtec.flow.MessagingEvent;
import com.imgtec.flow.client.users.DataStore;
import com.imgtec.flow.client.users.DataStoreItem;
import com.imgtec.flow.client.users.DataStoreItems;
import com.imgtec.hobbyist.R;
import com.imgtec.hobbyist.activities.ActivitiesAndFragmentsHelper;
import com.imgtec.hobbyist.activities.FlowActivity;
import com.imgtec.hobbyist.flow.AlertListener;
import com.imgtec.hobbyist.flow.AsyncMessage;
import com.imgtec.hobbyist.flow.AsyncMessageListener;
import com.imgtec.hobbyist.flow.DevicePresenceListener;
import com.imgtec.hobbyist.flow.FlowHelper;
import com.imgtec.hobbyist.flow.GPSReading;
import com.imgtec.hobbyist.flow.Geofence;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDListeningFragment;
import com.imgtec.hobbyist.fragments.navigationdrawer.NDMenuItem;
import com.imgtec.hobbyist.utils.BackgroundExecutor;
import com.imgtec.hobbyist.utils.BroadcastReceiverWithRegistrationState;
import com.imgtec.hobbyist.utils.CatmullRomSpline;
import com.imgtec.hobbyist.utils.ExternalCall;
import com.imgtec.hobbyist.utils.ExternalRun;
import com.imgtec.hobbyist.utils.SimpleFragmentFactory;
import com.imgtec.hobbyist.utils.WifiUtil;
import com.google.android.gms.maps.model.Marker;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import at.markushi.ui.CircleButton;

public class InteractiveModeFragment extends NDListeningFragment implements
        BackgroundExecutor.Callbacks<List<GPSReading>>, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,  LocationListener, AsyncMessageListener,
        DevicePresenceListener, AlertListener {

    // **************** Constants  *********************
    public static final String TAG = "InteractiveModeFragment";
    private static final int INITIAL_LOCATIONS_LOAD_EXECUTOR_ID = 100;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final float MY_LOCATION_COLOR = BitmapDescriptorFactory.HUE_AZURE;
    private static final float WIFIRE_CURRENT_LOCATION = BitmapDescriptorFactory.HUE_VIOLET;
    private static final long MILLIS_AGO_HUE_CEIL = 1 * 24 * 60 * 60 * 1000; // 1 day
    private static final int REPLAY_SPEED = 50;
    private static final int REPLAY_PATH_SUBSTEPS = 50;

    // must be ordered
    private static final Object[][] periodSelection = new Object[][]{
            {"30 seconds",  "15 minutes",           30  *1000, 30},
            {"1 minute",    "30 minutes",           60  *1000, 30},
            {"2 minutes",   "1 hour",           2  *60  *1000, 30},
            {"5 minutes",   "2½ hours",         5  *60  *1000, 30},
            {"10 minutes",  "5 hours",          10 *60  *1000, 30},
            {"25 minutes",  "12 hours",         25 *60  *1000, 29},
            {"1 hour",      "24 hours",         60 *60  *1000, 24},
    };


    // **************** UI Components  *********************
    private ImageView gpsIcon;
    private TextView satelliteCount;
    private TextView deviceName;

    private TextView hdopTextView;
    private TextView locationTextView;
    private TextView speedTextView;
    private TextView altitudeTextView;

    private View rootView;
    private ProgressBar loadingInitialLocations;

    private ImageView targetImageView;
    private TableLayout wifireInformationView;
    private LinearLayout wifireButtonsView;

    private LinearLayout editGeofenceButtons;

    private CircleButton playLocationHistoryButton;
    private CircleButton inspectPointsButton;

    private Button doneInspectingPointsButton;

    private FrameLayout periodDialog;
    private TextView periodTextView;
    private TextView lengthTextView;
    private SeekBar periodSelectionBar;

  // **************** Map components *********************
    private GoogleMap googleMap;

    private Marker phoneLocation;
    private MarkerOptions phoneLocationOptions;

    private Marker wifireCurrentLocation;
    private MarkerOptions wifireCurrentLocationOptions;

    private List<LatLng> points;
    private TreeMap<Date, Marker> positionMarkers;
    private Polyline trackingLine;

    private Circle geofenceCircle;



    // **************** Activity fields  *********************
    private ConnectivityReceiver connectionReceiver;

    private FlowHelper flowHelper;
    private Handler handler = new Handler();

    private LocationClient locationClient;
    private LocationRequest locationRequest;

    private Object playLocationHistoryAnimationToken;
    private List<LatLng> partialSplinePoints;
    private boolean hasZoomedToMarkers;

    private Map<String, CommandResponseHandler> waitingCommands;

    private int saveReadingPeriod;
    private int maximumReadings;
    private Timer updateTimer;


  public static InteractiveModeFragment newInstance() {
        return new InteractiveModeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.frag_interactive_mode, container, false);
        waitingCommands = new HashMap<>();
        return rootView;
    }


    private String createGeofenceParameters(double lat, double lng, double radius) {
        return String.format("<geofence><location><latitude>%f</latitude><longitude>%f</longitude></location><radius>%f</radius></geofence>", lat, lng, radius);
    }

    @Override
    public void onDestroyView() {
        FragmentManager fm = getActivity().getSupportFragmentManager();

        // because we add a fragment in the XML and InteractiveModeFragment is recreated
        // each time we need to remove the map fragment
        Fragment mapFragment = fm.findFragmentById(R.id.mapFragment);
        try {
          if (mapFragment != null) {
            fm.beginTransaction().remove(mapFragment).commit();
          }
        } catch (IllegalStateException e)
        {}
        super.onDestroyView();
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

        //online = true;
        fetchWiFireLocation();
        sendCommand("GET GEOFENCE", null, new CommandResponseHandler() {
            @Override
            public void onCommandResponse(AsyncMessage response) {
                try {
                    final Geofence retrievedFence = new Geofence(response.getNode("responseparams"));

                    if (retrievedFence.getRadius() == 0){
                        showToast("No geofence set on WiFire");
                    } else {
                        showToast("Retrieved geofence from WiFire");
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (geofenceCircle != null) geofenceCircle.remove();
                            if (retrievedFence.getRadius() != 0){
                                geofenceCircle = googleMap.addCircle(new CircleOptions()
                                                .center(retrievedFence.getLocation())
                                                .radius(retrievedFence.getRadius())
                                                .strokeColor(Color.CYAN)
                                );
                            }
                        }
                    });
                } catch (XmlPullParserException | IOException e) {
                    showToast("Failed to fetch geofence from WiFire");
                    e.printStackTrace();
                }



            }
        });
        sendCommand("GET PERIOD", null, new CommandResponseHandler() {
          @Override
          public void onCommandResponse(AsyncMessage response) {
            int periodTagWidth = "<period>".length();
            String params = response.getNode("responseparams");
            String periodS = params.substring(periodTagWidth, params.length()-(periodTagWidth+1));
            int period = Integer.parseInt(periodS);

            final int index = Arrays.binarySearch(periodSelection, new Object[]{null, null, period, null}, new Comparator<Object[]>() {
                @Override
                public int compare(Object[] lhs, Object[] rhs) {
                  return (int)lhs[2] - (int)rhs[2];
                }
            });
            saveReadingPeriod = (int)periodSelection[index][2];
            maximumReadings = (int)periodSelection[index][3];

            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                if (getActivity() == null) return;
                periodSelectionBar.setProgress(index);
                periodTextView.setText((String) periodSelection[index][0]);
                lengthTextView.setText((String) periodSelection[index][1]);
              }
            });

          }
        });

        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            fetchWiFireLocation();
            fetchWiFireLocationHistory(0);
          }
        }, 0, 30000);
     }


    @Override
    public void onPause() {
        flowHelper.removeDevicePresenceListener(this);
        connectionReceiver.unregister(appContext);
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            fetchWiFireLocation();
            fetchWiFireLocationHistory(0);
          }
        }, 0, 30000);
        super.onPause();
    }

    /**
     * ---------------------------------- Initialization functions -----------------------------------*
     */

    private void initFlowHelper() {
        flowHelper = FlowHelper.getInstance(getActivity());
        flowHelper.setAsyncMessageListener(this);
        flowHelper.setAlertListener(this);
    }

    public void initUI() {
        clearUI();
        deviceName = (TextView) rootView.findViewById(R.id.deviceName);
        setDeviceName();

        gpsIcon = (ImageView) rootView.findViewById(R.id.satellite);
        satelliteCount = (TextView) rootView.findViewById(R.id.satelliteCount);

        targetImageView = (ImageView) rootView.findViewById(R.id.targetImageView);
        wifireInformationView = (TableLayout) rootView.findViewById(R.id.wifireInformationView);
        wifireButtonsView = (LinearLayout) rootView.findViewById(R.id.wifireButtonsView);
        loadingInitialLocations = (ProgressBar) rootView.findViewById(R.id.loadingInitialLocations);

        hdopTextView = (TextView) rootView.findViewById(R.id.hdopTextView);
        locationTextView = (TextView) rootView.findViewById(R.id.locationTextView);
        speedTextView = (TextView) rootView.findViewById(R.id.speedTextView);
        altitudeTextView = (TextView) rootView.findViewById(R.id.altitudeTextView);

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

        initialiseMainMenu();

        initialiseConfigureGeofenceButtons();

        initialiseConfigurePeriodDialog();

        locationClient = new LocationClient(getActivity(), this, this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_SECONDS * 1000);

    }

    private void initialiseConfigurePeriodDialog() {
        periodDialog = (FrameLayout) rootView.findViewById(R.id.periodDialog);

        this.periodTextView = ((TextView) rootView.findViewById(R.id.periodTextView));
        this.lengthTextView = ((TextView) rootView.findViewById(R.id.lengthTextView));
        this.periodSelectionBar = ((SeekBar) rootView.findViewById(R.id.periodSelectionBar));

        periodSelectionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                periodTextView.setText((String) periodSelection[progress][0]);
                lengthTextView.setText((String) periodSelection[progress][1]);
            }
            public void onStartTrackingTouch(SeekBar seekBar) {}
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        ((Button)rootView.findViewById(R.id.confirmPeriod)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLoggingPeriod(periodSelectionBar);
            }
        });

        //setLoggingPeriod(periodSelectionBar);
    }

    private void setLoggingPeriod(SeekBar periodSelectionBar) {
        periodDialog.setVisibility(View.INVISIBLE);

        final int index = periodSelectionBar.getProgress();
        saveReadingPeriod = (int) periodSelection[index][2];
        maximumReadings = (int) periodSelection[index][3];

        sendCommand("SET PERIOD", "<period>" + saveReadingPeriod + "</period>", new CommandResponseHandler() {
            @Override
            public void onCommandResponse(AsyncMessage response) {
                showToast("Update period set to " + periodSelection[index][0]);
            }
        });
    }

    private void showToast(final String s) {
        showToast(s, Toast.LENGTH_LONG);
    }

    private void showToast(final String s, final int length) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null) return;
                Toast.makeText(getActivity().getApplicationContext(), s, length).show();
            }
        });
    }


    private void initialiseMainMenu() {
        ((CircleButton)rootView.findViewById(R.id.setGeofenceButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetImageView.setVisibility(View.VISIBLE);
                editGeofenceButtons.setVisibility(View.VISIBLE);
                wifireInformationView.setVisibility(View.GONE);
                wifireButtonsView.setVisibility(View.GONE);
            }
        });

        ((CircleButton)rootView.findViewById(R.id.gotoLocationButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomToMarkers();
            }
        });

        ((CircleButton) rootView.findViewById(R.id.getLocationButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchWiFireLocation();
            }
        });

        playLocationHistoryButton = ((CircleButton)rootView.findViewById(R.id.playHistoryButton));
        playLocationHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playLocationHistory();
            }
        });

        ((CircleButton)rootView.findViewById(R.id.setPeriodButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                periodDialog.setVisibility(View.VISIBLE);
            }
        });



        doneInspectingPointsButton = ((Button)rootView.findViewById(R.id.doneInspectingPointsButton));
        doneInspectingPointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Marker mo : positionMarkers.values()){
                    mo.setVisible(false);
                }
                doneInspectingPointsButton.setVisibility(View.GONE);
                wifireButtonsView.setVisibility(View.VISIBLE);
            }
        });

        inspectPointsButton =  ((CircleButton) rootView.findViewById(R.id.inspectPointsButton));
        inspectPointsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                for (Marker mo : positionMarkers.values()){
                    mo.setVisible(true);
                }
                doneInspectingPointsButton.setVisibility(View.VISIBLE);
                wifireButtonsView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void initialiseConfigureGeofenceButtons() {
        editGeofenceButtons = (LinearLayout) rootView.findViewById(R.id.editGeofenceButtons);
        ((Button) rootView.findViewById(R.id.confirmGeofenceButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetImageView.setVisibility(View.INVISIBLE);
                editGeofenceButtons.setVisibility(View.INVISIBLE);
                wifireInformationView.setVisibility(View.VISIBLE);
                wifireButtonsView.setVisibility(View.VISIBLE);

                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;

                LatLng left = googleMap.getProjection().fromScreenLocation(new Point(
                        (int) targetImageView.getX(),
                        (int) targetImageView.getY() + targetImageView.getHeight() / 2
                ));
                final LatLng center = googleMap.getProjection().fromScreenLocation(new Point(
                        (int) targetImageView.getX() + targetImageView.getWidth() / 2,
                        (int) targetImageView.getY() + targetImageView.getHeight() / 2
                ));
                LatLng right = googleMap.getProjection().fromScreenLocation(new Point(
                        (int) targetImageView.getX() + targetImageView.getWidth(),
                        (int) targetImageView.getY() + targetImageView.getHeight() / 2
                ));

                Location loc1 = new Location(LocationManager.GPS_PROVIDER);
                Location loc2 = new Location(LocationManager.GPS_PROVIDER);

                loc1.setLatitude(left.latitude);
                loc1.setLongitude(left.longitude);

                loc2.setLatitude(right.latitude);
                loc2.setLongitude(right.longitude);

                final float radius = loc1.distanceTo(loc2) / 2;
                Log.v("promptForGeofence", "the radius is " + radius);

                if (geofenceCircle != null) {
                    geofenceCircle.remove();
                }
                geofenceCircle = googleMap.addCircle(new CircleOptions()
                                .center(center)
                                .radius(radius)
                                .strokeColor(Color.CYAN)
                );

                sendCommand("SET GEOFENCE", createGeofenceParameters(center.latitude, center.longitude, radius), new CommandResponseHandler() {

                    @Override
                    public void onCommandResponse(AsyncMessage response) {
                        showToast("Geofence registered on WiFire");
                    }

                });
            }
        });
        ((Button) rootView.findViewById(R.id.removeGeofenceButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetImageView.setVisibility(View.INVISIBLE);
                editGeofenceButtons.setVisibility(View.INVISIBLE);
                wifireInformationView.setVisibility(View.VISIBLE);
                wifireButtonsView.setVisibility(View.VISIBLE);

                if (geofenceCircle != null) {
                    geofenceCircle.remove();
                    geofenceCircle = null;

                    sendCommand("SET GEOFENCE", createGeofenceParameters(0, 0, 0), new CommandResponseHandler() {
                        @Override
                        public void onCommandResponse(AsyncMessage response) {
                            showToast("Geofence removed from WiFire");
                        }
                    });
                }
            }
        });
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
        fetchWiFireLocationHistory(INITIAL_LOCATIONS_LOAD_EXECUTOR_ID);
       /* updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
          @Override
          public void run() {
            fetchWiFireLocation();
            fetchWiFireLocationHistory(0);
          }
        }, 0, 30000);*/
        //fetchWiFireLocation();
    }

  @Override
  public void onAlertReceived(AsyncMessage asyncMsg) {
    if (asyncMsg.getNode("type").equals("GEOFENCE ESCAPED")) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          new AlertDialog.Builder(getActivity())
                  .setTitle("Escaped Geofence")
                  .setMessage("The GPS has been detected outside the geofence!")
                  .setNeutralButton("OK",
                    new DialogInterface.OnClickListener() {
                      public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                      }
                    })
                  .setIcon(android.R.drawable.ic_dialog_alert)
                  .show();
        }
      });
    }
    Log.e("onAlertReceived", asyncMsg.buildXml());
    Log.e("onAlertReceived", asyncMsg.getNode("type"));
  }

  private interface CommandResponseHandler {
        void onCommandResponse(AsyncMessage response);
    }
    private void fetchWiFireLocation() {
        if (getActivity() == null) return;
        BackgroundExecutor.execute(new ExternalRun() {
            @Override
            public void execute() {
                sendCommand("GET LOCATION", null, new CommandResponseHandler(){
                    @Override
                    public void onCommandResponse(AsyncMessage response) {
                        String code = response.getNode("responsecode");
                        String params = response.getNode("responseparams");

                        Log.v("onCommandResponse", "response->responseparams = \"" + params + "\"");
                        if (code.equals("OK")){
                            try {
                                GPSReading currentLocation = new GPSReading(params);

                                updateCurrentLocation(currentLocation);

                                showToast("Updated WiFire location", Toast.LENGTH_SHORT);

                            } catch (XmlPullParserException | IOException | ParseException e) {
                                Log.e("onCommandResponse", "Error parsing GPS reading", e);
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("onCommandResponse", "Got response code" + code);
                        }

                    }
                });

            }
        });
    }

    private void updateCurrentLocation(final GPSReading currentLocation) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                satelliteCount.setText("" + currentLocation.getSatellites());

                hdopTextView.setText(String.format("%3d", (int)currentLocation.getHDOP()));
                locationTextView.setText(String.format("%3.3f, %3.3f", currentLocation.getLat(), currentLocation.getLng()));
                speedTextView.setText(String.format("%2.2f m/s", currentLocation.getSpeed()));
                altitudeTextView.setText(String.format("%4.2f m", currentLocation.getAltitude()));

                LatLng latlng = new LatLng(currentLocation.getLat(), currentLocation.getLng());
                if (wifireCurrentLocation != null) {
                  wifireCurrentLocation.remove();
                }
                wifireCurrentLocationOptions = new MarkerOptions()
                        .position(latlng)
                        .title("WiFire Current Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(WIFIRE_CURRENT_LOCATION))
                        .draggable(false);
                wifireCurrentLocation = googleMap.addMarker(wifireCurrentLocationOptions);
                if (!hasZoomedToMarkers) zoomToMarkers();

            }
        });
    }


    private void sendCommand(final String command, final String commandParams, final CommandResponseHandler onResponse) {
        final InteractiveModeFragment fthis = this;
        BackgroundExecutor.execute(new ExternalRun() {
          @Override
          public void execute() {
            // createAsyncCommandMessage can create messages with duplicate request IDs
            // if the thread switches while between increasing the current ID and assigning
            // it to the new message
            synchronized (fthis) {

                    /*if (!online) {
                        Log.v("InteractiveModeFragment.sendCommand", "Not sending command to " + command + "- device appears to be offline");
                        return;
                    }*/

              AsyncMessage message = flowHelper.createAsyncCommandMessage(command);
              if (commandParams != null) message.addNode("commandparams", commandParams);

              flowHelper.postAsyncMessage(message);

              waitingCommands.put(message.getRequestId(), onResponse);
              Log.v("sendCommand", "Sent command \"" + command + "\". Request ID: " + message.getRequestId());
              Log.v("sendCommand", message.buildXml());


            }
          }
        });
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

    private void playLocationHistory() {
        playLocationHistoryButton.setEnabled(false);
        if (playLocationHistoryAnimationToken != null) {
            handler.removeCallbacksAndMessages(playLocationHistoryAnimationToken);
            googleMap.stopAnimation();
            playLocationHistoryAnimationToken = null;

            // and fix the map once everything will have finished
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    trackingLine.setPoints(computeSplinePoints());

                    playLocationHistoryButton.setImageResource(R.drawable.ic_action_play);
                    playLocationHistoryButton.setEnabled(true);
                    zoomToMarkers();
                }
            }, 100);
        } else {
            playLocationHistoryAnimationToken = new Object();
            playLocationHistoryButton.setImageResource(R.drawable.ic_action_stop);
            playLocationHistoryButton.setEnabled(true);

            partialSplinePoints = new ArrayList<>();
            CatmullRomSpline c = new CatmullRomSpline(points, REPLAY_PATH_SUBSTEPS);

            LatLngBounds.Builder builder = LatLngBounds.builder();
            if (!points.isEmpty()) builder.include(points.get(0));
            animatePath(c.iterator(), 0, builder);
        }
    }

    private void animatePath(final Iterator<LatLng> pointsIterator, final int index, final LatLngBounds.Builder builder) {
        if (pointsIterator.hasNext()) {
            partialSplinePoints.add(pointsIterator.next());
            trackingLine.setPoints(partialSplinePoints);
            if (index % REPLAY_PATH_SUBSTEPS == 0){
                int trueIndex = index / REPLAY_PATH_SUBSTEPS + 1;
                if (trueIndex < points.size()) {
                    builder.include(points.get(trueIndex));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100), REPLAY_SPEED, new GoogleMap.CancelableCallback() {
                        @Override
                        public void onFinish() {
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
            }
            handler.postAtTime(new Runnable() {
                @Override
                public void run() {
                    animatePath(pointsIterator, index+1, builder);
                }
            }, playLocationHistoryAnimationToken, SystemClock.uptimeMillis() + REPLAY_SPEED / REPLAY_PATH_SUBSTEPS);
        } else {
            playLocationHistoryAnimationToken = null;
            playLocationHistoryButton.setImageResource(R.drawable.ic_action_play);
        }

    }

    private void fetchWiFireLocationHistory(int taskCode) {
        if (getActivity() == null) return;
        final Activity context = getActivity();
        BackgroundExecutor.submit(new ExternalCall<List<GPSReading>>() {
            @Override
            public List<GPSReading> submit() {
                try {
                    DataStore gpsReadingDatastore = FlowHelper.getInstance(context).getCurrentDevice().getDevice().getDataStore("GPSReading");

                    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    String timeAgo = sdf.format(new Date(System.currentTimeMillis() - (maximumReadings * saveReadingPeriod)));

                    Log.v("fetchWiFi...History", "gpsReadingDatastore.getItemsByQuery(\"@gpsreadingtime >= '\" + " + timeAgo + "+ \"'\")");
                    DataStoreItems gpsReadingDatastoreItems = gpsReadingDatastore.getItemsByQuery("@gpsreadingtime >= '" + timeAgo + "'");

                    Log.v("fetchWiFi...History", "Got " + gpsReadingDatastoreItems.size() + " entries from location history");

                    List<GPSReading> gpsReadings = new ArrayList<>(Math.min(maximumReadings, gpsReadingDatastoreItems.size()));
                    int skip = 0;
                    for (DataStoreItem dataStoreItem : gpsReadingDatastoreItems) {
                        if (gpsReadingDatastoreItems.size() - skip > maximumReadings) {
                            ++skip;
                        } else {
                            gpsReadings.add(new GPSReading(dataStoreItem.getContent()));
                        }
                    }

                    Log.v("fetchWiFi...History", "Down to " + gpsReadings.size() + " / " + maximumReadings + " entries from location history");


                  return gpsReadings;

                } catch (Exception exception) {
                    Log.e("MapActivity", exception.toString());
                    exception.printStackTrace();
                    return null;
                }
            }
        }, this, taskCode, context);
    }

    @Override
    public void onBackgroundExecutionResult(final List<GPSReading> gpsReadings, final int taskCode) {
        if (gpsReadings != null && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
              @Override
              public void run() {
                updateMap(gpsReadings);
                loadingInitialLocations.setVisibility(View.GONE);
                if (taskCode == INITIAL_LOCATIONS_LOAD_EXECUTOR_ID) {
                  zoomToMarkers();
                }
              }
            });
        }
    }

    private void zoomToMarkers() {
        ArrayList<Marker> markers = new ArrayList<>(positionMarkers.values());
        if (wifireCurrentLocation != null) markers.add(wifireCurrentLocation);
        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (Marker marker : markers){
            bounds.include(marker.getPosition());
        }
        if (playLocationHistoryAnimationToken == null && markers.size() > 0) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    hasZoomedToMarkers = true;
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    private void updateMap(List<GPSReading> gpsReadings) {
        if (googleMap != null && getActivity() != null) {
            if (gpsReadings != null) {

                points = new ArrayList<>();
                for (Marker m : positionMarkers.values()){
                    m.remove();
                }
                positionMarkers.clear();
                for (GPSReading gpsReading : gpsReadings) {
                    Date date = gpsReading.getUTCDateTime();
                    LatLng location = new LatLng(gpsReading.getLat(), gpsReading.getLng());
                    MarkerOptions markerOptions = (new MarkerOptions()
                            .position(location)
                            .title(gpsReading.getUTCDateTime().toString())
                            .snippet(String.format(
                                    "Altitude: %.1f  " +
                                            "Speed: %.2fm/s  " +
                                            "  " +
                                            "Satalites: %d  " +
                                            "HDOP: %d"
                                    , gpsReading.getAltitude(), gpsReading.getSpeed(),
                                    gpsReading.getSatellites(), (int) gpsReading.getHDOP()))
                            .draggable(false)
                            .visible(doneInspectingPointsButton.getVisibility() == View.VISIBLE));
                    positionMarkers.put(date, googleMap.addMarker(markerOptions));
                    points.add(location);
                }
            }

            if (!positionMarkers.isEmpty()) {
                // update the hues of each of the markers to show age
                Date oldestDate = positionMarkers.firstKey();
                Date newestDate = positionMarkers.lastKey();
                for (Map.Entry<Date, Marker> kv : positionMarkers.entrySet()) {
                    Date date = kv.getKey();
                    Marker marker = kv.getValue();
                    marker.setIcon(
                            BitmapDescriptorFactory.defaultMarker(
                                    // interp hue between newest and oldest
                                    getHueFromTimestamp(date, newestDate, oldestDate)
                            )
                    );

                }
            }

            trackingLine.setPoints(computeSplinePoints());
        }
    }

    private ArrayList<LatLng> computeSplinePoints() {
        ArrayList<LatLng> t = new ArrayList<LatLng>();
        if (points.size() >= 2) {
            CatmullRomSpline c = new CatmullRomSpline(points, REPLAY_PATH_SUBSTEPS);
            for (LatLng l : c) {
                t.add(l);
            }
        }
        return t;
    }

    /** Interpolate the time difference between the timestamp and now to a hue going from red to
     * green as the timestamp becomes newer
     *
     * @return the hue value
     */
    private float getHueFromTimestamp(Date utcDateTime, Date newestDate, Date oldestDate) {
        long millisAgo = newestDate.getTime() - utcDateTime.getTime();
        long maxMillisAgo = Math.min(MILLIS_AGO_HUE_CEIL, newestDate.getTime() - oldestDate.getTime());
        float lerp;
        if (millisAgo > 0){
            lerp = Math.min(1, (float) millisAgo / (float) maxMillisAgo);
        } else {
            lerp = 0;
        }
        return (1-lerp) * 100;
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (getActivity() == null) return;
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
                phoneLocationOptions = new MarkerOptions()
                        .position(latlng)
                        .title("My Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(MY_LOCATION_COLOR))
                        .draggable(false);
                phoneLocation = googleMap.addMarker(phoneLocationOptions);
            } else {
                phoneLocationOptions.position(latlng);
            }
        }
    }

     /**
     * Shows Flow's response to sent async command.
     */
    @Override
    public void onAsyncMessageResponse(MessagingEvent.AsyncMessageResponse response) {
        String responseText = null;
        boolean success = true;
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
                    success = false;
                    break;
                case SEND_FAILED:
                    responseText = getString(R.string.response_failed);
                    success = false;
                    break;
                default:
                    responseText = "";
                    break;
            }
        }
        if (!success){
            updateTimer.cancel();
            ActivitiesAndFragmentsHelper.showFragmentChangeDialog(
                    R.string.wifire_connectivity_problems,
                    R.string.back_to_connected_devices,
                    (FlowActivity) activity,
                    SimpleFragmentFactory.createFragment(ConnectedDevicesFragment.TAG));
        }
        //online = success;
        Log.v("onAsyncMessageResponse", response.name() + " - " + response + " :: " + responseText);
    }

    @Override
    public void onTextMessageReceived(AsyncMessage msg) {
         Log.v("onTextMessageReceived", msg.buildXml());
    }

    @Override
    public void onCommandMessageReceived(AsyncMessage msg) {
    }

    @Override
    public void onCommandRXMessageReceived(AsyncMessage msg) {
        CommandResponseHandler resp = waitingCommands.get(msg.getNode("clientid"));
        if (resp == null) {
            Log.w("onCommandRXMe...ved", "No response found for message " + msg.getNode("clientid"));
        } else {
            waitingCommands.remove(msg.getNode("clientid"));
            if (getActivity() != null) resp.onCommandResponse(msg);
        }
    }


    /**
     * --------------------------------- Dialog functions ----------------------------------------*
     */

    @Override
    public void onDevicePresenceChangeListener(boolean isConnected) {
        if (!isConnected) {
            updateTimer.cancel();
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
            //online = true;
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
