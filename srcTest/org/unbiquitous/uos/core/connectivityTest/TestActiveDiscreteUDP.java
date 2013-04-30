package org.unbiquitous.uos.core.connectivityTest;

import java.util.HashMap;
import java.util.Map;

import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.UOSApplicationContext;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDevice;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

import junit.framework.TestCase;

public class TestActiveDiscreteUDP extends TestCase {

	private static Logger logger = Logger.getLogger(TestActiveDiscreteUDP.class);

	protected UOSApplicationContext applicationContext;

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
	
	
	public void testUDPConsumesDiscreteTCP() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testUDPConsumesDiscreteTCP BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the TCP machine");
		
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("device", new JSONDevice(this.applicationContext.getGateway().getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContext.getGateway().callService(
				this.applicationContext.getDeviceManager().retrieveDevice("ProxyDevice"),
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver", 
				"deviceDriverImplIdTCPDevice",
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
			logger.error("Not possible to listDrivers from the TCP machine");
		}
	
		logger.info("---------------------- testUDPConsumesDiscreteTCP END ---------------------- ");
	}
	
	
	public void testUDPConsumesDiscreteBluetooth() throws Exception {
		
		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);
		
		logger.info("---------------------- testUDPConsumesDiscreteBluetooth BEGIN ---------------------- ");	
		logger.info("Trying to consume the listDrivers service from the Device Driver from the Bluetooth machine");
		
		Map<String,Object> parameterMap = new HashMap<String,Object>();
		parameterMap.put("device", new JSONDevice(this.applicationContext.getGateway().getCurrentDevice()).toString());
		
		ServiceResponse response = this.applicationContext.getGateway().callService(
				this.applicationContext.getDeviceManager().retrieveDevice("ProxyDevice"),
				"listDrivers", 
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver", 
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
			logger.error("Not possible to listDrivers from the Bluetooth machine");
		}
	
		logger.info("---------------------- testUDPConsumesDiscreteBluetooth END ---------------------- ");
	}
	
}
