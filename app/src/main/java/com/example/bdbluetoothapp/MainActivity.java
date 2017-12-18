package com.example.bdbluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.altbeacon.beacon.AltBeacon;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    private final static int REQUEST_ENABLE_BT = 1;

    //private DeviceListFragment mDeviceListFragment;
    private BluetoothAdapter BTAdapter;

    private BeaconManager beaconManager;
    ArrayAdapter<String> listAdapter;
    ArrayList<String> deviceItems;
    Button connectNew;
    ListView listView;

    // variables to store values

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        beaconManager = BeaconManager.getInstanceForApplication(this);

        if (beaconManager.checkAvailability()) {
            Log.i("BM", "Beacon Manager started");
        } else {
            Log.i("BM", "Beacon Manager unusable");
            System.exit(0);
        }

        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        // Phone does not support Bluetooth so let the user know and exit.
        if (BTAdapter == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Not compatible")
                    .setMessage("Your phone does not support Bluetooth")
                    .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
//            new AlertDialog.Builder(this)
//                    .setTitle("Bluetooth Support")
//                    .setMessage("Your phone support Bluetooth, enjoy!")
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .show();

            if (!BTAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // a short duration message on the screen
        Toast.makeText(getApplicationContext(), "Welcome to the app", Toast.LENGTH_SHORT).show();


        init();
        //addPairedDevice();
        //searchForNewDevice();

    }

    private void init() {
        connectNew = (Button) findViewById(R.id.bConnectNew);
        listView = (ListView) findViewById(R.id.myList);
        deviceItems = new ArrayList<String>();
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceItems);
        listView.setAdapter(listAdapter);
    }

    private void addPairedDevice() {
        Set<BluetoothDevice> bondedDevices = BTAdapter.getBondedDevices();

        if (bondedDevices.size() > 0) {
            for (BluetoothDevice device : bondedDevices) {
                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                deviceItems.add(newDevice.getDeviceName());
            }
        }

        listAdapter.notifyDataSetChanged();
    }


    private void searchForNewDevice() {
        deviceItems.clear();

        //BTAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(bReciever, filter);
        BTAdapter.startDiscovery();

        //String temp = bReciever.getResultData();
        //Log.d("rec", temp);

//        FragmentManager fragmentManager = getSupportFragmentManager();
//
//        mDeviceListFragment = DeviceDiscoveryFragment.newInstance(BTAdapter);
//        fragmentManager.beginTransaction().replace(R.id.container, mDeviceListFragment).commit();
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d("DEVICE_NAME", device.getName()+"\n");
                // Create a new device item

                String majorID = device.toString();



                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                // Add it to our adapter
                deviceItems.add(newDevice.getDeviceName()+"\n"+majorID);
                listAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            beaconManager.stopMonitoringBeaconsInRegion(new Region("MyRangeId", null, null, null));
            beaconManager.stopRangingBeaconsInRegion(new Region("MyRangeId2", null, null, null));
        } catch (RemoteException e) {    }
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.removeAllRangeNotifiers();
        beaconManager.unbind(this);
        beaconManager.setAndroidLScanningDisabled(true);
    }
    @Override
    public void onBeaconServiceConnect() {
        final String TAG = "MonitoringActivity";
        final String TARGET_UUID = "f7826da6-4fa2-4e98-8024-bc5b71e0893e";
        UUID myUUID = UUID.fromString(TARGET_UUID);
        Identifier myID = Identifier.fromUuid(myUUID);
        Log.i(TAG, "THIS IS MY ID: "+myID.toString());


        final int TARGET_UUID2 = 50215;
        Identifier myID2 = Identifier.fromInt(TARGET_UUID2);
        Log.i(TAG, "THIS IS MY ID2: "+myID2.toString());


        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw an beacon for the first time!");
                if (region.getId1() != null) {
                   //Toast.makeText(getApplicationContext(), region.getId1().toString(), Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "\n"+ region.getId1()+"\n" + region.getId2());
                }
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see an beacon");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);
                if (state == MonitorNotifier.INSIDE) {
                    Toast.makeText(getApplicationContext(), "Enter zone"+region.getUniqueId().toString(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Leave zone", Toast.LENGTH_SHORT).show();
                }

                Log.i(TAG, "REgion = " + region.getUniqueId());

                deviceItems.add("try");
            }

        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon I see is " + beacons.iterator().next().getBluetoothName());


                    Log.i(TAG, "Reading…"+"\n"+"proximityUuid:"+" "+ beacons.iterator().next().getId1()+"\n"+
                            "major:"+" "+beacons.iterator().next().getId2()+"\n"+
                            "minor:"+" "+beacons.iterator().next().getId3()+"\n"+
                            "RSSI:"+" "+beacons.iterator().next().getRssi());
                    Log.i(TAG, "Reading### " beacons.iterator().next().);
                }

            }
        });

        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("MyRangeId1", null, myID2, null));
            beaconManager.startRangingBeaconsInRegion(new Region("MyRangeId2", null, myID2, null));
        } catch (RemoteException e) {    }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
