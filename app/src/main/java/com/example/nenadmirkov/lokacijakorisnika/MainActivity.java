package com.example.nenadmirkov.lokacijakorisnika;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    protected static final String TAG = "MainActivity";
    static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 1001;
    static final int REQUEST_CODE_PICK_ACCOUNT = 1002;
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    protected TextView mLatitudeText;
    protected TextView mLongitudeText;
    protected TextView mAccuracyText;
    protected TextView mAddressText;
    protected TextView mDateTimeText;
    protected TextView mProviderText;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    String mLastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));
        mAccuracyText = (TextView) findViewById((R.id.accuracy_text));
        mAddressText = (TextView) findViewById((R.id.address_text));
        mDateTimeText = (TextView) findViewById((R.id.datetime_text));
        mProviderText = (TextView) findViewById((R.id.provider_text));

        createLocationRequest();
        buildGoogleApiClient();
    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart, konektujemo se na PlayService...");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            Log.d(TAG, "Diskonektujemo se...");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPlayServices()) {
            Log.i(TAG, "PlayServices je u redu.");
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            String lat = String.valueOf(mLastLocation.getLatitude());
            String lng = String.valueOf(mLastLocation.getLongitude());
            String acc = String.valueOf(mLastLocation.getAccuracy());
            String provider = String.valueOf(mLastLocation.getProvider());

            mLatitudeText.setText("Latitude: " + lat);
            mLongitudeText.setText("Longitude: " + lng);
            mDateTimeText.setText("Last found at: " + mLastUpdateTime);
            mAccuracyText.setText("Accuracy: " + acc);
            mProviderText.setText("Provider: " + provider);

            getAdress(mLastLocation);
        } else {
            Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
        }
        startLocationUpdates();


    }

    private void updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            String lat = String.valueOf(mCurrentLocation.getLatitude());
            String lng = String.valueOf(mCurrentLocation.getLongitude());
            String acc = String.valueOf(mCurrentLocation.getAccuracy());
            String provider = String.valueOf(mCurrentLocation.getProvider());

            mLatitudeText.setText("Latitude: " + lat);
            mLongitudeText.setText("Longitude: " + lng);
            mDateTimeText.setText("Last updated at: " + mLastUpdateTime);
            mAccuracyText.setText("Accuracy: " + acc);
            mProviderText.setText("Provider: " + provider);

            getAdress(mCurrentLocation);
        } else {
            Log.d(TAG, "location is null ...............");
        }
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    protected void getAdress(Location loc) {

        //Moja modifikacija
        Double dblLat = Double.parseDouble(String.valueOf(loc.getLatitude()));
        Double dblLon = Double.parseDouble(String.valueOf(loc.getLongitude()));

        Geocoder myLocation = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> myList = myLocation.getFromLocation(dblLat, dblLon, 1);
            mAddressText.setText("Podaci o lokaciji:\n");
            for (int i = 0; i < myList.get(0).getMaxAddressLineIndex(); i++) {
                mAddressText.append(myList.get(0).getAddressLine(i) + "\n");
                Log.i(TAG, myList.get(0).getAddressLine(i));
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                showErrorDialog(status);
            } else {
                Toast.makeText(this, "Uređaj nije kompatibilan.",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private boolean checkUserAccount() {
        String accountName = AccountUtils.getAccountName(this);
        if (accountName == null) {
            // Then the user was not found in the SharedPreferences. Either the
            // application deliberately removed the account, or the application's
            // data has been forcefully erased.
            showAccountPicker();
            return false;
        }

        Account account = AccountUtils.getGoogleAccountByName(this, accountName);
        if (account == null) {
            // Then the account has since been removed.
            AccountUtils.removeAccount(this);
            showAccountPicker();
            return false;
        }

        return true;
    }

    private void showAccountPicker() {
        Intent pickAccountIntent = AccountPicker.newChooseAccountIntent(
                null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE},
                true, null, null, null, null);
        startActivityForResult(pickAccountIntent, REQUEST_CODE_PICK_ACCOUNT);
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RECOVER_PLAY_SERVICES:
      /* ... */
            case REQUEST_CODE_PICK_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    String accountName = data.getStringExtra(
                            AccountManager.KEY_ACCOUNT_NAME);
                    AccountUtils.setAccountName(this, accountName);
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Ova aplikacija zahteva Google nalog.",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
