package org.unbiquitous.uos.core.connectivityTest;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

/*
 * This test only works along with other on thsi package.
 * Such testes are designed to be executed on different machines.
 */
public class TestActiveDiscreteTCP extends TestCase {

	private static Logger logger = UOSLogging.getLogger();

	protected UOS applicationContext;

	private static final int TIME_BETWEEN_TESTS = 500;
	
	private static final int TIME_TO_LET_BE_FOUND = 25000;
	
	protected static long currentTest = 0;

	private Object lock = Object.class;
	
	private boolean isOnTest = false;
	
	
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
		
		
		applicationContext = new UOS();
		applicationContext.start("br/unb/unbiquitous/ubiquitos/uos/connectivityTest/propertiesTCP");
			
	}
	
	@Override
	protected synchronized void tearDown() throws Exception {
		applicationContext.stop();
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
	
	
	public void _testTCPConsumesDiscreteUDP() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testTCPConsumesDiscreteUDP BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the UDP machine");
		
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("device", this.applicationContext.getGateway().getCurrentDevice().toJSON().toString());
		
		Response response = this.applicationContext.getGateway().callService(
				this.applicationContext.getFactory().gateway().getDeviceManager().retrieveDevice("ProxyDevice"),
				"listDrivers", 
				"uos.DeviceDriver", 
				"deviceDriverImplIdUDPDevice",
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Let's see what we got: ");
			Map<String,Object> mapa = response.getResponseData();
			
			JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			String fields[] = JSONObject.getNames(jsonList);
			
			for( int i = 0 ; i < fields.length ; i++ ){
				logger.info(fields[i] + " : " + jsonList.get(fields[i]));
			}
			
			
		}else{
			logger.severe("Not possible to listDrivers from the UDP machine");
		}
	
		logger.info("---------------------- testTCPConsumesDiscreteUDP END ---------------------- ");
	}
	
	
	public void _testTCPConsumesDiscreteBluetooth() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testTCPConsumesDiscreteBluetooth BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the Bluetooth machine");
		
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("device", this.applicationContext.getGateway().getCurrentDevice().toJSON().toString());
		
		Response response = this.applicationContext.getGateway().callService(
				this.applicationContext.getFactory().gateway().getDeviceManager().retrieveDevice("ProxyDevice"),
				"listDrivers", 
				"uos.DeviceDriver", 
				"deviceDriverImplIdBluetoothDevice",
				null, // No security needed 
				parameterMap // Informing the current device data to the remote device
				);
		
		assertNotNull(response);
		
		if ( response != null && (response.getError() == null || response.getError().isEmpty()) ){
			logger.info("Let's see what we got: ");
			Map<String,Object> mapa = response.getResponseData();
			
			JSONObject jsonList = new JSONObject(mapa.get("driverList"));
			
			String fields[] = JSONObject.getNames(jsonList);
			
			for( int i = 0 ; i < fields.length ; i++ ){
				logger.info(fields[i] + " : " + jsonList.get(fields[i]));
			}
			
			
		}else{
			logger.severe("Not possible to listDrivers from the Bluetooth machine");
		}
	
		logger.info("---------------------- testTCPConsumesDiscreteBluetooth END ---------------------- ");
	}
	
}
