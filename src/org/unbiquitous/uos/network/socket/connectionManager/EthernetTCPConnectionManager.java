package org.unbiquitous.uos.network.socket.connectionManager;


import java.io.IOException;
import java.net.EthUtilNetworkInterfaceHelper;
import java.net.SocketException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.cache.CacheController;
import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.network.socket.EthernetDevice;
import org.unbiquitous.uos.network.socket.channelManager.EthernetTCPChannelManager;
import org.unbiquitous.uos.network.socket.connection.EthernetTCPServerConnection;


/**
 * Manage the ubiquitos-smartspace service interface.
 *
 * @author Passarinho
 */
public class EthernetTCPConnectionManager extends EthernetConnectionManager{
	
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
	/** The ResourceBundle to get some properties. */
	private ResourceBundle resource;
	
	/** Specify the number of the ethernet port to be used*/
	private static final String UBIQUITOS_ETH_TCP_PORT_KEY = "ubiquitos.eth.tcp.port";
	private static final String UBIQUITOS_ETH_TCP_CONTROL_PORT_KEY = "ubiquitos.eth.tcp.port.control";
	private int UBIQUITOS_ETH_TCP_PORT;
	private int UBIQUITOS_ETH_TCP_CONTROL_PORT;
	
	/** Specify the passive port range to be used*/
	private static final String UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE_KEY = "ubiquitos.eth.tcp.passivePortRange";
	String UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE;
	
    /** Object for logging registration.*/
    private static final Logger logger = UOSLogging.getLogger();

    /** A Connection Manager Listener (ConnectionManagerControlCenter) */
    private ConnectionManagerListener connectionManagerListener = null;
    
    /** Server Connection */
    private EthernetDevice serverDevice;
    private EthernetTCPServerConnection server;
    
    /** Attribute to control the closing of the Connection Manager */
    private boolean closingEthernetConnectionManager = false;
    
    /** The ChannelManager for new channels */
    private EthernetTCPChannelManager channelManager;
    
    /**
     * Controller responsible for the active connections cache. 
     */
    private CacheController cacheController = new CacheController();
    
    /* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
    /**
	 * Constructor
	 * @throws UbiquitOSException
	 */
    public EthernetTCPConnectionManager() throws NetworkException {}
    
    
    /* *****************************
     *   	PUBLIC  METHODS - ConnectionManager
     * *****************************/

    /** 
     *  Sets the Listener who will be notified when a Connections is established.
     */
	public void setConnectionManagerListener(ConnectionManagerListener connectionManagerListener) {
		this.connectionManagerListener = connectionManagerListener;
	}
	
	/** 
     *  Sets the ResourceBundle to get some properties.
     */
	public void setResourceBundle(ResourceBundle resourceBundle) {
		resource = resourceBundle;
		
		if(resource == null){
        	String msg = "ResourceBundle is null";
        	logger.severe(msg);
            throw new RuntimeException(msg);
        }else{
        	try{
        		UBIQUITOS_ETH_TCP_PORT = Integer.parseInt(resource.getString(UBIQUITOS_ETH_TCP_PORT_KEY));
        		try {
					UBIQUITOS_ETH_TCP_CONTROL_PORT = Integer.parseInt(resource.getString(UBIQUITOS_ETH_TCP_CONTROL_PORT_KEY));
				} catch (Exception e) {
					logger.info("No Alternative TCP Port defined");
				}
        		UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE = resource.getString(UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE_KEY);
        	}catch (Exception e) {
        		String msg = "Incorrect ethernet tcp port";
            	logger.severe(msg);
                throw new RuntimeException(msg);
			}
        }
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
				String addr = EthUtilNetworkInterfaceHelper.listLocalAddresses()[0];
				serverDevice = new EthernetDevice(addr, UBIQUITOS_ETH_TCP_PORT, EthernetConnectionType.TCP);
				return serverDevice;
			} catch (SocketException e) {
				logger.log(Level.SEVERE,"",e);
			}
		}
		logger.fine("returning:"+serverDevice);
		return serverDevice;
	}
	
	/**
	 * A method for retrive the channel manager of this connection manager
	 * @return channel managar
	 */
	public ChannelManager getChannelManager(){
		if(channelManager == null){
			channelManager = new EthernetTCPChannelManager(UBIQUITOS_ETH_TCP_PORT, UBIQUITOS_ETH_TCP_CONTROL_PORT, UBIQUITOS_ETH_TCP_PASSIVE_PORT_RANGE, cacheController);
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
				server = new EthernetTCPServerConnection((EthernetDevice)getNetworkDevice(), cacheController);
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



