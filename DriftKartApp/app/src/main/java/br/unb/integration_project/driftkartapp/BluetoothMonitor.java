package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

//The app needs to be Client and the Arduino the Server. Client will connect on Server MAC Address.
//TODO: 1- Get BluetoothAdapter; 2- Device discovery; 3- Pair with device;
public class BluetoothMonitor {

    private BluetoothAdapter btAdapter;
    private Context appContext;
    public static final int NOT_HAVE_BLUETOOTH = -1;
    public static final int BLUETOOTH_OFFLINE = 0;
    public static final int BLUETOOTH_ONLINE = 1;

    //getDefaultAdapter() returns null if called from emulator.
    public BluetoothMonitor(Context pContext, BluetoothAdapter pBtAdapter) {
        btAdapter = pBtAdapter;
        appContext = pContext;
    }

    public void startDeviceDiscovery() {
        btAdapter.startDiscovery();
    }

    public int verifyBluetoothReady() {
        if(btAdapter != null) {
            if (btAdapter.isEnabled()) {
                return BLUETOOTH_ONLINE;
            } else {
                return BLUETOOTH_OFFLINE;
            }
        } else {
            return NOT_HAVE_BLUETOOTH;
        }
    }

    public void cancelDeviceDiscovery() {
        btAdapter.cancelDiscovery();
    }

    //Need to register for BluetoothDevice.ACTION_BOND_STATE_CHANGED;
    public void pairWithFoundedDevice(BluetoothDevice pBtDevice) {
        final String DEVICE_MAC_ADDRESS = pBtDevice.getAddress();
        BluetoothDevice previouslyPairedBtDevice = null;
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if(pairedDevices != null) {
            for(BluetoothDevice btDevice : pairedDevices) {
                if(btDevice.getAddress().equals(DEVICE_MAC_ADDRESS)) {
                    previouslyPairedBtDevice = btDevice;
                    break;
                }
            }
            if(previouslyPairedBtDevice == null) {
                //Creating bluetooth pairing.
                try {
                    Method method = pBtDevice.getClass().getMethod("createBond", (Class[])null);
                    method.invoke(pBtDevice, (Object[])null);
                }catch (Exception e) {e.printStackTrace();}
            }
        }
    }

    /*public void openSerialConnToFoundedDevice(BluetoothDevice btDevice) {
        final String SERIAL_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        try {
                //TODO: Implement this method.
                BluetoothSocket btScocket = btDevice.createInsecureRfcommSocketToServiceRecord(UUID
                        .fromString(SERIAL_UUID));
                btScocket.connect();
                Toast.makeText(appContext, "Conectou", Toast.LENGTH_LONG).show();
                btScocket.close();
        } catch(IOException ioe) {}
    }*/
}
