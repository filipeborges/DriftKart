package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BluetoothAdapter.class, Looper.class})
public class TestPrepareDeviceCommunication {
    BluetoothAdapter btAdapterMock;
    PrepareDeviceCommunication deviceComm;
    IntentFilter filterActionMock;
    MainActivity mockActivity;

    @Before
    public void setUp() {
        mockActivity = Mockito.mock(MainActivity.class);
        PowerMockito.mockStatic(BluetoothAdapter.class);
        btAdapterMock = PowerMockito.mock(BluetoothAdapter.class);
        filterActionMock = Mockito.mock(IntentFilter.class);
        PowerMockito.mockStatic(Looper.class);
        deviceComm = new PrepareDeviceCommunication(mockActivity);
    }

    @Test
    public void testGetConnectNotification() {
        Runnable connectNotification = deviceComm.getConnectNotification();
        Assert.assertNotNull(connectNotification);
    }

    @Test
    public void testGetExceptionNotification() {
        Runnable exceptionNotification = deviceComm.getExceptionNotification();
        Assert.assertNotNull(exceptionNotification);
    }

    @Test
    public void testEstablishBluetoothConnection() {
        BluetoothConnection btConnectionMock = Mockito.mock(BluetoothConnection.class);
        Handler uiHandlerMock = Mockito.mock(Handler.class);
        DataFlowHandling dtFlowHandlingMock = new DataFlowHandling(mockActivity,
                btConnectionMock, uiHandlerMock);
        deviceComm.setAttributesForUnitTest(btConnectionMock, dtFlowHandlingMock);
        Mockito.when(btConnectionMock.verifyBluetoothReady())
                .thenReturn(BluetoothConnection.NOT_HAVE_BLUETOOTH);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(mockActivity, Mockito.times(1)).showLongToastDialog(Matchers.anyString());
        Mockito.when(btConnectionMock.verifyBluetoothReady())
                .thenReturn(BluetoothConnection.BLUETOOTH_OFFLINE);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(mockActivity, Mockito.times(1))
                .showEnableBluetoothDialog(Matchers.anyString());
    }

    @Test
    public void testGetBtActionReceiver() {
    }

    @After
    public void tearDown() {
        this.mockActivity = null;
        this.btAdapterMock = null;
        this.filterActionMock = null;
        this.deviceComm = null;
    }
}