package org.unbiquitous.uos.core.connectivityTest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall.ServiceType;

import junit.framework.TestCase;

/*
 * This test only works along with other on thsi package.
 * Such testes are designed to be executed on different machines.
 */
public class TestActiveStreamUDP extends TestCase {

	private static Logger logger = Logger.getLogger(TestActiveStreamUDP.class);

	protected UOSApplicationContext applicationContext;

	private static final int TIME_BETWEEN_TESTS = 500;
	
	private static final int TIME_TO_LET_BE_FOUND = 25000;
	
	protected static long currentTest = 0;

	private Object lock = Object.class;
	
	private boolean isOnTest = false;
	
	private int activeChannels;
	
	private static final int max_receive_tries = 30;
	
	
	
	@Override
	protected synchronized void setUp() throws Exception {
		
		synchronized (lock) {
			if (isOnTest){
				System.out.println("====== Waiting Lock Release ("+lock.hashCode()+") ======");
				lock.wait();
			}
			System.out.println("====== Locked ("+lock.hashCode()+") "+isOnTest+"  ======");
			isOnTest = true;
		}
		
		logger.info("\n");
		logger.info("============== Teste : "+currentTest+++" ========================== Begin");
		logger.info("\n");
		
		
		applicationContext = new UOSApplicationContext();
		applicationContext.init("br/unb/unbiquitous/ubiquitos/uos/connectivityTest/propertiesUDP");
		
	}
	
	
	@Override
	protected synchronized void tearDown() throws Exception {
		applicationContext.tearDown();
		logger.info("============== Teste : "+(currentTest-1)+" ========================== End");
		Thread.sleep(TIME_BETWEEN_TESTS);
		synchronized (lock) {
			if (!isOnTest){
				System.out.println("====== Waiting Lock Release ("+lock.hashCode()+") ======");
				lock.wait();
			}
			System.out.println("====== UnLocked ("+lock.hashCode()+") "+isOnTest+"  ======");
			isOnTest = false;
			lock.notify();
		}
		
	}
	
	
	
	
	@SuppressWarnings({"unchecked","rawtypes"})
	public void _testUDPConsumesStreamTCP() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testUDPConsumesStreamTCP BEGIN ---------------------- ");	
		logger.info("Trying to consume the chat service from the Device Driver from the TCP machine");
		
		int channels = 5;
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver("StreamDriver");
		serviceCall.setService("chatService");
		serviceCall.setInstanceId("streamDriverIdTCPDevice");
		serviceCall.setChannelType("Ethernet:UDP");
		serviceCall.setServiceType(ServiceType.STREAM);
		serviceCall.setChannels(channels);
		
		Map parameters = new HashMap();
		parameters.put("message", "testMessage");
		parameters.put("channels", channels);
		
		serviceCall.setParameters(parameters);
	
		ServiceResponse response = applicationContext.getGateway().callService(this.applicationContext.getDeviceManager().retrieveDevice("ProxyDevice"), serviceCall);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Stream Service OK! ");
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			logger.info("Returned encapsulated" + " : " + mapa.get("message"));
			
			if( mapa.get("message") == null ){
				return;
			}
			
			//Gives some time to establish the streams correctly
			Thread.sleep(4000);
			
			activeChannels = channels;			

			for (int i = 0; i < channels; i++) {
				ChatThreaded chatChannel = new ChatThreaded(i, response.getMessageContext().getDataInputStream(i), response.getMessageContext().getDataOutputStream(i));
		        chatChannel.start();
			}
	        
			logger.debug("waiting");
	        while(activeChannels > 0){
	        	logger.debug("probe : "+activeChannels);
	        	Thread.sleep(1000);
	        }
	        logger.debug("fim");	
			
		}else{
			logger.error("Not possible to consume chat service from the TCP machine");
		}
	
		logger.info("---------------------- testUDPConsumesStreamTCP END ---------------------- ");
	}
	
	
	
	@SuppressWarnings({"unchecked","rawtypes"})
	public void _testUDPConsumesStreamBluetooth() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testUDPConsumesStreamBluetooth BEGIN ---------------------- ");	
		logger.info("Trying to consume the chat service from the Device Driver from the Bluetooth machine");
		
		int channels = 5;
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver("StreamDriver");
		serviceCall.setService("chatService");
		serviceCall.setInstanceId("streamDriverIdBluetoothDevice");
		serviceCall.setChannelType("Ethernet:UDP");
		serviceCall.setServiceType(ServiceType.STREAM);
		serviceCall.setChannels(channels);
		
		Map parameters = new HashMap();
		parameters.put("message", "testMessage");
		parameters.put("channels", channels);
		
		serviceCall.setParameters(parameters);
	
		ServiceResponse response = applicationContext.getGateway().callService(this.applicationContext.getDeviceManager().retrieveDevice("ProxyDevice"), serviceCall);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Stream Service OK! ");
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			logger.info("Returned encapsulated" + " : " + mapa.get("message"));
			
			if( mapa.get("message") == null ){
				return;
			}
			
			//Gives some time to establish the streams correctly
			Thread.sleep(4000);
			
			activeChannels = channels;			

			for (int i = 0; i < channels; i++) {
				ChatThreaded chatChannel = new ChatThreaded(i, response.getMessageContext().getDataInputStream(i), response.getMessageContext().getDataOutputStream(i));
		        chatChannel.start();
			}
	        
			logger.debug("waiting");
	        while(activeChannels > 0){
	        	logger.debug("probe : "+activeChannels);
	        	Thread.sleep(1000);
	        }
	        logger.debug("fim");	
			
		}else{
			logger.error("Not possible to consume chat service from the Bluetooth machine");
		}
	
		logger.info("---------------------- testUDPConsumesStreamBluetooth END ---------------------- ");
	}
	
	
	
	
	private synchronized void finalizeChannel(){
		activeChannels--;
	}
	
	
	private class ChatThreaded extends Thread{
		
		private int channelNumber;
		private InputStream in;
		private OutputStream out;
		
		public ChatThreaded(int channelNumber, InputStream in, OutputStream out){
			this.channelNumber = channelNumber;
			this.in = in;
			this.out = out;
		}

		public void run() {
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		        
		        for(int j = 0; j < 10; j++){
		        	String msg  = "CHANNEL["+channelNumber+"]: MSG DE TESTE DO CHAT " + j;
		            
		            logger.debug("CHANNEL["+channelNumber+"]: ENVIANDO MSG: ["+msg+"]");
		            
		            writer.write(msg);
		            writer.flush();
		            
		            Thread.sleep(1000);
		            
		            for(int trie = 0; trie < max_receive_tries; trie++){
		            	if(reader.ready()){
		                	int available = in.available();
		                	
		                	StringBuilder builder = new StringBuilder();
		                	for(int i = 0; i < available; i++){
		                       	builder.append((char)reader.read());
		                    }
		                	logger.debug("CHANNEL["+channelNumber+"]: RECEBIDO MSG: ["+builder.toString()+"]");
		                	break;
		                }
		            	Thread.sleep(300);
		            }
		        }
		        
		        finalizeChannel();
		        
			}catch (Exception e) {
				e.printStackTrace();
			}
			logger.debug("finalize :"+activeChannels);
		}
	}
}