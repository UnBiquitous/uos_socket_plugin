package org.unbiquitous.uos.network.socket.channelManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.cache.CacheController;
import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.connection.TCPClientConnection;
import org.unbiquitous.uos.network.socket.connection.TCPServerConnection;
import org.unbiquitous.uos.network.socket.connectionManager.SocketConnectionManager.EthernetConnectionType;


public class TCPChannelManager implements ChannelManager{ 
	
	private static final Logger logger = UOSLogging.getLogger();
	
	/*********************************
	 * ATTRIBUTES
	 *********************************/
	
	private List<NetworkDevice> freePassiveDevices;
	
	private Map<String, TCPServerConnection> startedServers;
	
	private int defaultPort;
	
	/**
     * Controller responsible for the active connections cache. 
     */
    private CacheController cacheController;

	private List<Integer> validPorts;
	
	/*********************************
	 * CONSTRUCTORS
	 * @param cacheController 
	 *********************************/
	
	public TCPChannelManager(int defaultPort ,int portRangestart, int portRangeEnd, CacheController cacheController){
		
		this.defaultPort = defaultPort;
		this.cacheController = cacheController;
		this.startedServers = new HashMap<String, TCPServerConnection>();
		
		freePassiveDevices = new ArrayList<NetworkDevice>();
		validPorts = new ArrayList<Integer>();
		validPorts.add(defaultPort);
		for(int port = portRangestart; port <= portRangeEnd; port++){
			validPorts.add(port);
			freePassiveDevices.add(new SocketDevice("0.0.0.0",port,EthernetConnectionType.TCP));
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
			port = defaultPort;
		}else if(address.length == 2){
			port = Integer.parseInt(address[1]);
		}else{
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
    	host = address[0];
    	
    	if (!validPorts.contains(port) ){
    		port = defaultPort;
    	}
    	
    	ClientConnection cached = cacheController.getConnection(host+':'+port);
		if (cached != null){
			logger.info("EthernetTCPChannelManager: openActiveConnection: Returning cached connection for host '"+host+"'\n\n"); 
			return cached;
		}
    	
		try {
			return new TCPClientConnection(host, port, cacheController);
		} catch (Exception e) {
			return null;
		}
	}
	
	
	public ClientConnection openPassiveConnection(String networkDeviceName) throws NetworkException, IOException{
		String[] address = networkDeviceName.split(":");
		
		if(address.length != 2){
			throw new NetworkException("Invalid parameters for creation of the channel.");
		}
		
		TCPServerConnection server = startedServers.get(networkDeviceName);
		if(server == null){
			String host = address[0];
	    	int port = Integer.parseInt(address[1]);
			// Passive (Stream) connections shouldn't be cached
	    	server = new TCPServerConnection(new SocketDevice(host, port, EthernetConnectionType.TCP), null);
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
		for(TCPServerConnection server : startedServers.values()){
			server.closeConnection();
		}
	}
}
