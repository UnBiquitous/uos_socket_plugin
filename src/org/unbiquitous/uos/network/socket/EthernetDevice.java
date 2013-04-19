package org.unbiquitous.uos.network.socket;

import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.network.socket.connectionManager.EthernetConnectionManager.EthernetConnectionType;



/**
 * This class implement a generic device discovered by the Bluetooth Radar in the smart-space
 * 
 * @author Passarinho
 *
 */
public class EthernetDevice extends NetworkDevice{	
	
	private static final String NETWORK_DEVICE_TYPE = "Ethernet";
	
	/* *****************************
	 *   	ATTRIBUTES
	 * *****************************/
	
	// The Ethernet device discovered
	protected String host;
	
	// the port of the connection
	protected int port;
	
	// the type of the connection: tcp, udp or rtp
	protected EthernetConnectionType connectionType;
	
	/* *****************************
	 *   	CONSTRUCTOR
	 * *****************************/

	/**
	 * Constructor 
	 * 
	 * @param remoteDevice
	 */
	public EthernetDevice(String host, int port, EthernetConnectionType connectionType){
		this.host = host;
		this.port = port;
		this.connectionType = connectionType;
	}
	
	
	/* *****************************
	 *   	PUBLIC METHODS
	 * *****************************/
	
	public String getNetworkDeviceName() {
		return this.host + ":" + port;
	}
	
	public String getNetworkDeviceType() {
		return NETWORK_DEVICE_TYPE + ":" + connectionType.name();
	}


	public int getPort() {
		return port;
	}


	public EthernetConnectionType getConnectionType() {
		return connectionType;
	}
	
}
