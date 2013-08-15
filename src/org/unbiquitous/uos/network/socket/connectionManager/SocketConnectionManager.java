package org.unbiquitous.uos.network.socket.connectionManager;

import org.unbiquitous.uos.core.network.connectionManager.ConnectionManager;

public abstract class SocketConnectionManager implements ConnectionManager{
	
	/** Enum for the connection type */
	public enum EthernetConnectionType{TCP, UDP, RTP};

}
