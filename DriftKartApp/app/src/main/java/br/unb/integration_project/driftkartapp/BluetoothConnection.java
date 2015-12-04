package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

//The app needs to be Client and the Arduino the Server. Client will connect on Server MAC Address.
//TODO: Close, on app quit, all resources used on Bluetooth connection.
public class BluetoothConnection {

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private int[] dataArray;
    private boolean canWriteToArray;
    private OutputStream outStream;
    public static final int PREVIOUSLY_PAIRED = 10;
    public static final int PAIRING_IN_PROGRESS = 11;
    public static final int PAIRING_EXCEPTION = 12;
    public static final int BLUETOOTH_OFFLINE = 20;
    public static final int BLUETOOTH_ONLINE = 21;
    public static final int NOT_HAVE_BLUETOOTH = 22;
    public static final int SERIAL_CONN_OPENED = 30;
    public static final int SERIAL_CONN_EXCEPTION = 31;
    public static final int NOT_PREVIOUSLY_PAIRED = 32;

    //getDefaultAdapter() returns null if called from emulator.
    public BluetoothConnection(BluetoothAdapter pBtAdapter) {
        btAdapter = pBtAdapter;
    }

    public int[] getDataArray() {
        return dataArray;
    }

    public synchronized void setCanWriteToArray(boolean pValue) {
        canWriteToArray = pValue;
    }

    public synchronized boolean canWriteToArray() {
        return canWriteToArray;
    }

    public void closeBluetoothResources() throws IOException {
        if(btSocket != null) {
            InputStream input = btSocket.getInputStream();
            input.close();
            btSocket.close();
        }
        if(outStream != null) {
            outStream.close();
        }
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
    //TODO: Refactor unit test.
    public int pairWithFoundedDevice(BluetoothDevice pBtDevice) {
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
                    //TODO: Try to change reflection to createBond().
                    Method method = pBtDevice.getClass().getMethod("createBond", (Class[])null);
                    method.invoke(pBtDevice, (Object[])null);

                    return PAIRING_IN_PROGRESS;
                }catch (Exception e) {
                    e.printStackTrace();
                    return PAIRING_EXCEPTION;
                }
            }
        }
        return PREVIOUSLY_PAIRED;
    }

    public int sendData(byte[] dataArray) {
        try {
            if(outStream == null) {
                outStream = btSocket.getOutputStream();
            }
            outStream.write(dataArray);
        }catch (IOException ioe) {
            return -1;
        }catch (NullPointerException npe) {
            return -2;
        }
        return dataArray.length;
    }

    public void readData(final int bytesCount, final Handler handler,
                         final Runnable dataReadedNotification, final Runnable exceptionNotification,
                         final boolean isLooped) {
        //TODO: Verify if btSocket is connected. InputStream is returned even if BTSOCKET is not connected.
        Thread readDataThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    setCanWriteToArray(true);
                    dataArray = new int[bytesCount];
                    InputStream input = btSocket.getInputStream();
                    do {
                        if(canWriteToArray()) {
                            for (int i = 0; i < bytesCount; i++) {
                                dataArray[i] = input.read();
                            }
                            setCanWriteToArray(false);
                            handler.post(dataReadedNotification);
                        }
                    }while (isLooped);
                    input.close();
                }catch (IOException ioe) {
                    ioe.printStackTrace();
                    handler.post(exceptionNotification);
                }
            }
        });
        readDataThread.start();
    }

    public int openSerialConnToDevice(BluetoothDevice btDevice, final Handler handler,
                                      final Runnable connectNotification,
                                      final Runnable exceptionNotification) {
        final String SERIAL_UUID = "00001101-0000-1000-8000-00805F9B34FB";

        if(btDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            return NOT_PREVIOUSLY_PAIRED;
        }else {
            try {
                //Just to make sure that other apps not started discovery process.
                cancelDeviceDiscovery();
                btSocket = btDevice.createRfcommSocketToServiceRecord(UUID
                        .fromString(SERIAL_UUID));

                Thread connectThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            btSocket.connect();
                            handler.post(connectNotification);
                        }catch (IOException ioe) {
                            handler.post(exceptionNotification);
                        }
                    }
                });
                connectThread.start();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return SERIAL_CONN_EXCEPTION;
            }
        }
        return SERIAL_CONN_OPENED;
    }
}
