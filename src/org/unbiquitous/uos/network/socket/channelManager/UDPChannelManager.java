package org.unbiquitous.uos.network.socket.channelManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.connection.UDPClientConnection;
import org.unbiquitous.uos.network.socket.connection.UDPServerConnection;
import org.unbiquitous.uos.network.socket.connectionManager.SocketConnectionManager.EthernetConnectionType;


public class UDPChannelManager implements ChannelManager{
	
	/*********************************
	 * ATTRIBUTES
	 *********************************/

	private List<NetworkDevice> freePassiveDevices;
	
	private Map<String, UDPServerConnection> startedServers;
	
	private int defaultPort;
	private int controlPort;
	
	
	/*********************************
	 * CONSTRUCTORS
	 *********************************/
	
	public UDPChannelManager(int defaultPort, int controlPort, String portRange){
		
		this.defaultPort = defaultPort;
		this.controlPort = controlPort;
		
		this.startedServers = new HashMap<String, UDPServerConnection>();
		
		freePassiveDevices = new ArrayList<NetworkDevice>();
		String[] limitPorts = portRange.split("-");
		int inferiorPort = Integer.parseInt(limitPorts[0]);
		int superiorPort = Integer.parseInt(limitPorts[1]);
		for(int port = inferiorPort; port <= superiorPort; port++){
			freePassiveDevices.add(new SocketDevice("0.0.0.0",port,EthernetConnectionType.UDP));
		}
	}
	
	/********************************
	 * PUBLIC METHODS
	 ********************************/
	
	public ClientConnection openActiveConnection(String networkDeviceName) throws NetworkException, IOException{
		String[] address = networkDeviceName.split(":");
		
		String host ;
		int port ;
		if (address.length == 1){
			port = controlPort;
		}else if(address.length == 2){
			port = Integer.parseInt(address[1]);
		}else{
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
    	host = address[0];
    	
    	UDPClientConnection etcc ;
		try {
			etcc = new UDPClientConnection(host, port);
		} catch (Exception e) {
			etcc = new UDPClientConnection(host, defaultPort);
		}
		return etcc;
	}

	public ClientConnection openPassiveConnection(String networkDeviceName) throws NetworkException, IOException{
		String[] address = networkDeviceName.split(":");
		
		if(address.length != 2){
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
		UDPServerConnection server = startedServers.get(networkDeviceName);
		if(server == null){
			String host = address[0];
	    	int port = Integer.parseInt(address[1]);
			
	    	server = new UDPServerConnection(new SocketDevice(host, port, EthernetConnectionType.UDP));
	    	startedServers.put(networkDeviceName, server);
		}
		
		return server.accept();
	}
	
	
	public NetworkDevice getAvailableNetworkDevice(){
		NetworkDevice networkDevice = freePassiveDevices.remove(0);
		freePassiveDevices.add(networkDevice);
		return networkDevice;
	}
	
	
	public void tearDown() throws NetworkException, IOException {
		for(UDPServerConnection server : startedServers.values()){
			server.closeConnection();
		}
	}

}
