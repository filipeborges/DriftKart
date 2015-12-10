package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
public class TestDataFlowHandling {

    MainActivity mainActivityMock;
    BluetoothConnection btConnectionMock;
    Handler uiHandlerMock;
    DataFlowHandling dtFlowHandling;

    @Before
    public void setUp() {
        mainActivityMock = Mockito.mock(MainActivity.class);
        btConnectionMock = Mockito.mock(BluetoothConnection.class);
        uiHandlerMock = Mockito.mock(Handler.class);
        dtFlowHandling = new DataFlowHandling(mainActivityMock, btConnectionMock, uiHandlerMock);
    }

    @Test
    public void testSetEngineMode() {
        //Send data returns succeeded value.
        Mockito.when(btConnectionMock.sendData((byte[])Mockito.any())).thenReturn(3);
        dtFlowHandling.setEngineMode((byte)1);
        Mockito.verify(mainActivityMock, Mockito.times(0)).showLongToastDialog(Mockito.anyString());

        //Send data return failed value (-1 and -2).
        Mockito.when(btConnectionMock.sendData((byte[])Mockito.any())).thenReturn(-1);
        dtFlowHandling.setEngineMode((byte) 1);
        Mockito.verify(mainActivityMock, Mockito.times(1)).showLongToastDialog(Mockito.anyString());

        Mockito.when(btConnectionMock.sendData((byte[]) Mockito.any())).thenReturn(-2);
        dtFlowHandling.setEngineMode((byte) 1);
        Mockito.verify(mainActivityMock, Mockito.times(2)).showLongToastDialog(Mockito.anyString());
    }

    @Test
    public void testStartSensorMonitoring() {
        DataFlowHandling dtFlowHandling = new DataFlowHandling(mainActivityMock,
                btConnectionMock, uiHandlerMock);

        dtFlowHandling.startSensorMonitoring();
        Mockito.verify(btConnectionMock, Mockito.times(1))
                .readData(Mockito.eq(3), Mockito.eq(uiHandlerMock), Mockito.any(Runnable.class),
                        Mockito.any(Runnable.class), Mockito.eq(true));
    }
}
