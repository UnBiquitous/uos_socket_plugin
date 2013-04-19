package org.unbiquitous.uos.core.driverManager;

import java.io.IOException;

import org.unbiquitous.uos.network.socket.connection.EthernetTCPClientConnection;


public class TestSendMessageTCP extends TestSendMessage {

	protected void connect() throws IOException{
		con = new EthernetTCPClientConnection("localhost",14984, null/*EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT*/);
	}
}
