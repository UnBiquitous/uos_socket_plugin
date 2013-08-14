package org.unbiquitous.uos.network.socket.radar;


import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;
import org.unbiquitous.uos.core.network.radar.Radar;
import org.unbiquitous.uos.core.network.radar.RadarListener;
import org.unbiquitous.uos.network.socket.EthernetDevice;
import org.unbiquitous.uos.network.socket.connectionManager.EthernetConnectionManager.EthernetConnectionType;

import br.unb.cic.ethutil.EthUtil;
import br.unb.cic.ethutil.EthUtilClientListener;


/**
 * This class implements a ethernet Radar for the smart-space usaing ARP discovery mode
 * It implements 3 interfaces:
 *   Runnable - For running on a independent thread
 *   EthUtilClientListener - for recieving the Ethernet discovery events
 *   Radar - For stating and stoping the Radar. Default for all UbiquitOS radars
 *   
 * It has a listener that is invoked when a host is found or left the LAN. 
 *   
 *
 * @author Passarinho
 */
public class ArpRadar implements EthUtilClientListener, Radar {
    
	/* *****************************
	 *   	ATRUBUTES
	 * *****************************/
	
    /** Object for logging registration. */
	private static final Logger logger = UOSLogging.getLogger();
    
	
	/** A Convenient way to Access Ethernet */
	private EthUtil ethUtil = null;
	
    /** This is the list of devices present in the smart-space. */
    private Set<String> localHostRepository = null;
    
    /** A RadarListener object interested in receiving UbiquitOS Radar notifications, 
     * like "a new device has entered the smart-space" and "device X has left the smart-space". */
    private RadarListener radarListener;
    
    /** Indicates whether the radar is running or not. */
    private boolean started = false;
    
    /** A Thread for running this Radar*/
    Thread thread = null;
    
    /**
     * The connection manager responsible for handling the information of connections.
     */
    private ConnectionManager connectionManager;
    
    /* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/
	
    /**
     * Constructor
     * @param radarControlCenter 
     * @param listener Some object interested in receive Radar notifications
     *  about devices entrance and exit.
     */
    public ArpRadar(RadarListener radarListener ) {
    	// add the listener
    	this.radarListener = radarListener;
    	ethUtil = new EthUtil(this);
    }
    
    /* *****************************
	 *   	PUBLIC METHODS - Runnable
	 * *****************************/
    
    /**
     * Runnable implementation
     *  - called my runnable.start() method to start the thread
     */
    public void run() {
        try {
            // Start the device discovery. According to the defined discovery Mode
            // log it.
        	logger.fine("[EthernetArpRadar] Starting Radar... ARP Discovery");
        	// start ARP discovery.
        	
            ethUtil.discoverDevices(EthUtil.DISCOVER_DEVICES_USING_ARP);
        } catch (Exception ex) {
        	logger.severe("[EthernetArpRadar] Could Not realize the host discovery...");
        }
    }
    
    /* *****************************
	 *   	PUBLIC METHODS - Radar
	 * *****************************/
    
    /**
     * Start the space scan process.
     */
    public void startRadar() {
    	// sets the flag
        started = true; 
    }
    
    /**
     * Stop the space scan process.
     */
    public void stopRadar() {
    	// sets the flag
    	started = false;
    }
    
    
    /* *****************************
	 *   	PUBLIC METHODS - EthUtilClientListener
	 * *****************************/
    
    
    /**
     * Part of the EthUtilClientListener interface implementation.
     * Method invoked when a new device is discovered by the ARP host discovery
     * 
     * @param host 
     */
    public void deviceDiscovered(String host) {

    	logger.fine("[EthernetArpRadar] A device was found [" + host + "].");
        //Notify listeners.
    	logger.info("[EthernetArpRadar] [" + host + "] is in the smart-space.");
    	// Creates a EthernetDevice Object
    	//FIXME: ArpRadar : This asumption only works with the TCP-Plugin and don't consider the PortParameter.
    	EthernetDevice device = new EthernetDevice(host, 14984, EthernetConnectionType.TCP);
    	// Notifies the listener
    	radarListener.deviceEntered(device);
    }
    
    
    /**
     * Part of the EthUtilClientListener interface implementation.
     * Method invoked when the discovery method is finished.
     * 
     * @param host 
     */
    public void deviceDiscoveryFinished(Vector<String> recentilyDiscoveredHosts) {
        
    	logger.info("[EthernetArpRadar] Ethernet Discovery Finished. Found Devices: "+ recentilyDiscoveredHosts);

    	// If localHostRepository equals null, First time Discovery is called), populates it with the found hosts
    	if (localHostRepository == null){
    		localHostRepository = new HashSet<String>(recentilyDiscoveredHosts);
    	}else{
    		//else, checks if some host exited the smart-space
    		for (String existingHost : localHostRepository) {
    			// If localHost wasn't found on the network... It left the smart-space
				if (!recentilyDiscoveredHosts.contains(existingHost)){
					// remove from localRepository.
					localHostRepository.remove(existingHost); 
					// notifies the Radar Control Center
					logger.info("[EthernetArpRadar] Host ["+existingHost+"] has left the smart-space.");
			    	//FIXME: ArpRadar : This asumption only works with the TCP-Plugin and don't consider the PortParameter.
					radarListener.deviceLeft(new EthernetDevice(existingHost,  14984, EthernetConnectionType.TCP)); 
				}
			}
    		// add all found hosts to the local repository
    		localHostRepository.addAll(recentilyDiscoveredHosts);
    	}
    	
        //If stopRadar method was called, a new device discovery will not be started
        if (started) {
            //Start a new host discovery process
        	logger.fine("[EthernetArpRadar] Starting a new discovery.");
        	try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
        	ethUtil.discoverDevices(EthUtil.DISCOVER_DEVICES_USING_ARP);
        }
    }
    
    @Override
    public void setConnectionManager(ConnectionManager connectionManager) {
    	this.connectionManager = connectionManager;
    }
}
