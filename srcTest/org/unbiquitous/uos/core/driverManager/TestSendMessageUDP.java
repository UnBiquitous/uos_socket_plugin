package org.unbiquitous.uos.core.driverManager;

import java.io.IOException;

import org.unbiquitous.uos.network.socket.connection.EthernetUDPClientConnection;


public class TestSendMessageUDP extends TestSendMessage {

	@Override
	protected void connect() throws IOException{
		con = new EthernetUDPClientConnection("localhost",15001/*EthernetUDPConnectionManager.UBIQUITOS_ETH_UDP_PORT*/);
	}

}
