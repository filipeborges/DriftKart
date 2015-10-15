package br.unb.integration_project.driftkartapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

public class SensorDataHanddling {

    private Context appContext;
    private BluetoothAdapter btAdapter;
    private BluetoothMonitor btMonitor;
    private AlertDialog searchDialog;
    private IntentFilter filterAction; //Needed for unit test mock.
    public static final int NOT_HAVE_BLUETOOTH = -1;
    public static final int BLUETOOTH_OFFLINE = 0;
    public static final int BLUETOOTH_ONLINE = 1;
    private BroadcastReceiver btActionReceiver = new BroadcastReceiver() {
        //CAUTION: This method will run on main thread of process.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(appContext);
                dialogBuilder.setMessage("Fazendo busca...");
                searchDialog = dialogBuilder.create();
                searchDialog.show();

            } else if(action.equals(BluetoothDevice.ACTION_FOUND)) {
                searchDialog.dismiss();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Toast.makeText(appContext, "Achou: "+device.getName(), Toast.LENGTH_LONG).show();
                //TODO: Handdling the case of device not discoverable, but bluetooth ON.


                /*Creating bond.
                try {
                    Method method = device.getClass().getMethod("createBond", (Class[])null);
                    method.invoke(device, (Object[])null);
                }catch (Exception e) {
                    e.printStackTrace();
                }*/

                btMonitor.cancelDeviceDiscovery();
                //btMonitor.openSerialConnToFoundedDevice(device);
            }
        }
    };

    public SensorDataHanddling(Context pContext, BluetoothAdapter pBtAdapter,
                               IntentFilter pFilterAction) {
        appContext = pContext;
        btAdapter = pBtAdapter;
        filterAction = pFilterAction;
    }

    public int establishBluetoothConnection() {
        if(btAdapter != null) {
            if(btAdapter.isEnabled()) {
                filterAction.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filterAction.addAction(BluetoothDevice.ACTION_FOUND);

                appContext.registerReceiver(btActionReceiver, filterAction);

                btMonitor = new BluetoothMonitor(appContext, btAdapter);
                btMonitor.startDeviceDiscovery();

                return BLUETOOTH_ONLINE;
            } else {
                return BLUETOOTH_OFFLINE;
            }
        } else {
            return NOT_HAVE_BLUETOOTH;
        }
    }

    public BroadcastReceiver getBtActionReceiver() {
        return btActionReceiver;
    }
}
