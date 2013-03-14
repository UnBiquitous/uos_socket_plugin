package br.unb.unbiquitous.ubiquitos.uos.connectivityTest;

import junit.framework.TestCase;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;

public class TestPassiveProxy extends TestCase {


	private static Logger logger = Logger.getLogger(TestPassiveProxy.class);

	protected UOSApplicationContext proxyApplicationContext;
	
	//protected UOSApplicationContext applicationContextPassiveTCP;
	
	//protected UOSApplicationContext applicationContextPassiveUDP;

	private static final int TIME_BETWEEN_TESTS = 1000;
	
	protected static long currentTest = 0;

	private Object lock = Object.class; 
	
	private boolean isOnTest = false;
	
	//private int activeChannels;
	
	//private static final int max_receive_tries = 30;
	
	
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
		
		logger.info("***** Starting proxyApplicationContext **********");
		proxyApplicationContext = new UOSApplicationContext();
		proxyApplicationContext.init("br/unb/unbiquitous/ubiquitos/uos/connectivityTest/propertiesProxy");
		logger.info("***** proxyApplicationContext started **********");
		
		
		
		
		/*logger.info("***** Starting applicationContextPassiveTCP **********");
		applicationContextPassiveTCP = new UOSApplicationContext();
		applicationContextPassiveTCP.init("br/unb/unbiquitous/ubiquitos/uos/connectivityTest/propertiesTCP");
		logger.info("***** ApplicationContextPassiveTCP started **********");*/
		
		/*logger.info("***** Starting applicationContextPassiveUDP **********");
		applicationContextPassiveUDP = new UOSApplicationContext();
		applicationContextPassiveUDP.init("br/unb/unbiquitous/ubiquitos/uos/connectivityEngine/propertiesUDP");
		logger.info("***** ApplicationContextPassiveUDP started **********");*/
		
		RadarLocalhost.forceDeviceJoin();
		
		
	}

	//Se exclui da base de devices e troca o network address do current device do application context para o address passado.
	/*private void reinsertCurrentDevice(
			RemoteDriverDao remoteDriverDao, 
			DeviceDao deviceDao, 
			UOSApplicationContext applicationContext, 
			String deviceName, 
			String deviceAddr) {
		List<RemoteDriverData> returnedDrivers =  remoteDriverDao.findByService(null, null, deviceName);
		
		if (returnedDrivers != null){
			for (RemoteDriverData rdd : returnedDrivers){
				remoteDriverDao.unregisterDriver(rdd.getInstanceID(), deviceName);
			}
		}
		deviceDao.delete(deviceName);
		
		applicationContext.getCurrentDevice().getNetworks().get(0).setNetworkAddress(deviceAddr);
	}*/
	

	protected synchronized void tearDown() throws Exception {
		Thread.sleep(TIME_BETWEEN_TESTS);
		logger.info("************** TEAR DOWN **************");
		//applicationContextPassiveTCP.tearDown();
		//applicationContextPassiveUDP.tearDown();
		proxyApplicationContext.tearDown();
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
	
	
	
	public void testWaitingForBeingConsumed(){
		
		logger.info("----------- testWaitingForBeingConsumed BEGIN ----------------");
		
		while(true){
			try {
				Thread.sleep(5000000);
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		logger.info("----------- testWaitingForBeingConsumed END ----------------");
		
	}

	
	
	/********************REGISTER*********************/

	/*public void testRegisterDriverTCP() throws Exception {
		logger.info("---------------------- testRegisterDriverTCP BEGIN ---------------------- ");
	
		logger.info("Trying to consume the listDrivers service from the Register Driver with the TCP context");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContextPassiveTCP.getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContextPassiveTCP.getAdaptabilityEngine().callService(
				this.applicationContextRadar.getCurrentDevice(), //Use de dummy device with the caller address 
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.uos.driver.RegisterDriver", 
				null, // Any Instance is ok 
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("ListDrivers OK!");
			
			//for( response.getResponseData().g)
			//JSONDriver 
			//try {
			//	
			//} catch (JSONException e) {
			//	logger.error("Not possible complete handshake with device '"+device.getNetworkDeviceName()+"'",e);
			//}
		}else{
			logger.error("Not possible to listDrivers with the register '"+this.applicationContextRadar.getCurrentDevice().getName()+"': Cause : "+response.getError());
		}
	
		logger.info("---------------------- testRegisterDriverTCP END ---------------------- ");
	}*/
	
	
	/*public void testRegisterDriverUDP() throws Exception {
		logger.info("---------------------- testRegisterDriverUDP BEGIN ---------------------- ");
	
		logger.info("Trying to consume the listDrivers service from the Register Driver with the UDP context");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContextPassiveUDP.getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContextPassiveUDP.getAdaptabilityEngine().callService(
				this.applicationContextRadar.getCurrentDevice(), //Use de dummy device with the caller address 
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.uos.driver.RegisterDriver", 
				null, // Any Instance is ok 
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty())){
			logger.info("ListDrivers OK!");
			//try {
			//	
			//} catch (JSONException e) {
			//	logger.error("Not possible complete handshake with device '"+device.getNetworkDeviceName()+"'",e);
			//}
		}else{
			if(response != null){
				logger.error("Not possible to listDrivers with the register '"+this.applicationContextRadar.getCurrentDevice().getName()+"': Cause : "+response.getError());
			}else{
				logger.error("Not possible to listDrivers with the register '"+this.applicationContextRadar.getCurrentDevice().getName()+"': Cause : null");
			}
		}
	
		logger.info("---------------------- testRegisterDriverUDP END ---------------------- ");
	}*/
	
	
	
	
	/********************DISCRETE*********************/
	
	
	
	/*public void testLOCALTCPConsumesDiscreteUDP() throws Exception {
		logger.info("---------------------- testLOCALTCPConsumesDiscreteUDP BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the UDP context");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContextPassiveTCP.getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContextPassiveTCP.getAdaptabilityEngine().callService(
				this.applicationContextRadar.getCurrentDevice(), //Use the dummy device with the caller address 
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver", 
				"deviceDriverImplIdPassiveUDP", // Any Instance is ok 
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			
			JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			String fields[] = JSONObject.getNames(jsonList);
			//JSONDriver jsonDriver = new JSONDriver();
			Set<UpDriver> listReceivedDrivers = new HashSet<UpDriver>();
			
			for( int i = 0 ; i < fields.length ; i++ ){
				logger.info(fields[i] + " : " + jsonList.get(fields[i]));
				JSONDriver jsonDriver = new JSONDriver(jsonList.get(fields[i]).toString());
				UpDriver upDriver = jsonDriver.getAsObject();
				listReceivedDrivers.add(upDriver);
			}
			
			Set<UpDriver> listDrivers = new HashSet<UpDriver>();
			for( DriverData dd : this.applicationContextPassiveUDP.getDriverManager().listDrivers() ){
				UpDriver upDriver = dd.getDriverClass();
				listDrivers.add(upDriver);
			}
			
			
			logger.info("driverList encapsulated" + " : " + mapa.get("driverList"));
			
			for( UpDriver driverReceived : listReceivedDrivers ){
				for( UpDriver driver : listDrivers ){
					if( driverReceived.getName().equals(driver.getName())){
						if(driverReceived.getServices().equals(driver.getServices()))
							logger.info("Driver found");
						break;
					}
					
				}
			}
			
			
		}else{
			logger.error("Not possible to listDrivers from the UDP Context");
		}
	
		logger.info("---------------------- testLOCALTCPConsumesDiscreteUDP END ---------------------- ");
	}*/
	
	
	
	/*public void testREALUDPConsumesDiscreteTCP() throws Exception {
		logger.info("---------------------- testREALUDPConsumesDiscreteTCP BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the TCP machine");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContextPassiveUDP.getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContextPassiveUDP.getAdaptabilityEngine().callService(
				this.applicationContextRadar.getCurrentDevice(),
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver", 
				"deviceDriverImplWindows",
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			
			JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			String fields[] = JSONObject.getNames(jsonList);
			
			for( int i = 0 ; i < fields.length ; i++ ){
				logger.info(fields[i] + " : " + jsonList.get(fields[i]));
			}
			
			
		}else{
			logger.error("Not possible to listDrivers from the UDP machine");
		}
	
		logger.info("---------------------- testREALUDPConsumesDiscreteTCP END ---------------------- ");
	}*/
	
	
	/*public void testREALTCPConsumesDiscreteUDP() throws Exception {
		logger.info("---------------------- testREALTCPConsumesDiscreteUDP BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the UDP machine");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContextPassiveTCP.getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContextPassiveTCP.getAdaptabilityEngine().callService(
				this.applicationContextRadar.getCurrentDevice(),
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver", 
				"deviceDriverImplWindows",
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			
			JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			String fields[] = JSONObject.getNames(jsonList);
			
			for( int i = 0 ; i < fields.length ; i++ ){
				logger.info(fields[i] + " : " + jsonList.get(fields[i]));
			}
			
			
		}else{
			logger.error("Not possible to listDrivers from the UDP machine");
		}
	
		logger.info("---------------------- testREALTCPConsumesDiscreteUDP END ---------------------- ");
	}*/
	
	
	
	/********************STREAMS*********************/
	
	/*public void testLOCALTCPConsumesStreamUDP() throws Exception {
		logger.info("---------------------- testLOCALTCPConsumesStreamUDP BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the UDP context");
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		parameterMap.put("device", new JSONDevice(this.applicationContextPassiveTCP.getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContextPassiveTCP.getAdaptabilityEngine().callService(
				this.applicationContextRadar.getCurrentDevice(), //Use the dummy device with the caller address 
				"chatService", 
				"StreamDriver", 
				null, // Any Instance is ok 
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Service Completed OK! ");
		}else{
			logger.error("Not possible to consume the chatService");
		}
	
		logger.info("---------------------- testLOCALTCPConsumesStreamUDP END ---------------------- ");
	}*/
	
	
	
	/*public void testREALTCPConsumesStreamUDP() throws Exception {
		logger.info("---------------------- testREALTCPConsumesStreamUDP BEGIN ---------------------- ");	
		logger.info("Trying to consume the chat service from the Device Driver from the UDP machine");
		
		int channels = 5;
		
		ServiceCall serviceCall = new ServiceCall();
		serviceCall.setDriver("StreamDriver");
		serviceCall.setService("chatService");
		serviceCall.setInstanceId("streamDriverIdRoomPassiveWindows");
		serviceCall.setServiceType(ServiceType.STREAM);
		serviceCall.setChannels(channels);
		
		Map parameters = new HashMap();
		parameters.put("message", "testMessage");
		parameters.put("channels", channels);
		//parameters.put("device", new JSONDevice(this.applicationContextPassiveTCP.getCurrentDevice()).toString());
		
		serviceCall.setParameters(parameters);
	
		ServiceResponse response = applicationContextRadar.getAdaptabilityEngine().callService(this.applicationContextRadar.getDeviceManager().retrieveDevice("RoomPassiveWindows"), serviceCall);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Stream Service OK! ");
			logger.info("Let's see what we got: ");
			Map<String, String> mapa = response.getResponseData();
			logger.info("Returned encapsulated" + " : " + mapa.get("message"));
			
			activeChannels = channels;			

			for (int i = 0; i < channels; i++) {
				ChatThreaded chatChannel = new ChatThreaded(i, ContextController.getCurrentMessageContext().getDataInputStream(i), ContextController.getCurrentMessageContext().getDataOutputStream(i));
		        chatChannel.start();
			}
	        
			logger.debug("waiting");
	        while(activeChannels > 0){
	        	logger.debug("probe : "+activeChannels);
	        	Thread.sleep(1000);
	        }
	        logger.debug("fim");
					
			
			//Map<String, String> mapa = response.getResponseData();
			
			//JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			//String fields[] = JSONObject.getNames(jsonList);
			
			//for( int i = 0 ; i < fields.length ; i++ ){
			//	logger.info(fields[i] + " : " + jsonList.get(fields[i]));
			//}
			
			
		}else{
			logger.error("Not possible to consume chat service from the UDP machine");
		}
	
		logger.info("---------------------- testREALTCPConsumesStreamUDP END ---------------------- ");
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
	}*/

	
	
	
}
