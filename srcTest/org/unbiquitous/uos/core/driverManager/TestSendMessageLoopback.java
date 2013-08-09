package org.unbiquitous.uos.core.driverManager;

import org.unbiquitous.uos.core.network.loopback.LoopbackDevice;
import org.unbiquitous.uos.core.network.loopback.connection.LoopbackClientConnection;
import org.unbiquitous.uos.core.network.loopback.connectionManager.LoopbackConnectionManager;

public class TestSendMessageLoopback extends TestSendMessage{

	
	@Override
	protected void connect() throws Exception {

		Thread.sleep(1000);
		con = new LoopbackClientConnection(new LoopbackDevice(), LoopbackConnectionManager.DEFAULT_ID);		
	}
	

}
