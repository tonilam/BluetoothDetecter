package com.example.bdbluetoothapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by tony on 18/12/17.
 */

public class DeviceDiscoveryFragment extends Fragment implements AdapterView.OnItemClickListener {
    private BluetoothAdapter BTAdapter;
    private ArrayList<DeviceItem> deviceItemList;

    private OnFragmentInteractionListener mListener;
    private static BluetoothAdapter bTAdapter;
    private AbsListView mListView;
    private ArrayAdapter<DeviceItem> listAdapter;

    private ArrayList<String> deviceItems;


    public static DeviceDiscoveryFragment newInstance(BluetoothAdapter adapter) {
        DeviceDiscoveryFragment fragment = new DeviceDiscoveryFragment();
        bTAdapter = adapter;
        return fragment;
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("DEVICELIST", "Bluetooth device found\n");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false");
                // Add it to our adapter
                deviceItems.add(newDevice.getDeviceName());
                listAdapter.notifyDataSetChanged();
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        getActivity().registerReceiver(bReciever, filter);
        BTAdapter.startDiscovery();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }
}