package br.unb.integration_project.driftkartapp;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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
public class TestBluetoothMonitor {

    BluetoothMonitor btMonitor;
    Context mockContext;
    BluetoothAdapter btAdapterMock;

    @Before
    public void setUp() {
        btAdapterMock = PowerMockito.mock(BluetoothAdapter.class);
        Mockito.when(btAdapterMock.isEnabled()).thenReturn(true);
        mockContext = Mockito.mock(Context.class);
        btMonitor = new BluetoothMonitor(mockContext, btAdapterMock);
    }

    @Test
    public void testStartDiscovery() {
        btMonitor.startDeviceDiscovery();
        Mockito.verify(btAdapterMock, Mockito.times(1)).startDiscovery();
    }

    @Test
    public void testCancelDiscovery() {
        btMonitor.cancelDeviceDiscovery();
        Mockito.verify(btAdapterMock, Mockito.times(1)).cancelDiscovery();
    }

    @After
    public void tearDown() {
        btAdapterMock = null;
        mockContext = null;
        btMonitor = null;
    }
}
