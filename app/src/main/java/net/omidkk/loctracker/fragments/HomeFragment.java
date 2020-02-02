package net.omidkk.loctracker.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import net.omidkk.loctracker.R;
import net.omidkk.loctracker.app.MyApplication;
import net.omidkk.loctracker.model.User;
import net.omidkk.loctracker.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private CardView frameCoordinates;
    private AppCompatTextView txtLatitude, txtLongitude;
    private double latitude, longitude;
    private boolean passedMaxDistance = false;
    private User user;
    private Bitmap profileImage = null;
    private LocationCallback locationCallback;
    private Activity context;
    private int max = 5;
    private View markerView;
    private FusedLocationProviderClient mFusedLocationClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getActivity();
        initViews(view);

        checkLocationPermissions();
        //marker default image
        profileImage = BitmapFactory.decodeResource(getResources(), R.drawable.img_placeholder2);

        frameCoordinates.setVisibility(View.GONE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        getStartLocation();
        markerView = inflater.inflate(R.layout.marker, null, false);

        getSettings();

        user = User.getUser();
        String profileImageUrl = String.valueOf(user.getPhoto());
        if (profileImageUrl != null) {
            new DownloadProfileImage().execute(profileImageUrl);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getLocationUpdates();

        return view;
    }


    private void initViews(View view) {
        txtLatitude = view.findViewById(R.id.txt_latitude);
        txtLongitude = view.findViewById(R.id.txt_longitude);
        frameCoordinates = view.findViewById(R.id.frame_coordinates);
    }

    private void checkLocationPermissions() {if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
        // Permission is not granted
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1234);
    }
    }

    private void getStartLocation() throws SecurityException {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    User.getUser().setLocation(location);
                    updateLocationOnMap(location);
                    //tracking location changes in google analytics
                    MyApplication.getInstance().trackEvent(Constants.CATEGORY_LOCATION, Constants.ACTION_LOCATION_CHANGES, "lat:" + location.getLatitude() + "--" + "lng" + location.getLongitude());
                }
            }
        });
    }

    private void getSettings() {
        SharedPreferences preferences = getActivity().getSharedPreferences(Constants.KEY_SETTINGS_PREFERENCES, MODE_PRIVATE);
        max = preferences.getInt(Constants.KEY_DISTANCE, Constants.DEFAULT_VALUE_DISTANCE);
    }

    private class DownloadProfileImage extends AsyncTask<String, Void, Bitmap> {
        protected Bitmap doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                return null;
            }
        }

        protected void onPostExecute(Bitmap result) {
            profileImage = result;
            LatLng latLng = new LatLng(latitude, longitude);
            addMarker(latLng);
        }

    }

    private void getLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(Constants.UPDATE_INTERVAL);
        locationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location.hasAccuracy()) {
                        if (location.getAccuracy() < 15.0) {
                            setCoordinateTextViews(location);
                            updateLocationOnMap(location);
                            calculateDistance(location);
                        }
                    }

                }
            }
        };
    }

    private void setCoordinateTextViews(Location location) {
        if (frameCoordinates.getVisibility() == View.GONE) {
            frameCoordinates.setVisibility(View.VISIBLE);
        }
        txtLatitude.setText(String.valueOf(location.getLatitude()));
        txtLongitude.setText(String.valueOf(location.getLongitude()));
    }

    private void calculateDistance(Location location) {
        double startLat = user.getLocation().getLatitude();
        double startLng = user.getLocation().getLongitude();
        float[] results = new float[2];
        Location.distanceBetween(startLat, startLng, location.getLatitude(), location.getLongitude(), results);
        if (results[0] > max) {
            if (!passedMaxDistance) {
                MyApplication.getInstance().trackEvent(Constants.CATEGORY_LOCATION, Constants.ACTION_PASS_MAX_DISTANCE, "passed maximum distance");
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("warning!")
                        .setIcon(R.drawable.ic_warning_24px)
                        .setMessage("please go back to the original position!")
                        .setCancelable(false)
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                builder.show();
            }
            passedMaxDistance = true;
        }
    }

    private void updateLocationOnMap(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if (mMap != null) {
            mMap.clear();
        }
        addMarker(latLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
    }


    public void addMarker(LatLng latLng) {
        CircleImageView imgMarker = markerView.findViewById(R.id.img_marker);
        imgMarker.setImageBitmap(profileImage);
        Bitmap markerLayout = createDrawableFromView(context, markerView);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(markerLayout);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .draggable(false)
                .icon(icon);
        mMap.addMarker(markerOptions);

    }

    public static Bitmap createDrawableFromView(Activity context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() throws SecurityException {
        mFusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }




}
