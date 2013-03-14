package br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.ResourceBundle;

import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ChannelManager;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManagerListener;
import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.channelManager.EthernetUDPChannelManager;
import br.unb.unbiquitous.ubiquitos.network.ethernet.connection.EthernetUDPServerConnection;
import br.unb.unbiquitous.ubiquitos.network.ethernet.udp.UdpChannel;
import br.unb.unbiquitous.ubiquitos.network.exceptions.NetworkException;
import br.unb.unbiquitous.ubiquitos.network.model.NetworkDevice;

public class EthernetUDPConnectionManager extends EthernetConnectionManager{
	
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
	/** The ResourceBundle to get some properties. */
	private ResourceBundle resource;
	
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
	private static final Logger logger = Logger.getLogger(EthernetUDPConnectionManager.class);

    /** A Connection Manager Listener (ConnectionManagerControlCenter) */
    private ConnectionManagerListener connectionManagerListener = null;
    
    /** Server Connection */
    private EthernetDevice serverDevice;
    private EthernetUDPServerConnection server;
    
    /** Attribute to control the closing of the Connection Manager */
    private boolean closingEthernetConnectionManager = false;
    
    /** The ChannelManager for new channels */
    private EthernetUDPChannelManager channelManager;
    
    /* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
    /**
	 * Constructor
	 * @throws UbiquitOSException
	 */
    public EthernetUDPConnectionManager() throws NetworkException {}
    
    
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
        	logger.fatal(msg);
            throw new RuntimeException(msg);
        }else{
        	try{
        		UBIQUITOS_ETH_UDP_PORT = Integer.parseInt(resource.getString(UBIQUITOS_ETH_UDP_PORT_KEY));
        		try {
					UBIQUITOS_ETH_UDP_CONTROL_PORT = Integer.parseInt(resource.getString(UBIQUITOS_ETH_UDP_CONTROL_PORT_KEY));
				} catch (Exception e) {
					logger.info("No Alternative UDP Port defined");
				}
        		UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE = resource.getString(UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE_KEY);
        	}catch (Exception e) {
        		String msg = "Incorrect ethernet udp port";
            	logger.fatal(msg);
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
			logger.debug("Closing Ethernet UDP Connection Manager...");
			server.closeConnection();
			UdpChannel.tearDown();
			logger.debug("Ethernet UDP Connection Manager is closed.");
		} catch (IOException ex) {
			closingEthernetConnectionManager = false;
			String msg = "Error closing Ethernet UDP Connection Manager. ";
            logger.fatal(msg, ex);
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
				Enumeration<NetworkInterface> e1 = (Enumeration<NetworkInterface>)NetworkInterface.getNetworkInterfaces();
				while(e1.hasMoreElements()) {
					NetworkInterface ni = e1.nextElement();
					
					if (!ni.isLoopback() && !ni.isVirtual() && ni.isUp()){
						Enumeration<InetAddress> e2 = ni.getInetAddresses();
						String addr = null;
						while(e2.hasMoreElements()) {
							InetAddress ia = e2.nextElement();
							if (!ia.isLoopbackAddress() && !ia.isAnyLocalAddress() && !ia.isMulticastAddress()){
								if (!ia.toString().contains(":")){
									addr = ia.toString(); // FIXME : UDP Plugin : This denies a ipv6 server to be create which is a very restrictive strategy.
								}
							}
						}
						if (addr != null){
							serverDevice = new EthernetDevice(addr.substring(1), UBIQUITOS_ETH_UDP_PORT, EthernetConnectionType.UDP);
							return serverDevice;
						}
					}
				}
			} catch (SocketException e) {
				logger.error(e);
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
			channelManager = new EthernetUDPChannelManager(UBIQUITOS_ETH_UDP_PORT, UBIQUITOS_ETH_UDP_CONTROL_PORT, UBIQUITOS_ETH_UDP_PASSIVE_PORT_RANGE);
		}
		return channelManager;
	}
    
	/**
	 * Method extends from Runnable. Starts the connection Manager
	 */
    public void run() {
    	logger.debug("Starting UbiquitOS Smart-Space Ethernet UDP Connection Manager.");
        logger.info("Starting Ethernet UDP Connection Manager...");
        
		try {
			server = new EthernetUDPServerConnection((EthernetDevice)getNetworkDevice());
		} catch (IOException ex) {
			String msg = "Error starting Ethernet UDP Connection Manager. ";
            logger.fatal(msg, ex);
            throw new RuntimeException(msg,ex);
		}
		
        logger.info("Ethernet UDP Connection Manager is started.");
        
        while (true) {
    		try {
    			connectionManagerListener.handleClientConnection(server.accept());
    		} catch (IOException ex) {
    			if(!closingEthernetConnectionManager){
    				String msg = "Error starting Ethernet UDP Connection Manager. ";
                    logger.fatal(msg, ex);
                    throw new RuntimeException(msg,ex);
    			}else{
    				return;
    			}
    		}
		}
    }

}
