package org.unbiquitous.uos.network.socket.radar;

import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

import java.util.Vector;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.radar.RadarListener;

public class EthernetPingRadarTest {

	
	//TODO: Null breaks
	
	private RadarListener listener;
	private EthernetPingRadar radar;

	@Before public void setUp(){
		listener = mock(RadarListener.class);
		radar = new EthernetPingRadar(listener);
	}
	
	@Test public void notfyLostDevices(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		radar.deviceDiscoveryFinished(new Vector<String>());
		
		ArgumentCaptor<NetworkDevice> d = captor(NetworkDevice.class); 
		verify(listener).deviceLeft(d.capture());
		
		assertThat(d.getAllValues()).hasSize(1);
		assertThat(d.getValue().getNetworkDeviceName()).isEqualTo("1.2.3.4:14984");
	}
	
	@Test public void notfyLostDevicesAfterASuccessfullDiscovery(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		
		discovered.clear();
		discovered.add("4.3.2.1");
		
		radar.deviceDiscoveryFinished(discovered);
		
		ArgumentCaptor<NetworkDevice> d = captor(NetworkDevice.class); 
		verify(listener).deviceLeft(d.capture());
		
		assertThat(d.getAllValues()).hasSize(1);
		assertThat(d.getValue().getNetworkDeviceName()).isEqualTo("1.2.3.4:14984");
	}
	
	@Test public void ethutilCanSendDuplicates(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		discovered.add("4.3.2.1");
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		
		discovered.clear();
		discovered.add("4.3.2.1");
		
		radar.deviceDiscoveryFinished(discovered);
		
		ArgumentCaptor<NetworkDevice> d = captor(NetworkDevice.class); 
		verify(listener).deviceLeft(d.capture());
		
		assertThat(d.getAllValues()).hasSize(1);
		assertThat(d.getValue().getNetworkDeviceName()).isEqualTo("1.2.3.4:14984");
	}

	@Test public void notfyNobodyWhenThereIsNoChange(){
		Vector<String> discovered = new Vector<String>();
		discovered.add("1.2.3.4");
		
		radar.deviceDiscoveryFinished(discovered);
		radar.deviceDiscoveryFinished(discovered);
		
		verify(listener,never()).deviceLeft((NetworkDevice)any());
	}
	
	@Test public void shouldNotBreakOnNull(){
		radar.deviceDiscoveryFinished(null);
		verify(listener,never()).deviceLeft((NetworkDevice)any());
	}
	
	private <T> ArgumentCaptor<T> captor(Class<T> clazz) {
		return ArgumentCaptor.forClass(clazz);
	}
	
}
