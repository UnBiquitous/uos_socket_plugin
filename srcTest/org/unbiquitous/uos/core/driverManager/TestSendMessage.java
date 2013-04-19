package org.unbiquitous.uos.core.driverManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;

import junit.framework.TestCase;


public abstract class TestSendMessage extends TestCase {
	
	private static final int timeToWaitBetweenRetries = 300;

	private static final Logger logger = Logger.getLogger(TestSendMessage.class);
	
	private static UOSApplicationContext context;
	
	private static int testNumber = 0;
	
	private static final int timeToWaitBetweenTests = 600;
	
	private static final int max_receive_tries = 30;
	
	protected ClientConnection con ;
	
	protected synchronized void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests/2);
		logger.debug("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOSApplicationContext();
		context.init("org/unbiquitous/uos/core/driverManager/ubiquitos");
		Thread.sleep(timeToWaitBetweenTests/2);
		connect();
	};
	
	protected abstract void connect() throws Exception;
	
	protected synchronized void tearDown() throws Exception {
		context.tearDown();
		System.gc();
	}
	
	public void testSendDechoRequestWithValidIntanceId()
		throws UnknownHostException, IOException, InterruptedException {
		
		String serviceCallMessage = "{type:'SERVICE_CALL_REQUEST',serviceType:'DISCRETE',driver:'DummyDriver',service:'echoService',parameters:{message:'testMessage'},instanceId:'dummyDriverId'}";
		
		logger.debug("Sending Message:");
		logger.debug(serviceCallMessage);
		
		String response = sendReceive(serviceCallMessage);
		
		logger.debug("Returned Message:");
		logger.debug(response);
		
		assertEquals("{\"responseData\":{\"message\":\"testMessage\"},\"type\":\"SERVICE_CALL_RESPONSE\"}", response);
	}
	
	public void testSendDechoRequestWithInvalidIntanceId()
		throws UnknownHostException, IOException, InterruptedException {
		
		String serviceCallMessage = "{type:'SERVICE_CALL_REQUEST',serviceType:'DISCRETE',driver:'DummyDriver',service:'echoService',parameters:{message:'testMessage'},instanceId:'dummyDriverIdInvalid'}";
		
		logger.debug("Sending Message:");
		logger.debug(serviceCallMessage);
		
		String response = sendReceive(serviceCallMessage);
		
		logger.debug("Returned Message:");
		logger.debug(response);
		
		assertEquals("{\"responseData\":{},\"error\":\"No Instance found with id 'dummyDriverIdInvalid'\",\"type\":\"SERVICE_CALL_RESPONSE\"}", response);
		
	}
	
	public void testSendDechoRequestWithoutInstanceIdAndValidDriver()
		throws UnknownHostException, IOException, InterruptedException {
		
		String serviceCallMessage = "{type:'SERVICE_CALL_REQUEST',serviceType:'DISCRETE',driver:'DummyDriver',service:'echoService',parameters:{message:'testMessage'}}";
		
		logger.debug("Sending Message:");
		logger.debug(serviceCallMessage);
		
		String response = sendReceive(serviceCallMessage);
		
		logger.debug("Returned Message:");
		logger.debug(response);
		
		assertEquals("{\"responseData\":{\"message\":\"testMessage\"},\"type\":\"SERVICE_CALL_RESPONSE\"}", response);
		
	}

	public void testSendDechoRequestWithoutInstanceIdAndInvalidDriver() 
		throws UnknownHostException, IOException, InterruptedException {
		
		String serviceCallMessage = "{type:'SERVICE_CALL_REQUEST',serviceType:'DISCRETE',driver:'DummyDriverInvalid',service:'echoService',parameters:{message:'testMessage'}}";
		
		logger.debug("Sending Message:");
		logger.debug(serviceCallMessage);
		
		String response = sendReceive(serviceCallMessage);
		
		logger.debug("Returned Message:");
		logger.debug(response);
		
		assertEquals("{\"responseData\":{},\"error\":\"No instance found for handling driver 'DummyDriverInvalid'\",\"type\":\"SERVICE_CALL_RESPONSE\"}", response);
	}
	
	public void testSendDechoRequestTwoValidRequests()
		throws UnknownHostException, IOException, InterruptedException {
		
		String serviceCallMessage = "{type:'SERVICE_CALL_REQUEST',driver:'DummyDriver',service:'echoService',parameters:{message:'testMessage'},instanceId:'dummyDriverId'}";
		
		logger.debug("Sending Message:");
		logger.debug(serviceCallMessage);
		
		String response = sendReceive(serviceCallMessage);
		
		logger.debug("Returned Message:");
		logger.debug(response);
		
		assertEquals("{\"responseData\":{\"message\":\"testMessage\"},\"type\":\"SERVICE_CALL_RESPONSE\"}", response);
		
		logger.debug("Sending Message:");
		logger.debug(serviceCallMessage);
		
		response = sendReceive(serviceCallMessage);
		
		logger.debug("Returned Message:");
		logger.debug(response);
		
		assertEquals("{\"responseData\":{\"message\":\"testMessage\"},\"type\":\"SERVICE_CALL_RESPONSE\"}", response);
	}
	
	public void testSendDechoRequestTenValidRequests()
			throws UnknownHostException, IOException, InterruptedException {
		
		String serviceCallMessage = "{type:'SERVICE_CALL_REQUEST',driver:'DummyDriver',service:'echoService',parameters:{message:'testMessage'},instanceId:'dummyDriverId'}";
		
		for(int i = 0; i < 10; i++){
			logger.debug("Sending Message:");
			logger.debug(serviceCallMessage);
			
			String response = sendReceive(serviceCallMessage);
			
			logger.debug("Returned Message:");
			logger.debug(response);
			
			assertEquals("{\"responseData\":{\"message\":\"testMessage\"},\"type\":\"SERVICE_CALL_RESPONSE\"}", response);
		}
	}
	
	protected String sendReceive(String message) throws UnknownHostException, IOException, InterruptedException{

		OutputStream outputStream = con.getDataOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		logger.debug("Sending message.");
		writer.write(message);
		writer.write('\n');
		writer.flush();

		Thread.sleep(1000);
		
		InputStream inputStream = con.getDataInputStream();
		
		logger.debug("Receiving message.");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < max_receive_tries; i++){
			if (reader.ready()){
	        	for(Character c = (char)reader.read();c != '\n';c = (char)reader.read()){
	        		builder.append(c);
	        	}
	        	break;
			}
			Thread.sleep(timeToWaitBetweenRetries);
		}
		
		
		if (builder.length() == 0){
			return null;
		}
		return builder.toString(); 
	}
	
	

}
