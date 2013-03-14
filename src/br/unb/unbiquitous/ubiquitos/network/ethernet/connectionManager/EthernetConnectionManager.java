package br.unb.unbiquitous.ubiquitos.network.ethernet.connectionManager;

import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManager;

public abstract class EthernetConnectionManager implements ConnectionManager{
	
	/** Enum for the connection type */
	public enum EthernetConnectionType{TCP, UDP, RTP};

}
