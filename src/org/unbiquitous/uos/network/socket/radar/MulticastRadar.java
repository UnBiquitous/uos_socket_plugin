package org.unbiquitous.uos.network.socket.radar;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.radar.Radar;
import org.unbiquitous.uos.core.network.radar.RadarListener;
import org.unbiquitous.uos.network.socket.EthernetDevice;
import org.unbiquitous.uos.network.socket.connectionManager.EthernetConnectionManager.EthernetConnectionType;

public class MulticastRadar implements Radar {

	private static final Logger logger = UOSLogging.getLogger();
	
	protected DatagramSocketFactory socketFactory = new DatagramSocketFactory();
	private Boolean running = false;

	private final RadarListener listener;
	private Set<String> knownAddresses;
	
	public MulticastRadar(RadarListener listener) {
		this.listener = listener;
		this.knownAddresses = new HashSet<String>();
	}
	
	@Override
	public void run() {
		Integer port = 14984;
		try {
			DatagramSocket socket = socketFactory.newSocket(port);
			sendBeacon(socket, InetAddress.getByName("255.255.255.255"), port);
			while(running){
				receiveAnswers(port, socket);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Problems running the radar.", e);
		}
		
	}

	private void sendBeacon(DatagramSocket socket, InetAddress address, Integer port)
			throws UnknownHostException, IOException {
		socket.send(new DatagramPacket(new byte[]{1}, 1, address, port));
	}

	private void receiveAnswers(Integer port, DatagramSocket socket)
			throws IOException {
		DatagramPacket packet = new DatagramPacket(new byte[1], 1);
		socket.receive(packet);
		if (packet.getAddress() != null){
			String address = packet.getAddress().getHostAddress();
			if (!knownAddresses.contains(address)){
				//TODO: rename this class to SocketDevice
				EthernetDevice found = new EthernetDevice(address,port, 
						EthernetConnectionType.TCP);
				listener.deviceEntered(found);
				sendBeacon(socket, packet.getAddress(), port);
				knownAddresses.add(address);
			}
		}
	}

	@Override
	public void setConnectionManager(ConnectionManager arg0) {
		// TODO Auto-generated method stub
		
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