package br.unb.integration_project.driftkartapp;

import android.os.Handler;

//TODO: Refactor this class, so it can readData and sendData
public class DataFlowHandling {

    private MainActivity mainActivity;
    private BluetoothConnection btConnection;
    private Handler uiHandler;

    public DataFlowHandling(MainActivity pMainActivity, BluetoothConnection pBtConnection,
                            Handler pUiHandler) {
        mainActivity = pMainActivity;
        btConnection = pBtConnection;
        uiHandler = pUiHandler;
    }

    public void setEngineMode(byte pEngineMode) {
        byte[] engineFlagData = new byte[1];
        engineFlagData[0] = pEngineMode;
        int returnSendData = btConnection.sendData(engineFlagData);
        if(returnSendData < 0) {
            mainActivity.showLongToastDialog("Não conectado com o Kart!");
        }
    }

    public void startSensorMonitoring() {
        Runnable dataReadedNotification = new Runnable() {
            @Override
            public void run() {
                int[] readedData = btConnection.getDataArray();
                char flag = (char)readedData[0];
                int lowByte = readedData[1];
                int highByte = readedData[2] << 8;
                //Give permission to readData() to read next bytes of data.
                btConnection.setCanWriteToArray(true);
                int dataReaded = highByte | lowByte;
                if(flag == 'S') {
                    mainActivity.setSpeed(dataReaded);
                }else {
                    mainActivity.setBattery(dataReaded, true);
                }
            }
        };
        btConnection.readData(3, uiHandler, dataReadedNotification, null, true);
    }

}
