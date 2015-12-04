package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

//TODO: This class should be: PrepareCommunication.
public class PrepareDeviceCommunication {

    private boolean isReceiverRegistered = false;
    private MainActivity mainActivity;
    private BluetoothConnection btConnection;
    private BluetoothDevice btDevice;
    private DataFlowHandling dataFlowHandling;
    private Handler uiHandler;
    private IntentFilter filterAction; //Needed for unit test mock.
    private final String KART_MAC_ADDRESS = "20:15:02:03:53:66";
    private BroadcastReceiver btActionReceiver = new BroadcastReceiver() {
        //CAUTION: This method will run on main thread of process.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                mainActivity.showSearchDialog();

            } else if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice foundedDevice = intent.getParcelableExtra(BluetoothDevice
                        .EXTRA_DEVICE);
                if(foundedDevice.getAddress().equals(KART_MAC_ADDRESS)) {
                    btConnection.cancelDeviceDiscovery();
                    btDevice = foundedDevice;
                    if(btConnection.pairWithFoundedDevice(btDevice) == BluetoothConnection
                            .PREVIOUSLY_PAIRED) {
                        if(btConnection.openSerialConnToDevice(btDevice, uiHandler,
                                getConnectNotification(), getExceptionNotification()) != BluetoothConnection.SERIAL_CONN_OPENED) {
                            mainActivity.showLongToastDialog("Falha na Conexão!");
                        }
                    }
                }

            } else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if(btDevice == null) {
                    btConnection.cancelDeviceDiscovery();
                    mainActivity.dismissSearchDialog();
                    mainActivity.showConnTryAgainDialog();
                }

            } else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int BOND_STATE = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                final int PREVIOUS_BOND_STATE = intent.getIntExtra(BluetoothDevice
                        .EXTRA_PREVIOUS_BOND_STATE, -1);
                if(BOND_STATE == BluetoothDevice.BOND_BONDED) {
                    if(btConnection.openSerialConnToDevice(btDevice, uiHandler,
                            getConnectNotification(), getExceptionNotification()) != BluetoothConnection.SERIAL_CONN_OPENED) {
                        mainActivity.showLongToastDialog("Falha na Conexão!");
                    }
                } else if(PREVIOUS_BOND_STATE == BluetoothDevice.BOND_BONDING
                        && BOND_STATE == BluetoothDevice.BOND_NONE) {
                    mainActivity.dismissSearchDialog();
                }
            }
        }
    };

    public Runnable getExceptionNotification() {
        return new Runnable() {
            @Override
            public void run() {
                btConnection.openSerialConnToDevice(btDevice, uiHandler,
                        getConnectNotification(), getExceptionNotification());
            }
        };
    }

    public Runnable getConnectNotification() {
        return new Runnable() {
            @Override
            public void run() {
                mainActivity.dismissSearchDialog();
                mainActivity.showLongToastDialog("Conectado ao Kart!");
                dataFlowHandling.startSensorMonitoring();
            }
        };
    }

    public PrepareDeviceCommunication(MainActivity pActivity) {
        uiHandler = new Handler(Looper.getMainLooper());
        mainActivity = pActivity;
        filterAction = new IntentFilter();
        btConnection = new BluetoothConnection(BluetoothAdapter.getDefaultAdapter());
        dataFlowHandling = new DataFlowHandling(mainActivity, btConnection, uiHandler);
    }

    public DataFlowHandling getDataFlowHandling() {
        return dataFlowHandling;
    }

    public void establishBluetoothConnection() {
        //TODO: Handdling the case of device not discoverable, but bluetooth ON.
        switch (btConnection.verifyBluetoothReady()) {
            case BluetoothConnection.BLUETOOTH_OFFLINE:
                mainActivity.showEnableBluetoothDialog(BluetoothAdapter.
                        ACTION_REQUEST_ENABLE);
                break;
            case BluetoothConnection.NOT_HAVE_BLUETOOTH:
                mainActivity.showLongToastDialog("Dispositivo sem Bluetooth!");
                break;
            case BluetoothConnection.BLUETOOTH_ONLINE:
                if(!isReceiverRegistered) {
                    filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                    filterAction.addAction(BluetoothDevice.ACTION_FOUND);
                    filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    filterAction.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);

                    isReceiverRegistered = true;
                    mainActivity.registerReceiver(btActionReceiver, filterAction);
                }
                btConnection.startDeviceDiscovery();
                break;
        }
    }

    public void closeBluetoothResources() {
        btConnection.cancelDeviceDiscovery();
        if(isReceiverRegistered) {
            mainActivity.unregisterReceiver(btActionReceiver);
        }
        try {
            btConnection.closeBluetoothResources();
        }catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    //Created for unit test purposes.
    public void setAttributesForUnitTest(BluetoothConnection btConnectionMock,
                                         DataFlowHandling dataFlowHandlingMock) {
        btConnection = btConnectionMock;
        dataFlowHandling = dataFlowHandlingMock;
    }
}
