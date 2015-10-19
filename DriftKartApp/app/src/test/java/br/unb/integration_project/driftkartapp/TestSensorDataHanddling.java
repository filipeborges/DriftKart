package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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
public class TestSensorDataHanddling {

    PrepareDeviceCommunication sensorDataHanddling;
    Context mockContext;
    BluetoothAdapter btAdapterMock;
    IntentFilter filterActionMock;

    @Before
    public void setUp() {
        mockContext = Mockito.mock(Context.class);
        PowerMockito.mockStatic(BluetoothAdapter.class);
        btAdapterMock = Mockito.mock(BluetoothAdapter.class);
        filterActionMock = Mockito.mock(IntentFilter.class);
        sensorDataHanddling = new PrepareDeviceCommunication(mockContext,
                btAdapterMock, filterActionMock);
    }

    @Test
    public void testEstablishBluetoothConnection() {
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(false);
        int _return = sensorDataHanddling.establishBluetoothConnection();
        Assert.assertEquals(PrepareDeviceCommunication.BLUETOOTH_OFFLINE, _return);

        Mockito.when(btAdapterMock.isEnabled()).thenReturn(true);
        _return = sensorDataHanddling.establishBluetoothConnection();
        Assert.assertEquals(PrepareDeviceCommunication.BLUETOOTH_ONLINE, _return);

        sensorDataHanddling = new PrepareDeviceCommunication(mockContext, null, filterActionMock);
        _return = sensorDataHanddling.establishBluetoothConnection();
        Assert.assertEquals(PrepareDeviceCommunication.NOT_HAVE_BLUETOOTH, _return);
    }

    @Test
    public void testGetBtActionReceiver() {
        Assert.assertNotNull(sensorDataHanddling.getBtActionReceiver());
    }

    @After
    public void tearDown() {
        mockContext = null;
        btAdapterMock = null;
        filterActionMock = null;
        sensorDataHanddling = null;
    }
}