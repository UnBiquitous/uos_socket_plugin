package org.unbiquitous.uos.core.adaptabitilyEngine.events;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.network.socket.connection.TCPClientConnection;


public class TestAdaptabilityEngineRegisterListener extends TestCase {
	
	private static final String EVENT_KEY_PARAM = "eventKey";

	private static final String EVENT_DRIVER_CORRECT = "br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.events.DummyEventDriver";

	private static final String TEST_EVENT_KEY_CORRECT = "TEST_EVENT_KEY";
	
	private static final String TEST_EVENT_KEY_CORRECT_2 = "TEST_EVENT_KEY_2";

	private static Logger logger = UOSLogging.getLogger();
	
	private static UOS context;
	
	private static int testNumber = 0;
	
	private static final int timeToWaitBetweenTests = 100;
	
	protected ClientConnection con ;
	
	private static final int max_receive_tries = 30;
	
	private static final int timeToWaitBetweenRetries = 100;
	
	private AdaptabilityEngine adaptabilityEngine;
	private Gateway gateway;
	
	private DeviceManager deviceManager;
	
	private DummyEventDriver currentEventDriver ;
	
	private DummyEventListener currentEventListener;
	
	protected void setUp() throws Exception {
		Thread.sleep(timeToWaitBetweenTests/2);
		logger.fine("\n\n######################### TEST "+testNumber+++" #########################\n\n");
		context = new UOS();
		context.init("br/unb/unbiquitous/ubiquitos/uos/adaptabitilyEngine/events/ubiquitos");
		Thread.sleep(timeToWaitBetweenTests/2);
		currentEventDriver = DummyEventDriver.getCurrentDummyEventDriver();
		gateway = context.getGateway();
		currentEventListener = new DummyEventListener();
		deviceManager = context.getFactory().gateway().getDeviceManager();
	}
	
	protected void tearDown() throws Exception {
		context.tearDown();
	}
	
	/**
	 * Tests if a single register attempt for a listener sends a register message to the driver.
	 * @throws Exception
	 */
	public void testRegisterSuccessfulListener() throws Exception{
		gateway.register(currentEventListener, gateway.getCurrentDevice(), EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT);
		
		Call registerCall = currentEventDriver.getLastServiceCall();
		
		assertNotNull("Service Call Incorrect.", registerCall);
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, registerCall.getParameter(EVENT_KEY_PARAM));
		
	}
	
	/**
	 * Tests if a double register attempt for a listener sends a register message to the driver only at the first time.
	 * @throws Exception
	 */
	public void testRegisterListenerTwiceSameEventkey() throws Exception{
		gateway.register(currentEventListener, gateway.getCurrentDevice(), EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT);
		
		Call registerCall = currentEventDriver.getLastServiceCall();
		int firstRegisterCallCount = currentEventDriver.getLastServiceCallCount();
		
		assertNotNull("Service Call Incorrect.", registerCall);
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, registerCall.getParameter(EVENT_KEY_PARAM));
		
		gateway.register(currentEventListener, gateway.getCurrentDevice(), EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT);
		
		assertEquals("Unecessary register call.",firstRegisterCallCount, currentEventDriver.getLastServiceCallCount());
	}
	
	/**
	 * Tests if a double register attempt for a listener sends two register message to the driver for each event key sent.
	 * @throws Exception
	 */
	public void testRegisterListenerTwiceDiferentEventkey() throws Exception{
		gateway.register(currentEventListener, gateway.getCurrentDevice(), EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT);
		
		Call registerCall = currentEventDriver.getLastServiceCall();
		int firstRegisterCallCount = currentEventDriver.getLastServiceCallCount();
		
		assertNotNull("Service Call Incorrect.", registerCall);
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, registerCall.getParameter(EVENT_KEY_PARAM));
		
		gateway.register(currentEventListener, gateway.getCurrentDevice(), EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT_2);
		
		assertNotNull("Service Call Incorrect.", registerCall);
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT_2, currentEventDriver.getLastServiceCall().getParameter(EVENT_KEY_PARAM));
		assertNotSame("Incorrect Service Call.",firstRegisterCallCount, currentEventDriver.getLastServiceCallCount());
	}
	
	/**
	 * Tests if a notify message is sent to the registered listener.
	 * @throws Exception
	 */
	public void testReceiveSuccessfulNotifyMessage() throws Exception{
		//Register listener for the event
		
		UpDevice device = new UpDevice("DummyDevice");
		device.addNetworkInterface("127.0.0.1:5555", "Ethernet:TCP");
		deviceManager.registerDevice(device);
		
		gateway.register(currentEventListener, device, EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT);
		
		Call registerCall = currentEventDriver.getLastServiceCall();
		
		assertNotNull("Service Call Incorrect.", registerCall);
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, registerCall.getParameter(EVENT_KEY_PARAM));
		
		// send notify message
		
		Notify notify = new Notify(TEST_EVENT_KEY_CORRECT,EVENT_DRIVER_CORRECT);
		
		send(notify);
		
		assertNotNull("Notify Incorrect.",currentEventListener.getLastEvent());
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, currentEventListener.getLastEvent().getEventKey());
		
	}
	
	/**
	 * Tests if a notify message is rightly sent to the registered listener through the adaptability engine interface .
	 * @throws Exception
	 */
	public void testSendSuccessfulNotifyMessage() throws Exception{
		//Register listener for the event
		
		UpDevice device = new UpDevice("DummyDevice");
		device.addNetworkInterface("127.0.0.1:5555", "Ethernet:TCP");
		deviceManager.registerDevice(device);
		
		gateway.register(currentEventListener, device, EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT);
		
		Call registerCall = currentEventDriver.getLastServiceCall();
		
		assertNotNull("Service Call Incorrect.", registerCall);
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, registerCall.getParameter(EVENT_KEY_PARAM));
		
		// send notify message
		
		Notify notify = new Notify(TEST_EVENT_KEY_CORRECT,EVENT_DRIVER_CORRECT);
		
		adaptabilityEngine.notify(notify, device);
		
		assertNotNull("Notify Incorrect.",currentEventListener.getLastEvent());
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, currentEventListener.getLastEvent().getEventKey());
		
	}
	
	/**
	 * Tests if a notify message is rightly sent to the registered listener through the adaptability engine interface .
	 * @throws Exception
	 */
	public void testSendSuccessfulUnregisteringMessage() throws Exception{
		//Register listener for the event
		UpDevice device = new UpDevice("DummyDevice");
		device.addNetworkInterface("127.0.0.1:5555", "Ethernet:TCP");
		deviceManager.registerDevice(device);
		
		gateway.register(currentEventListener, device, EVENT_DRIVER_CORRECT, TEST_EVENT_KEY_CORRECT);
		
		Call registerCall = currentEventDriver.getLastServiceCall();
		int registerCount = currentEventDriver.getLastServiceCallCount();
		
		assertNotNull("Service Call Incorrect.", registerCall);
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, registerCall.getParameter(EVENT_KEY_PARAM));
		
		// send notify message
		Notify notify = new Notify(TEST_EVENT_KEY_CORRECT,EVENT_DRIVER_CORRECT);
		
		send(notify);
		
		int notifyCount = currentEventListener.getLastEventCount();
		
		assertNotNull("Notify Incorrect.",currentEventListener.getLastEvent());
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, currentEventListener.getLastEvent().getEventKey());
		
		// ask to remove the listener from this event key
		
		adaptabilityEngine.unregisterForEvent(currentEventListener, device, EVENT_DRIVER_CORRECT, null, TEST_EVENT_KEY_CORRECT);
		
		Call unregisterCall = currentEventDriver.getLastServiceCall();
		
		assertNotNull("Service Call Incorrect.", unregisterCall);
		assertNotSame("Service Call Incorrect.", registerCount, currentEventDriver.getLastServiceCallCount());
		assertEquals("EventKey don't match.",TEST_EVENT_KEY_CORRECT, unregisterCall.getParameter(EVENT_KEY_PARAM));
		
		// Try to send another notify
		
		notify = new Notify(TEST_EVENT_KEY_CORRECT);
		
		send(notify);
		
		// if its the same count, no message has been delivered
		assertEquals("Notify Incorrect.",notifyCount,currentEventListener.getLastEventCount());
		
	}
	
	
	protected Response sendReceive(Call serviceCall) throws UnknownHostException, IOException, InterruptedException, JSONException{
		StringBuilder builder = sendReceive(serviceCall.toJSON().toString());
		
		if (builder.length() == 0){
			return null;
		}
		
		return Response.fromJSON(new JSONObject(builder.toString()));
	}
	
	protected void send(Notify notify) throws UnknownHostException, IOException, InterruptedException, JSONException{
		
		String message = notify.toJSON().toString();
		
		sendReceive(message);
		
	}

	private StringBuilder sendReceive(String message) throws IOException,
			InterruptedException {
		con = new TCPClientConnection("localhost",14984, null/*EthernetTCPConnectionManager.UBIQUITOS_ETH_TCP_PORT*/);
			
		OutputStream outputStream = con.getDataOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		logger.fine("Sending message.");
		writer.write(message);
		writer.write('\n');
		writer.flush();

		Thread.sleep(1000);
		
		InputStream inputStream = con.getDataInputStream();
		
		logger.fine("Receiving message.");
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < max_receive_tries; i++){
			if (reader.ready()){
	        	for(Character c = (char)reader.read();c != '\n';c = (char)reader.read()){
	        		builder.append(c);
	        	}
	        	break;
			}
			Thread.sleep(timeToWaitBetweenRetries);
		}
		return builder;
	}
}
