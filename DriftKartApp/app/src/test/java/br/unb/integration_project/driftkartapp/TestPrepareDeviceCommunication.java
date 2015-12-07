package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
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
    public void testEstablishBluetoothConnection() {
        BluetoothConnection btConnection = Mockito.mock(BluetoothConnection.class);
        deviceComm.setAttributesForUnitTest(btConnection, new DataFlowHandling(mockActivity, btConnection, Mockito.mock(Handler.class)));
        Mockito.when(btConnection.verifyBluetoothReady()).thenReturn(BluetoothConnection.NOT_HAVE_BLUETOOTH);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(mockActivity, Mockito.times(1)).showLongToastDialog(Matchers.anyString());
        Mockito.when(btConnection.verifyBluetoothReady()).thenReturn(BluetoothConnection.BLUETOOTH_OFFLINE);
        deviceComm.establishBluetoothConnection();
        Mockito.verify(mockActivity, Mockito.times(1)).showEnableBluetoothDialog(Matchers.anyString());
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