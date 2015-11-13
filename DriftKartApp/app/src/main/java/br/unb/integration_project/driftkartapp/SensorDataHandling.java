package br.unb.integration_project.driftkartapp;

import android.os.Handler;

public class SensorDataHandling {

    private MainActivity mainActivity;
    private BluetoothConnection btConnection;
    private Handler uiHandler;

    public SensorDataHandling(MainActivity pMainActivity, BluetoothConnection pBtConnection,
                              Handler pUiHandler) {
        mainActivity = pMainActivity;
        btConnection = pBtConnection;
        uiHandler = pUiHandler;
    }

    public void startSensorMonitoring() {
        showReadedData();
    }

    public void showReadedData() {
        Runnable dataReadedNotification = new Runnable() {
            @Override
            public void run() {
                int[] readedData = btConnection.getDataArray();
                int lowByte = readedData[0];
                int highByte = readedData[1] << 8;
                //Give permission to readData() to read next bytes of data.
                btConnection.setCanWriteToArray(true);
                int dataReaded = highByte | lowByte;
                //TODO: Test this before.
                //TODO: Verify if buffer data is from SPEED or BATTERY.
                mainActivity.setSpeed(String.valueOf(dataReaded));
            }
        };
        btConnection.readData(2, uiHandler, dataReadedNotification, null, true);
    }

}
