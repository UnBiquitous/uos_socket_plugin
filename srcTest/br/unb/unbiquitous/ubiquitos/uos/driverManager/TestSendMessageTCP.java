package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.io.IOException;

import br.unb.unbiquitous.ubiquitos.network.ethernet.connection.EthernetTCPClientConnection;

public class TestSendMessageTCP extends TestSendMessage {

	protected void connect() throws IOException{
		con = new EthernetTCPClientConnection("localhost",14984, null/*EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT*/);
	}
}
