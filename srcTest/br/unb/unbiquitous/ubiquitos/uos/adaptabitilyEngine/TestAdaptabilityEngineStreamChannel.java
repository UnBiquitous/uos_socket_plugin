package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall.ServiceType;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

public abstract class TestAdaptabilityEngineStreamChannel extends TestCase{
	
	private static final String TEST_DATA_CHAT_SERVICE_MESSAGE_KEY = "message";
	
	private static final String TEST_DATA_CHAT_SERVICE_CHANNELS_KEY = "channels";

	private static final String TEST_DATA_STREAM_DRIVER_ID = "streamDriverId";
	
	private static final String TEST_DATA_CHAT_SERVICE = "chatService";

	private static final String TEST_DATA_STREAM_DRIVER_NAME = "StreamDriver";
	
	private static final Logger logger = Logger.getLogger(TestAdaptabilityEngineStreamChannel.class);
	
	private static UOSApplicationContext context;
	
	private static int testNumber = 0;
	
	private static final int timeToWaitBetweenTests = 100;
	
	private static final int max_receive_tries = 30;
	
	protected UpDevice providerDevice;
	
	private Gateway gateway;
	
	private int activeChannels;
	
	protected void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests/2);
		logger.debug("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOSApplicationContext();
		context.init("br/unb/unbiquitous/ubiquitos/uos/adaptabitilyEngine/ubiquitos");
		Thread.sleep(timeToWaitBetweenTests/2);
		gateway = context.getGateway();
	};
	
	protected void tearDown() throws Exception {
		context.tearDown();
		System.gc();
	}
	
	protected abstract void setProviderDevice();
	
	public TestAdaptabilityEngineStreamChannel() {
		setProviderDevice();
	}
	
	@SuppressWarnings("unchecked")
	public void testChatService() throws ServiceCallException, IOException, InterruptedException{
		
		int channels = 5;
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver(TEST_DATA_STREAM_DRIVER_NAME);
		serviceCall.setService(TEST_DATA_CHAT_SERVICE);
		serviceCall.setInstanceId(TEST_DATA_STREAM_DRIVER_ID);
		serviceCall.setServiceType(ServiceType.STREAM);
		serviceCall.setChannels(channels);
		
		@SuppressWarnings("rawtypes")
		Map parameters = new HashMap();
		parameters.put(TEST_DATA_CHAT_SERVICE_MESSAGE_KEY, "testMessage");
		parameters.put(TEST_DATA_CHAT_SERVICE_CHANNELS_KEY, channels);
		
		serviceCall.setParameters(parameters);
		
		ServiceResponse response = gateway.callService(providerDevice, serviceCall);
		
		logger.debug("Returned Msg: ["+response.getResponseData().get(TEST_DATA_CHAT_SERVICE_MESSAGE_KEY)+"]");
		
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
		        
			}catch (Exception e) {
				logger.error("Problems executing test",e);
			}finally{
				finalizeChannel();
			}
			logger.debug("finalize :"+activeChannels);
		}
	}
}
