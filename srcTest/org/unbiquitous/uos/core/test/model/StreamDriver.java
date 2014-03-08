package org.unbiquitous.uos.core.test.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;


/**
 * 
 * This is a simple stream driver to test the channel feature.
 * It just do like a chat app where it take a msg and return the response for that request by the channel.
 * 
 * @author Lucas Lins
 *
 */
public class StreamDriver implements UosDriver {
	
	private static final String MESSAGE_KEY = "message";
	private static final String CHANNELS_KEY = "channels";
	
	private static final Logger logger = UOSLogging.getLogger();
	
	/**
	 * Receives the request and starts the threaded chat manager.
	 * 
	 * @param serviceCall
	 * @param serviceResponse
	 * @param messageContext
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void chatService(Call serviceCall, Response serviceResponse, CallContext messageContext){
		
		logger.fine("Handling StreamDriver.chatService Call");
		
		Map parameters = serviceCall.getParameters();
		
		int channels = (Integer)parameters.get(CHANNELS_KEY);
		
		logger.fine("Caller DeviceName : "+messageContext.getCallerNetworkDevice().getNetworkDeviceName());
		
		Map responseMap = new HashMap();
		
		responseMap.put(MESSAGE_KEY, "CHAT STARTING...");
		
		for(int i = 0; i < channels; i++){
			(new ChatServiceThreaded(i, messageContext)).start();
		}
		
		serviceResponse.setResponseData(responseMap);

	}
	
	/**
	 * The threaded class that will manager the data the will be received by the channel and generates the
	 * response for it.
	 *  
	 * @author Lucas Lins
	 *
	 */
	private class ChatServiceThreaded extends Thread{
		
		private int MAX_NOT_READY_TRIES = 10;
		private int NOT_READY_SLEEP_TIME = 100;
		
		private int channel;
		private CallContext msgContext;
		
		public ChatServiceThreaded(int channel, CallContext msgContext){
			this.channel = channel;
			this.msgContext = msgContext;
		}
		
		public void run(){
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(msgContext.getDataInputStream(channel)));
	            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(msgContext.getDataOutputStream(channel)));
	            
	            int notReadyCount = 0;
	            while(true){
	            	if (reader.ready()){
		            	int available = msgContext.getDataInputStream(channel).available();
		            	
		            	StringBuilder builder = new StringBuilder();
		            	for(int i = 0; i < available; i++){
		            		builder.append((char)reader.read());
		            	}
		            	logger.fine("CHANNEL_DRIVER["+channel+"]: RECEBIDO MSG: ["+builder.toString()+"]");
		            	
		            	String msgRetorno = "CHANNEL_DRIVER["+channel+"]: STREAM SERVICE RECEBEU: {"+builder.toString()+"}";
		            	
		            	logger.fine("CHANNEL_DRIVER["+channel+"]: ENVIANDO MSG: ["+msgRetorno+"]");
		            	
		            	writer.write(msgRetorno);
		            	writer.flush();
	            	}else{
	            		notReadyCount++;
	            	}
	            	
	            	if (notReadyCount > MAX_NOT_READY_TRIES){
	            		Thread.sleep(NOT_READY_SLEEP_TIME);
	            	}
	            }
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public UpDriver getDriver() {
		UpDriver driver = new UpDriver();
    	driver.setName("StreamDriver");
    	
    	List<UpService> services = new ArrayList<UpService>();
    	
    	//populate services
    	
    	UpService listDrivers = new UpService();
    	listDrivers.setName("chatService");
    	Map<String, UpService.ParameterType> listDriversParameters = new HashMap<String, UpService.ParameterType>(); 
    	listDriversParameters.put(MESSAGE_KEY, UpService.ParameterType.MANDATORY);
    	listDrivers.setParameters(listDriversParameters);
    	
    	services.add(listDrivers);
    	
    	driver.setServices(services);
    	
    	return driver;
	}

	@Override
	public void init(Gateway gateway, String instanceId) {}

	@Override
	public void destroy() {}

	@Override
	public List<UpDriver> getParent() {
		// TODO Auto-generated method stub
		return null;
	}

}
