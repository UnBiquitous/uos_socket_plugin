package org.unbiquitous.uos.core.driver.deviceDriver;

import static org.fest.assertions.api.Assertions.assertThat;

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
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.driver.DeviceDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.test.model.DummyDriver;

public class TestDeviceDriverListDrivers extends TestCase {
private static final Logger logger = UOSLogging.getLogger();
	
	private static UOS context;
	
	private static int testNumber = 0;
	
	private static final int timeToWaitBetweenTests = 2000;
	
	protected void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests/2);
		logger.fine("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOS();
		context.start("org/unbiquitous/uos/core/deviceManager/ubiquitos");
		Thread.sleep(timeToWaitBetweenTests/2);
	};
	
	protected void tearDown() throws Exception {
		context.stop();
		System.gc();
	}
	
	public void testSendListDrivers() 
		throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'uos.DeviceDriver',service:'listDrivers'}";
		
		logger.fine("Sending Message:");
		logger.fine(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.fine("Returned Message:");
		logger.fine("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,Object> testListDrivers = new HashMap<String,Object>();
		
		UpDriver upDeviceDriver = (new DeviceDriver()).getDriver();
		JSONObject jsonDeviceDriver = upDeviceDriver.toJSON();
		
		testListDrivers.put("uos.DeviceDriver1", jsonDeviceDriver);
		testListDrivers.put("defaultDeviceDriver", jsonDeviceDriver);
		testListDrivers.put("uos.DeviceDriver3", jsonDeviceDriver);
		testListDrivers.put("testListId", jsonDeviceDriver);
		
		UpDriver upDummyDriver = (new DummyDriver()).getDriver();
		JSONObject jsonDummyDriver = upDummyDriver.toJSON();
		
		testListDrivers.put("dummyDriverId", jsonDummyDriver);
		testListDrivers.put("DummyDriver6", jsonDummyDriver);
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers));
		
		assertThat(jsonResponse.optJSONObject("responseData").toMap())
										.isEqualTo(expectedDriverList.toMap());
	}
	
	public void testSendListDriversByDriverNameValid1() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'uos.DeviceDriver',parameters:{driverName:'uos.DeviceDriver'},service:'listDrivers'}";
		
		logger.fine("Sending Message:");
		logger.fine(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.fine("Returned Message:");
		logger.fine("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,Object> testListDrivers = new HashMap<String,Object>();
		
		UpDriver upDriver = (new DeviceDriver()).getDriver();
		
		JSONObject jsonDriver = upDriver.toJSON();
		
		testListDrivers.put("uos.DeviceDriver1", jsonDriver);
		testListDrivers.put("uos.DeviceDriver3", jsonDriver);
		testListDrivers.put("defaultDeviceDriver", jsonDriver);
		testListDrivers.put("testListId", jsonDriver);
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers));
		
		assertThat(jsonResponse.optJSONObject("responseData").toMap())
			.isEqualTo(expectedDriverList.toMap());
	}
	
	public void testSendListDriversByDriverNameValid2() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'uos.DeviceDriver',parameters:{driverName:'DummyDriver'},service:'listDrivers'}";
		
		logger.fine("Sending Message:");
		logger.fine(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.fine("Returned Message:");
		logger.fine("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,Object> testListDrivers = new HashMap<String,Object>();
		
		UpDriver upDummyDriver = (new DummyDriver()).getDriver();
		JSONObject jsonDummyDriver = upDummyDriver.toJSON();
		
		testListDrivers.put("dummyDriverId", jsonDummyDriver);
		testListDrivers.put("DummyDriver6", jsonDummyDriver);
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers));
		
		assertEquals(expectedDriverList.toMap(), jsonResponse.optJSONObject("responseData").toMap());
	}
	
	public void testSendListDriversByDriverNameEmpty() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'uos.DeviceDriver',parameters:{driverName:''},service:'listDrivers'}";
		
		logger.fine("Sending Message:");
		logger.fine(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.fine("Returned Message:");
		logger.fine("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,String> testListDrivers = new HashMap<String,String>();
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers));
		
		assertEquals(expectedDriverList.toMap(), jsonResponse.optJSONObject("responseData").toMap());
	}
	
	public void testSendListDriversByDriverNameWrong() 
	throws UnknownHostException, IOException, InterruptedException, JSONException {
		
		String notifyMessage = "{type:'SERVICE_CALL_REQUEST',driver:'uos.DeviceDriver',parameters:{driverName:'no.exists.driver.name'},service:'listDrivers'}";
		
		logger.fine("Sending Message:");
		logger.fine(notifyMessage);
		
		String response = sendReceive(notifyMessage);
		
		logger.fine("Returned Message:");
		logger.fine("["+response+"]");
		
		JSONObject jsonResponse = new JSONObject(response);
		
		assertEquals("SERVICE_CALL_RESPONSE",jsonResponse.opt("type"));
		
		assertNotNull(jsonResponse.optJSONObject("responseData"));
		assertNotNull(jsonResponse.optJSONObject("responseData").opt("driverList"));
		
		Map<String,String> testListDrivers = new HashMap<String,String>();
		
		JSONObject expectedDriverList = new JSONObject();
		
		expectedDriverList.put("driverList", new JSONObject(testListDrivers));
		
		assertEquals(expectedDriverList.toMap(), jsonResponse.optJSONObject("responseData").toMap());
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
