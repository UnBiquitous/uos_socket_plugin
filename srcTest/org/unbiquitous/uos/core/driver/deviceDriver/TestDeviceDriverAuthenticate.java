package org.unbiquitous.uos.core.driver.deviceDriver;

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
import org.unbiquitous.uos.core.messageEngine.messages.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class TestDeviceDriverAuthenticate extends TestCase {

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

	public void testSendAuthenticate() throws UnknownHostException, IOException, InterruptedException {

		String notifyMessage = "{\"type\":\"SERVICE_CALL_REQUEST\",\"driver\":\"uos.DeviceDriver\",\"service:\"authenticate\",\"parameters\":{\"securityType\":\"BASIC\"}}";

		logger.fine("Sending Message:");
		logger.fine(notifyMessage);

		String response = sendReceive(notifyMessage);

		logger.fine("Returned Message:");
		logger.fine("[" + response + "]");

		mapper.readValue(response, Response.class);
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
