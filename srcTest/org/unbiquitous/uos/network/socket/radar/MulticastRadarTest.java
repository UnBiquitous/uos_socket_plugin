package org.unbiquitous.uos.network.socket.radar;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.radar.Radar;
import org.unbiquitous.uos.core.network.radar.RadarListener;
import org.unbiquitous.uos.network.socket.SocketDevice;

public class MulticastRadarTest {

	private static final String THREAD_NAME = "radar-t";
	private Integer port;
	private MulticastRadar radar;
	private DatagramSocket serverSocket;
	private DatagramSocketFactory factory;
	private RadarListener listener;

	@Before public void setUp() throws Exception{
		port = 14984;
		//TODO: port is not used
		ResourceBundle bundle = new ListResourceBundle() {
			protected Object[][] getContents() {
				return new Object[][] {
						{ "ubiquitos.eth.tcp.port", port.toString() }
				}; 
			}
		};
		listener = mock(RadarListener.class);
		radar = new MulticastRadar(listener);
		mockSockets();
	}

	private void mockSockets() throws SocketException {
		serverSocket = mock(DatagramSocket.class);
		factory = mock(DatagramSocketFactory.class);
		when(factory.newSocket(port)).thenReturn(serverSocket);
		radar.socketFactory = factory;
	}
	
	@After public void tearDown(){
		radar.stopRadar();
		DateTimeUtils.setCurrentMillisSystem();
	}
	
	@Test public void mustBeARadarWithAProperFactory(){
		radar = new MulticastRadar(null);
		assertThat(radar).isInstanceOf(Radar.class);
		assertThat(radar.socketFactory)
			.isNotNull()
			.isInstanceOf(DatagramSocketFactory.class);
	}
	
	@Test public void listenToThePortSpecifyedWithA10sTimeout() throws Throwable{
		run();
		assertEventually(1000, new Runnable() {
			public void run() {
				try {
					verify(factory).newSocket(port);
					verify(serverSocket,times(1)).setSoTimeout(10*1000);
				} catch (SocketException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	@Test public void waitForDeviceToNotifyItsExistence() throws Exception{
		run();
		
		ArgumentCaptor<DatagramPacket> arg = forClass(DatagramPacket.class); 
		verify(serverSocket,atLeastOnce()).receive(arg.capture());
		assertThat(arg.getValue()).isNotNull();
	}
	
	@Test public void doesNotFailOnATimeout() throws Exception{
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation)throws Throwable {
				throw new SocketTimeoutException("Test exception");
			}
		}).when(serverSocket).receive((DatagramPacket)any());
		run();
	}
	
	@Test public void startAndStopMustControlTheNumberOfThreads() throws Throwable{
		run();
		assertEventually(1000, new Runnable() {
			public void run() {
				Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
				for (Thread t : allStackTraces.keySet()){
					if(t.getName().equals(THREAD_NAME)){
						return;
					}
				}
				throw new AssertionError("Thread "+THREAD_NAME+" not found");
			}
		});
		
		radar.stopRadar();
		assertEventually(1000, new Runnable() {
			public void run() {
				Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
				for (Thread t : allStackTraces.keySet()){
					if(t.getName().equals(THREAD_NAME)){
						throw new AssertionError("Thread "+THREAD_NAME+" not expected");
					}
				}
			}
		});
	}
	
	@Test public void sendsABroadcastBeaconAtStartup() throws Throwable{
		run();
		final ArgumentCaptor<DatagramPacket> arg = forClass(DatagramPacket.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				try {
					verify(serverSocket,times(1)).send(arg.capture());
					DatagramPacket beacon = arg.getValue();
					assertThat(beacon.getAddress().getHostAddress())
						.isEqualTo("255.255.255.255");
					assertThat(beacon.getPort())
						.isEqualTo(port);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	@Test public void whenSomebodySendsABeaconNotifiesItsDiscoveryOnce() throws Throwable{
		mockADeviceEntry("1.1.1.1","2.2.2.2","3.3.3.3");
		run();
		final ArgumentCaptor<NetworkDevice> arg = forClass(NetworkDevice.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				verify(listener,times(3)).deviceEntered(arg.capture());
				NetworkDevice device = arg.getValue();
				assertThat(device).isInstanceOf(SocketDevice.class);
				NetworkDevice device1 = arg.getAllValues().get(0);
				assertThat(device1.getNetworkDeviceName())
												.isEqualTo("1.1.1.1"+":"+port);
				NetworkDevice device2 = arg.getAllValues().get(1);
				assertThat(device2.getNetworkDeviceName())
												.isEqualTo("2.2.2.2"+":"+port);
				NetworkDevice device3 = arg.getAllValues().get(2);
				assertThat(device3.getNetworkDeviceName())
												.isEqualTo("3.3.3.3"+":"+port);
			}
		});
	}

	@Test public void sendsADirectResponseBeaconWhenSomebodyIsFound() throws Throwable{
		mockADeviceEntry("1.1.1.1");
		run();
		final ArgumentCaptor<DatagramPacket> arg = forClass(DatagramPacket.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				try {
					verify(serverSocket,times(2)).send(arg.capture());
					DatagramPacket beacon = arg.getAllValues().get(1);
					assertThat(beacon.getAddress().getHostAddress())
						.isEqualTo("1.1.1.1");
					assertThat(beacon.getPort())
						.isEqualTo(port);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	//TODO: How to detect device left ?
	
	@Test public void checkForLeftDevicesEvery30seconds() throws Throwable{
		final List<String> enteredAddress = new ArrayList<String>(){
			{
				add("1.1.1.1");add("2.2.2.2");add("3.3.3.3");
			}
		};
		 
		doAnswer(new Answer<Void>() {
			int index = 0;
			public Void answer(InvocationOnMock invocation)
					throws Throwable {
				String addr = enteredAddress.get(index++ % enteredAddress.size() );
				Object[] args = invocation.getArguments();
				DatagramPacket packet = (DatagramPacket) args[0];
				packet.setAddress(InetAddress.getByName(addr));
				return null;
			}
		}).when(serverSocket).receive((DatagramPacket)any());
		
		run();
		Thread.sleep(10);
		enteredAddress.remove("2.2.2.2");
		
		final ArgumentCaptor<NetworkDevice> arg = forClass(NetworkDevice.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				DateTimeUtils.setCurrentMillisFixed(new DateTime().plusSeconds(30).getMillis());
				verify(listener,times(1)).deviceLeft(arg.capture());
				NetworkDevice device = arg.getValue();
				assertThat(device.getNetworkDeviceName()).isEqualTo("2.2.2.2:"+port);
			}
		});
	}
	
	@Test public void sendsABeaconEvery30seconds() throws Throwable{
		run();
		final ArgumentCaptor<DatagramPacket> arg = forClass(DatagramPacket.class);
		assertEventually(1000, new Runnable() {
			public void run() {
				try {
					DateTimeUtils.setCurrentMillisFixed(new DateTime().plusSeconds(30).getMillis());
					verify(serverSocket,times(2)).send(arg.capture());
					DatagramPacket beacon = arg.getAllValues().get(1);
					assertThat(beacon.getAddress().getHostAddress())
						.isEqualTo("255.255.255.255");
					assertThat(beacon.getPort())
						.isEqualTo(port);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	private void mockADeviceEntry(final String ... enteredAddress) throws IOException {
		doAnswer(new Answer<Void>() {
			int index = 0;
			public Void answer(InvocationOnMock invocation)
					throws Throwable {
				String addr = enteredAddress[index++ % enteredAddress.length ];
				Object[] args = invocation.getArguments();
				DatagramPacket packet = (DatagramPacket) args[0];
				packet.setAddress(InetAddress.getByName(addr));
				return null;
			}
		}).when(serverSocket).receive((DatagramPacket)any());
	}
	
	private void run(){
		radar.startRadar(); //TODO: why ?
		Thread t = new Thread(radar,THREAD_NAME);
		t.start();
	}
	
	private void assertEventually(int timeoutInMilliseconds, Runnable assertion) throws Throwable{
		long begin = System.currentTimeMillis();
		long now = begin;
		Throwable lastException = null;
		do{
			try{
				assertion.run();
				return;
			}catch(RuntimeException e){
				lastException = e;
			}catch(AssertionError e){
				lastException = e;
			}
			now = System.currentTimeMillis(); 
		}while((now - begin) < timeoutInMilliseconds);
		throw lastException;
	}
}
