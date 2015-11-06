package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BluetoothAdapter.class)
public class TestPrepareDeviceCommunication {

    PrepareDeviceCommunication deviceComm;
    MainActivity mockActivity;
    BluetoothAdapter btAdapterMock;
    IntentFilter filterActionMock;

    @Before
    public void setUp() {
        mockActivity = Mockito.mock(MainActivity.class);
        PowerMockito.mockStatic(BluetoothAdapter.class);
        btAdapterMock = Mockito.mock(BluetoothAdapter.class);
        filterActionMock = Mockito.mock(IntentFilter.class);
  //      deviceComm = new PrepareDeviceCommunication(mockActivity,
    //            btAdapterMock, filterActionMock);
    }

   /* @Test
    public void testEstablishBluetoothConnection() {
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(false);
        int _return = deviceComm.establishBluetoothConnection();
        Assert.assertEquals(PrepareDeviceCommunication.BLUETOOTH_OFFLINE, _return);

        Mockito.when(btAdapterMock.isEnabled()).thenReturn(true);
        _return = deviceComm.establishBluetoothConnection();
        Assert.assertEquals(PrepareDeviceCommunication.BLUETOOTH_ONLINE, _return);

        deviceComm = new PrepareDeviceCommunication(mockActivity, null, filterActionMock);
        _return = deviceComm.establishBluetoothConnection();
        Assert.assertEquals(PrepareDeviceCommunication.NOT_HAVE_BLUETOOTH, _return);
    }*/

    @Test
    public void testGetBtActionReceiver() {
     //   Assert.assertNotNull(deviceComm.getBtActionReceiver());
    }

    @After
    public void tearDown() {
        mockActivity = null;
        btAdapterMock = null;
        filterActionMock = null;
        deviceComm = null;
    }
}