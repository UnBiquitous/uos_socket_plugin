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
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.driver.DeviceDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.test.model.DummyDriver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import junit.framework.TestCase;

public class TestDeviceDriverListDrivers extends TestCase {
	private static final Logger logger = UOSLogging.getLogger();

	private static UOS context;

	private static int testNumber = 0;

	private static final int timeToWaitBetweenTests = 2000;

	private static final ObjectMapper mapper = new ObjectMapper();

	protected void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests / 2);
		logger.fine("\n\n######################### TEST " + testNumber++ + " #########################\n\n");
		context = new UOS();
		context.start("org/unbiquitous/uos/core/deviceManager/ubiquitos");
		Thread.sleep(timeToWaitBetweenTests / 2);
	};

	protected void tearDown() throws Exception {
		context.stop();
		System.gc();
	}

	public void testSendListDrivers() throws UnknownHostException, IOException, InterruptedException {
		String notifyMessage = "{\"type\":\"SERVICE_CALL_REQUEST\",\"driver\":\"uos.DeviceDriver\",\"service\":\"listDrivers\"}";

		logger.fine("Sending Message:");
		logger.fine(notifyMessage);

		String response = sendReceive(notifyMessage);

		logger.fine("Returned Message:");
		logger.fine("[" + response + "]");

		JsonNode jsonResponse = mapper.readTree(response);

		assertEquals("SERVICE_CALL_RESPONSE", jsonResponse.get("type").asText());

		assertNotNull(jsonResponse.get("responseData"));
		assertNotNull(jsonResponse.get("responseData").get("driverList"));

		ObjectNode testListDrivers = mapper.createObjectNode();

		UpDriver upDeviceDriver = (new DeviceDriver()).getDriver();
		JsonNode jsonDeviceDriver = mapper.valueToTree(upDeviceDriver);

		testListDrivers.set("uos.DeviceDriver1", jsonDeviceDriver);
		testListDrivers.set("defaultDeviceDriver", jsonDeviceDriver);
		testListDrivers.set("uos.DeviceDriver3", jsonDeviceDriver);
		testListDrivers.set("testListId", jsonDeviceDriver);

		UpDriver upDummyDriver = (new DummyDriver()).getDriver();
		JsonNode jsonDummyDriver = mapper.valueToTree(upDummyDriver);

		testListDrivers.set("dummyDriverId", jsonDummyDriver);
		testListDrivers.set("DummyDriver6", jsonDummyDriver);

		ObjectNode expectedDriverList = mapper.createObjectNode();

		expectedDriverList.set("driverList", testListDrivers);

		assertThat(jsonResponse.get("responseData")).isEqualTo(expectedDriverList);
	}

	public void testSendListDriversByDriverNameValid1() throws UnknownHostException, IOException, InterruptedException {

		String notifyMessage = "{\"type\":\"SERVICE_CALL_REQUEST\",\"driver\":\"uos.DeviceDriver\",\"parameters\":{\"driverName\":\"uos.DeviceDriver\"},\"service\":\"listDrivers\"}";

		logger.fine("Sending Message:");
		logger.fine(notifyMessage);

		String response = sendReceive(notifyMessage);

		logger.fine("Returned Message:");
		logger.fine("[" + response + "]");

		JsonNode jsonResponse = mapper.readTree(response);

		assertEquals("SERVICE_CALL_RESPONSE", jsonResponse.get("type").asText());

		assertNotNull(jsonResponse.get("responseData"));
		assertNotNull(jsonResponse.get("responseData").get("driverList"));

		ObjectNode testListDrivers = mapper.createObjectNode();

		UpDriver upDriver = (new DeviceDriver()).getDriver();
		JsonNode jsonDriver = mapper.valueToTree(upDriver);

		testListDrivers.set("uos.DeviceDriver1", jsonDriver);
		testListDrivers.set("uos.DeviceDriver3", jsonDriver);
		testListDrivers.set("defaultDeviceDriver", jsonDriver);
		testListDrivers.set("testListId", jsonDriver);

		ObjectNode expectedDriverList = mapper.createObjectNode();
		expectedDriverList.set("driverList", testListDrivers);

		assertThat(jsonResponse.get("responseData")).isEqualTo(expectedDriverList);
	}

	public void testSendListDriversByDriverNameValid2() throws UnknownHostException, IOException, InterruptedException {

		String notifyMessage = "{\"type\":\"SERVICE_CALL_REQUEST\",\"driver\":\"uos.DeviceDriver\",\"parameters\":{\"driverName\":\"DummyDriver\"},\"service\":\"listDrivers\"}";

		logger.fine("Sending Message:");
		logger.fine(notifyMessage);

		String response = sendReceive(notifyMessage);

		logger.fine("Returned Message:");
		logger.fine("[" + response + "]");

		JsonNode jsonResponse = mapper.readTree(response);

		assertEquals("SERVICE_CALL_RESPONSE", jsonResponse.get("type").asText());

		assertNotNull(jsonResponse.get("responseData"));
		assertNotNull(jsonResponse.get("responseData").get("driverList"));

		ObjectNode testListDrivers = mapper.createObjectNode();

		UpDriver upDummyDriver = (new DummyDriver()).getDriver();
		JsonNode jsonDummyDriver = mapper.valueToTree(upDummyDriver);

		testListDrivers.set("dummyDriverId", jsonDummyDriver);
		testListDrivers.set("DummyDriver6", jsonDummyDriver);

		ObjectNode expectedDriverList = mapper.createObjectNode();
		expectedDriverList.set("driverList", testListDrivers);

		assertEquals(expectedDriverList, jsonResponse.get("responseData"));
	}

	public void testSendListDriversByDriverNameEmpty() throws UnknownHostException, IOException, InterruptedException {

		String notifyMessage = "{\"type\":\"SERVICE_CALL_REQUEST\",\"driver\":\"uos.DeviceDriver\",\"parameters\":{\"driverName\":\"\"},\"service\":\"listDrivers\"}";

		logger.fine("Sending Message:");
		logger.fine(notifyMessage);

		String response = sendReceive(notifyMessage);

		logger.fine("Returned Message:");
		logger.fine("[" + response + "]");

		JsonNode jsonResponse = mapper.readTree(response);

		assertEquals("SERVICE_CALL_RESPONSE", jsonResponse.get("type").asText());

		assertNotNull(jsonResponse.get("responseData"));
		assertNotNull(jsonResponse.get("responseData").get("driverList"));

		ObjectNode testListDrivers = mapper.createObjectNode();

		ObjectNode expectedDriverList = mapper.createObjectNode();

		expectedDriverList.set("driverList", testListDrivers);

		assertEquals(expectedDriverList, jsonResponse.get("responseData"));
	}

	public void testSendListDriversByDriverNameWrong() throws UnknownHostException, IOException, InterruptedException {

		String notifyMessage = "{\"type\":\"SERVICE_CALL_REQUEST\",\"driver\":\"uos.DeviceDriver\",\"parameters\":{\"driverName\":\"no.exists.driver.name\"},\"service\":\"listDrivers\"}";

		logger.fine("Sending Message:");
		logger.fine(notifyMessage);

		String response = sendReceive(notifyMessage);

		logger.fine("Returned Message:");
		logger.fine("[" + response + "]");

		JsonNode jsonResponse = mapper.readTree(response);

		assertEquals("SERVICE_CALL_RESPONSE", jsonResponse.get("type").asText());

		assertNotNull(jsonResponse.get("responseData"));
		assertNotNull(jsonResponse.get("responseData").get("driverList"));

		ObjectNode testListDrivers = mapper.createObjectNode();
		
		ObjectNode expectedDriverList = mapper.createObjectNode();

		expectedDriverList.set("driverList", testListDrivers);

		assertEquals(expectedDriverList, jsonResponse.get("responseData"));
	}

	private static String sendReceive(String message) throws UnknownHostException, IOException, InterruptedException {
		Socket socket = new Socket("localhost",
				14984/* EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT */);

		OutputStream outputStream = socket.getOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

		writer.write(message);
		writer.write('\n');
		writer.flush();
		Thread.sleep(1000);

		InputStream inputStream = socket.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

		StringBuilder builder = new StringBuilder();
		if (reader.ready()) {
			for (Character c = (char) reader.read(); c != '\n'; c = (char) reader.read()) {
				builder.append(c);
			}
		}
		socket.close();
		if (builder.length() == 0) {
			return null;
		}
		return builder.toString();
	}
}
