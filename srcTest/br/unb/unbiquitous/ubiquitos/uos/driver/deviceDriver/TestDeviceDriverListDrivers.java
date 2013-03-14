package br.unb.unbiquitous.ubiquitos.uos.driver.deviceDriver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import br.unb.unbiquitous.json.JSONException;
import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext;
import br.unb.unbiquitous.ubiquitos.uos.driver.DeviceDriverImpl;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.json.JSONDriver;
import br.unb.unbiquitous.ubiquitos.uos.test.model.DummyDriver;

public class TestDeviceDriverListDrivers extends TestCase {
private static final Logger logger = Logger.getLogger(TestDeviceDriverListDrivers.class);
	
	private static UOSApplicationContext context;
	
	private static int testNumber = 0;
	
	private static final int timeToWaitBetweenTests = 2000;
	
	protected void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests/2);
		logger.debug("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOSApplicationContext();
		context.init("br/unb/unbiquitous/ubiquitos/uos/deviceManager/ubiquitos");
		Thread.sleep(timeToWaitBetweenTests/2);
	};
	
	protected void tearDown() throws Exception {
		context.tearDown();
		System.gc();
	}
	
	public void testSendListDrivers() 
		throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',service:'listDrivers'}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,String> testListDrivers = new HashMap<String,String>();
		
		UpDriver upDeviceDriver = (new DeviceDriverImpl()).getDriver();
		JSONDriver jsonDeviceDriver = new JSONDriver(upDeviceDriver);
		
		testListDrivers.put("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver1", jsonDeviceDriver.toString());
		testListDrivers.put("defaultDeviceDriver", jsonDeviceDriver.toString());
		testListDrivers.put("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver3", jsonDeviceDriver.toString());
		testListDrivers.put("testListId", jsonDeviceDriver.toString());
		
		UpDriver upDummyDriver = (new DummyDriver()).getDriver();
		JSONDriver jsonDummyDriver = new JSONDriver(upDummyDriver);
		
		testListDrivers.put("dummyDriverId", jsonDummyDriver.toString());
		testListDrivers.put("DummyDriver6", jsonDummyDriver.toString());
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers).toString());
		
		assertEquals(expectedDriverList.toString(), jsonResponse.optJSONObject("responseData").toString());
	}
	
	public void testSendListDriversByDriverNameValid1() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',parameters:{driverName:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver'},service:'listDrivers'}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,String> testListDrivers = new HashMap<String,String>();
		
		UpDriver upDriver = (new DeviceDriverImpl()).getDriver();
		
		JSONDriver jsonDriver = new JSONDriver(upDriver);
		
		testListDrivers.put("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver1", jsonDriver.toString());
		testListDrivers.put("br.unb.unbiquitous.ubiquitos.driver.DeviceDriver3", jsonDriver.toString());
		testListDrivers.put("defaultDeviceDriver", jsonDriver.toString());
		testListDrivers.put("testListId", jsonDriver.toString());
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers).toString());
		
		assertEquals(expectedDriverList.toString(), jsonResponse.optJSONObject("responseData").toString());
	}
	
	public void testSendListDriversByDriverNameValid2() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',parameters:{driverName:'DummyDriver'},service:'listDrivers'}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,String> testListDrivers = new HashMap<String,String>();
		
		UpDriver upDummyDriver = (new DummyDriver()).getDriver();
		JSONDriver jsonDummyDriver = new JSONDriver(upDummyDriver);
		
		testListDrivers.put("dummyDriverId", jsonDummyDriver.toString());
		testListDrivers.put("DummyDriver6", jsonDummyDriver.toString());
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers).toString());
		
		assertEquals(expectedDriverList.toString(), jsonResponse.optJSONObject("responseData").toString());
	}
	
	public void testSendListDriversByDriverNameEmpty() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',parameters:{driverName:''},service:'listDrivers'}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,String> testListDrivers = new HashMap<String,String>();
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers).toString());
		
		assertEquals(expectedDriverList.toString(), jsonResponse.optJSONObject("responseData").toString());
	}
	
	public void testSendListDriversByDriverNameWrong() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'br.unb.unbiquitous.ubiquitos.driver.DeviceDriver',parameters:{driverName:'no.exists.driver.name'},service:'listDrivers'}";
		
		logger.debug("Sending Message:");
		logger.debug(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.debug("Returned Message:");
		logger.debug("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,String> testListDrivers = new HashMap<String,String>();
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers).toString());
		
		assertEquals(expectedDriverList.toString(), jsonResponse.optJSONObject("responseData").toString());
	}
	
	
	
	
	
	
	
	private static String sendReceive(String message) throws UnknownHostException, IOException, InterruptedException{
		Socket socket = new Socket("localhost",14984/*EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT*/);
		
		OutputStream outputStream = socket.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		
		writer.write(message);
		writer.write('\n');
		writer.flush();
		Thread.sleep(1000);
		
		InputStream inputStream = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		StringBuilder builder = new StringBuilder();
		if (reader.ready()){
        	for(Character c = (char)reader.read();c != '\n';c = (char)reader.read()){
        		builder.append(c);
        	}
		}
		socket.close();
		if (builder.length() == 0){
			return null;
		}
		return builder.toString(); 
	}
}
