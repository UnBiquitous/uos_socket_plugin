package org.unbiquitous.uos.network.socket.connectionManager;

import java.io.IOException;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.network.socket.SocketDevice;
import org.unbiquitous.uos.network.socket.channelManager.UDPChannelManager;
import org.unbiquitous.uos.network.socket.connection.UDPServerConnection;
import org.unbiquitous.uos.network.socket.udp.UdpChannel;

import br.unb.cic.ethutil.EthUtilNetworkInterfaceHelper;


public class UDPConnectionManager extends SocketConnectionManager{
	
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
	/** The ResourceBundle to get some properties. */
	private InitialProperties properties;
	
	/** Specify the number of the ethernet port to be used*/
	private static final String UBIQUITOS_ETH_UDP_PORT_KEY = "ubiquitos.eth.udp.port";
	private static final String UBIQUITOS_ETH_UDP_CONTROL_PORT_KEY = "ubiquitos.eth.udp.port.control";
	private int UBIQUITOS_ETH_UDP_PORT;
	private int UBIQUITOS_ETH_UDP_CONTROL_PORT;
	
	/** Specify the passive port range to be used*/
	private static final String UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE_KEY = "ubiquitos.eth.udp.passivePortRange";
	private static String UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE;
	
	public static final int UDP_BUFFER_SIZE = 1024;
	
    /** Object for logging registration.*/
	private static final Logger logger = UOSLogging.getLogger();

    /** A Connection Manager Listener (ConnectionManagerControlCenter) */
    private ConnectionManagerListener connectionManagerListener = null;
    
    /** Server Connection */
    private SocketDevice serverDevice;
    private UDPServerConnection server;
    
    /** Attribute to control the closing of the Connection Manager */
    private boolean closingEthernetConnectionManager = false;
    
    /** The ChannelManager for new channels */
    private UDPChannelManager channelManager;
    
    /* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
    /**
	 * Constructor
	 * @throws UbiquitOSException
	 */
    public UDPConnectionManager() throws NetworkException {}
    
    
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
	public void init(InitialProperties resourceBundle) {
		properties = resourceBundle;
		
		if(properties == null){
        	String msg = "ResourceBundle is null";
        	logger.severe(msg);
            throw new RuntimeException(msg);
        }else{
        	try{
        		UBIQUITOS_ETH_UDP_PORT = Integer.parseInt(properties.getString(UBIQUITOS_ETH_UDP_PORT_KEY));
        		try {
					UBIQUITOS_ETH_UDP_CONTROL_PORT = Integer.parseInt(properties.getString(UBIQUITOS_ETH_UDP_CONTROL_PORT_KEY));
				} catch (Exception e) {
					logger.info("No Alternative UDP Port defined");
				}
        		UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE = properties.getString(UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE_KEY);
        	}catch (Exception e) {
        		String msg = "Incorrect ethernet udp port";
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
			logger.fine("Closing Ethernet UDP Connection Manager...");
			server.closeConnection();
			UdpChannel.tearDown();
			logger.fine("Ethernet UDP Connection Manager is closed.");
		} catch (IOException ex) {
			closingEthernetConnectionManager = false;
			String msg = "Error closing Ethernet UDP Connection Manager. ";
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
				serverDevice = new SocketDevice(addr, UBIQUITOS_ETH_UDP_PORT, EthernetConnectionType.UDP);
				return serverDevice;
			} catch (SocketException e) {
				logger.log(Level.SEVERE,"",e);
			}
		}
		return serverDevice;
	}
	
	
	/**
	 * A method for retrive the channel manager of this connection manager
	 * @return channel managar
	 */
	public ChannelManager getChannelManager(){
		if(channelManager == null){
			channelManager = new UDPChannelManager(UBIQUITOS_ETH_UDP_PORT, UBIQUITOS_ETH_UDP_CONTROL_PORT, UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE);
		}
		return channelManager;
	}
    
	/**
	 * Method extends from Runnable. Starts the connection Manager
	 */
    public void run() {
    	logger.fine("Starting UbiquitOS Smart-Space Ethernet UDP Connection Manager.");
        logger.info("Starting Ethernet UDP Connection Manager...");
        
		try {
			server = new UDPServerConnection((SocketDevice)getNetworkDevice());
		} catch (IOException ex) {
			String msg = "Error starting Ethernet UDP Connection Manager. ";
            logger.log(Level.SEVERE,msg, ex);
            throw new RuntimeException(msg,ex);
		}
		
        logger.info("Ethernet UDP Connection Manager is started.");
        
        while (true) {
    		try {
    			connectionManagerListener.handleClientConnection(server.accept());
    		} catch (IOException ex) {
    			if(!closingEthernetConnectionManager){
    				String msg = "Error starting Ethernet UDP Connection Manager. ";
                    logger.log(Level.SEVERE,msg, ex);
                    throw new RuntimeException(msg,ex);
    			}else{
    				return;
    			}
    		}
		}
    }

}

