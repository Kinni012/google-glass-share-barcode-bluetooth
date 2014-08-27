package com.corneliudascalu.testglass;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollView;

import com.corneliudascalu.testglass.service.BluetoothClient;
import com.corneliudascalu.testglass.service.BluetoothService;
import com.corneliudascalu.testglass.util.RequestCodes;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MainActivity extends Activity {

    public static final String TAG = "Main";

    public static final int BT_ENABLE_REQUEST_CODE = 123;

    public static final String EXTRA_DEVICES = "devices";

    /** {@link CardScrollView} to use as the main content view. */
    private CardScrollView mCardScroller;

    /** "Hello World!" {@link View} generated by {@link #buildView()}. */
    private View mView;

    private BluetoothClient bluetoothClient;

    private boolean bound;

    private View cardView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mView = buildView();

        setContentView(mView);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not available");
        } else if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                    RequestCodes.REQUEST_ENABLE_BLUETOOTH);
        } else {
            Log.d(TAG, "Bluetooth already enabled");
            Toast.makeText(this, "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
            getPairedDevices();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardView.requestFocus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    /**
     * Builds a Glass styled "Hello World!" view using the {@link Card} class.
     */
    private View buildView() {
        Card card = new Card(this);

        card.setText(R.string.hello_world);
        cardView = card.getView();

        return cardView;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RequestCodes.REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth enabled");
                    Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
                    getPairedDevices();
                } else {
                    Log.e(TAG, "Failed to enable bluetooth");
                    Toast.makeText(this, "Failed to enable bluetooth", Toast.LENGTH_SHORT).show();
                }
                break;
            case RequestCodes.REQUEST_SELECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    BluetoothDevice device = data
                            .getParcelableExtra(DeviceChooserActivity.EXTRA_SELECTED_DEVICE);
                    connectToDevice(device);
                } else {
                    Toast.makeText(this, "Failed to select a device", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        bluetoothClient.connectToDevice(device);
    }

    public void getPairedDevices() {
        new AsyncTask<Void, Void, ArrayList<BluetoothDevice>>() {

            @Override
            protected ArrayList<BluetoothDevice> doInBackground(Void... params) {
                BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
                Set<BluetoothDevice> devices = defaultAdapter.getBondedDevices();
                return new ArrayList<BluetoothDevice>(devices);
            }

            @Override
            protected void onPostExecute(ArrayList<BluetoothDevice> devices) {
                super.onPostExecute(devices);
                chooseDevice(devices);
            }
        }.execute();
    }

    public void chooseDevice(ArrayList<BluetoothDevice> devices) {
        Intent intent = new Intent(this, DeviceChooserActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_DEVICES, devices);
        startActivityForResult(intent, RequestCodes.REQUEST_SELECT_DEVICE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            bluetoothClient = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
}
