package org.unbiquitous.uos.network.socket.connectionManager;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.InitialProperties.Tuple;
import org.unbiquitous.uos.core.network.cache.CacheController;
import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.TCPProperties;
import org.unbiquitous.uos.network.socket.channelManager.TCPChannelManager;
import org.unbiquitous.uos.network.socket.connection.TCPServerConnection;

import br.unb.cic.ethutil.EthUtilNetworkInterfaceHelper;


/**
 * Manage the ubiquitos-smartspace service interface.
 *
 * @author Passarinho
 */
public class TCPConnectionManager extends SocketConnectionManager{
	
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
	private TCPProperties properties;
	
	private int port = 14984;
	private Tuple<Integer, Integer> passivePortRange = new Tuple<Integer, Integer>(14985, 14990);
	
    private static final Logger logger = UOSLogging.getLogger();

    private ConnectionManagerListener connectionManagerListener = null;
    
    private SocketDevice serverDevice;
    private TCPServerConnection server;
    
    private boolean closingEthernetConnectionManager = false;
    
    private TCPChannelManager channelManager;
    private NetworkInterfaceProvider interfaceProvider = new NetworkInterfaceProvider();
    private CacheController cacheController = new CacheController();
    
	private String ignoreFilter;
    
	public void setConnectionManagerListener(ConnectionManagerListener connectionManagerListener) {
		this.connectionManagerListener = connectionManagerListener;
	}

	public static class NetworkInterfaceProvider {
		public String[] interfaces() throws IOException{
			return EthUtilNetworkInterfaceHelper.listLocalAddresses();
		}
	}
	
	public void setNetworkInterfaceProvider(
			NetworkInterfaceProvider interfaceProvider) {
		this.interfaceProvider = interfaceProvider;
	}
	
	public CacheController getCacheController() {
		return cacheController;
	}
	
	/** 
     *  Sets the ResourceBundle to get some properties.
     */
	public void init(InitialProperties _properties) {
		if(_properties instanceof TCPProperties){
			properties = (TCPProperties) _properties;
		}else{
			properties = new TCPProperties(_properties);
		}
		
		if(properties == null){
        	String msg = "ResourceBundle is null";
        	logger.severe(msg);
            throw new RuntimeException(msg);
        }else{
        	try{
        		if(properties.getPort() != null){
        			port = properties.getPort();
        		}
        		if(properties.getPassivePortRange() != null){
        			passivePortRange = properties.getPassivePortRange();
        		}
        		ignoreFilter = properties.getString("ubiquitos.eth.tcp.ignoreFilter");
        	}catch (Exception e) {
        		String msg = "Incorrect ethernet tcp port";
            	logger.severe(msg);
                throw new RuntimeException(msg);
			}
        }
	}
	
	public InitialProperties getProperties(){
		return this.properties;
	}
	/**
	 * Finalize the Connection Manager.
	 */
	public void tearDown(){
		try {
			closingEthernetConnectionManager = true;
			logger.fine("Closing Ethernet TCP Connection Manager...");
			server.closeConnection();
			if(channelManager != null){
				channelManager.tearDown();
			}
			logger.fine("Ethernet TCP Connection Manager is closed.");
		} catch (Exception ex) {
			closingEthernetConnectionManager = false;
			String msg = "Error closing Ethernet TCP Connection Manager. ";
            logger.log(Level.SEVERE,msg, ex);
            throw new RuntimeException(msg + ex);
		}
	}
    
	/* *****************************
     *   	PUBLIC  METHODS - ConnectionManager/Runnable
     * *****************************/
	
	/**
	 * A method for retrieve the networkDevice of this connection.
	 * @return networkDevice
	 */
	public NetworkDevice getNetworkDevice() {
		if(serverDevice == null){
			try {
				return createCurrentDevice();
			} catch (IOException e) {
				logger.log(Level.SEVERE,"",e);
			}
		}
		logger.fine("returning:"+serverDevice);
		return serverDevice;
	}

	private NetworkDevice createCurrentDevice() throws IOException {
		String[] localAddrs = interfaceProvider.interfaces();
		boolean hasAdresses = localAddrs != null && localAddrs.length > 0;
		if(hasAdresses){
			String addr = selectAddress(localAddrs);
			serverDevice = new SocketDevice(addr, port, EthernetConnectionType.TCP);
			return serverDevice;
		}else{
			throw new NetworkException("No network available");
		}
	}

	private String selectAddress(String[] localAddrs) {
		if (ignoreFilter == null){
			return localAddrs[0];
		}else{
			return applyIgnoreFilter(localAddrs);
		}
	}

	private String applyIgnoreFilter(String[] localAddrs) {
		for(String nInt :localAddrs){
			if(!nInt.matches(ignoreFilter)){
				return nInt;
			}
		}
		return null;
	}
	
	/**
	 * A method for retrive the channel manager of this connection manager
	 * @return channel managar
	 */
	public ChannelManager getChannelManager(){
		if(channelManager == null){
			channelManager = new TCPChannelManager(port, passivePortRange.x, passivePortRange.y, cacheController);
		}
		return channelManager;
	}
    
	/**
	 * Method extends from Runnable. Starts the connection Manager
	 */
    public void run() {
    	logger.fine("Starting UbiquitOS Smart-Space Ethernet TCP Connection Manager.");
        logger.info("Starting Ethernet TCP Connection Manager...");
        
        int tries = 0;
        int max_retries = 5;
        long waiting_time = 300;
        IOException ex = null;
        while (tries < max_retries){
			try {
				server = new TCPServerConnection((SocketDevice)getNetworkDevice(), cacheController);
				tries = max_retries;
			} catch (IOException e) {
				String msg = "Error starting Ethernet TCP Connection Manager : "+e.getMessage();
	            logger.severe(msg);
	            ex = e;
	            tries ++;
	            try {
					Thread.sleep(waiting_time);
				} catch (InterruptedException e1) {
					logger.log(Level.SEVERE,"",e1);
				}
			}
        }
		
        if (server == null){
        	String msg = "Error starting Ethernet TCP Connection Manager. ";
            logger.log(Level.SEVERE,msg, ex);
            throw new RuntimeException(msg,ex);
        }
        
        logger.info("Ethernet TCP Connection Manager is started.");
        
        while (true) {
    		try {
    			connectionManagerListener.handleClientConnection(server.accept());
    		} catch (IOException e) {
    			if(!closingEthernetConnectionManager){
    				String msg = "Error starting Ethernet TCP Connection Manager. ";
                    logger.log(Level.SEVERE,msg, e);
                    throw new RuntimeException(msg + e);
    			}else{
    				return;
    			}
    		}
		}
    }
    
}



