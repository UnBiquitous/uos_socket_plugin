package org.unbiquitous.uos.core.driverManager;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.messageEngine.Base64;
import org.unbiquitous.uos.network.socket.connection.EthernetTCPClientConnection;


public class TestSendEncapsulatedMessage extends TestSendMessage {
	
	private static Logger logger = UOSLogging.getLogger(); 

	protected void connect() throws IOException{
		con = new EthernetTCPClientConnection("localhost",14984, null/*EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT*/);
	}
	
	@Override
	protected String sendReceive(String message) throws UnknownHostException,
			IOException, InterruptedException {
		
		logger.fine("Encapsulating message : "+message);
		String base64message = new String(Base64.encodeBase64(message.getBytes()));
		logger.fine("into message (base64) : "+base64message);
		
		
		String encapsulatedRequest = "{type:'ENCAPSULATED_MESSAGE',securityType:'BASIC',innerMessage:'"+base64message+"'}";
		
		String encapsulatedResponse = super.sendReceive(encapsulatedRequest);
		
		String base64Response = encapsulatedResponse.replace("{\"securityType\":\"BASIC\",\"type\":\"ENCAPSULATED_MESSAGE\",\"innerMessage\":\"", "");
		base64Response = base64Response.substring(0,base64Response.length()-2);
		
		logger.fine("Uncapsulating response (base64) : "+base64Response);
		String response = new String (Base64.decodeBase64(base64Response.getBytes()));
		logger.fine("into response  : "+response);
		return response ;
	}
	
}
