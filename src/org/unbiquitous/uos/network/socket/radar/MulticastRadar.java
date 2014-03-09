package org.unbiquitous.uos.network.socket.radar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.radar.Radar;
import org.unbiquitous.uos.core.network.radar.RadarListener;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.connectionManager.SocketConnectionManager.EthernetConnectionType;

public class MulticastRadar implements Radar {

	private static final Logger logger = UOSLogging.getLogger();
	
	protected DatagramSocketFactory socketFactory = new DatagramSocketFactory();
	private Boolean running = false;

	private final RadarListener listener;
	private Set<String> knownAddresses;

	private DateTime lastCheck;
	private Set<String> lastAddresses;

	private DatagramSocket socket;

	private Integer port = 14984;
	
	public MulticastRadar(RadarListener listener) {
		this.listener = listener;
		this.knownAddresses = new HashSet<String>();
	}
	
	@Override
	public void run() {
		try {
			this.lastCheck = new DateTime();
			this.lastAddresses = new HashSet<String>();
			socket = socketFactory.newSocket(port);
			int tenSeconds = 10*1000;
			socket.setSoTimeout(tenSeconds);
			sendBeacon(socket, InetAddress.getByName("255.255.255.255"), port);
			while(running){
				receiveAnswers(port, socket);
				checkLeftDevices(port, socket);
			}
			socket.close();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Problems running the radar.", e);
		}
		
	}

	private void checkLeftDevices(Integer port, DatagramSocket socket) throws IOException {
		DateTime now = new DateTime();
		if (Seconds.secondsBetween(lastCheck, now).getSeconds() > 30){
			sendBeacon(socket, InetAddress.getByName("255.255.255.255"), port);
			lastAddresses.removeAll(knownAddresses);
			for(String address: lastAddresses){
				SocketDevice left = new SocketDevice(address,port, 
						EthernetConnectionType.TCP);
				listener.deviceLeft(left);
			}
			lastAddresses = knownAddresses;
			knownAddresses =  new HashSet<String>();
			lastCheck = now;
		}
	}

	private void sendBeacon(DatagramSocket socket, InetAddress address, Integer port)
			throws UnknownHostException, IOException {
		socket.send(new DatagramPacket(new byte[]{1}, 1, address, port));
	}

	private void receiveAnswers(Integer port, DatagramSocket socket) throws IOException {
		try {
			DatagramPacket packet = new DatagramPacket(new byte[1], 1);
			socket.receive(packet);
			handleBeacon(port, socket, packet);
		} catch (SocketTimeoutException e) {
			// Timeout is expected to happen.
		}
	}

	private void handleBeacon(Integer port, DatagramSocket socket,
			DatagramPacket packet) throws UnknownHostException, IOException {
		if (packet.getAddress() != null){
			String address = packet.getAddress().getHostAddress();
			if (!knownAddresses.contains(address)){
				//TODO: rename this class to SocketDevice
				logger.info(String.format("Entered device %s.", address));
				SocketDevice found = new SocketDevice(address,port, 
						EthernetConnectionType.TCP);
				listener.deviceEntered(found);
				sendBeacon(socket, packet.getAddress(), port);
				knownAddresses.add(address);
			}
		}
	}

	@Override
	public void setConnectionManager(ConnectionManager manager) {
		InitialProperties properties = manager.getProperties();
		if (properties.containsKey("ubiquitos.eth.tcp.port")){
			String portStr = properties.getString("ubiquitos.eth.tcp.port");
			port = Integer.valueOf(portStr);
		}
	}

	@Override
	public void startRadar() {
		running = true;
	}

	@Override
	public void stopRadar() {
		running = false;
	}

}

class DatagramSocketFactory{
	DatagramSocket newSocket() throws SocketException{
		return new DatagramSocket();
	}
	DatagramSocket newSocket(int port) throws SocketException{
		return new DatagramSocket(port);
	}
}