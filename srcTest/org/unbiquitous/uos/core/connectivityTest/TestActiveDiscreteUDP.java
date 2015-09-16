package org.unbiquitous.uos.core.connectivityTest;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.ServiceCallException;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import junit.framework.TestCase;

public class TestActiveDiscreteUDP extends TestCase {

	private static Logger logger = UOSLogging.getLogger();

	protected UOS applicationContext;

	private static final int TIME_BETWEEN_TESTS = 500;

	private static final int TIME_TO_LET_BE_FOUND = 25000;

	protected static long currentTest = 0;

	private static final ObjectMapper mapper = new ObjectMapper();

	private Object lock = Object.class;

	private boolean isOnTest = false;

	@Override
	protected synchronized void setUp() throws Exception {

		synchronized (lock) {
			if (isOnTest) {
				System.out.println("====== Waiting Lock Release (" + lock.hashCode() + ") ======");
				lock.wait();
			}
			System.out.println("====== Locked (" + lock.hashCode() + ") " + isOnTest + "  ======");
			isOnTest = true;
		}

		logger.info("\n");
		logger.info("============== Teste : " + currentTest++ + " ========================== Begin");
		logger.info("\n");

		applicationContext = new UOS();
		applicationContext.start("org/unbiquitous/uos/core/connectivityTest/propertiesUDP");

	}

	@Override
	protected synchronized void tearDown() throws Exception {
		applicationContext.stop();
		logger.info("============== Teste : " + (currentTest - 1) + " ========================== End");
		Thread.sleep(TIME_BETWEEN_TESTS);
		synchronized (lock) {
			if (!isOnTest) {
				System.out.println("====== Waiting Lock Release (" + lock.hashCode() + ") ======");
				lock.wait();
			}
			System.out.println("====== UnLocked (" + lock.hashCode() + ") " + isOnTest + "  ======");
			isOnTest = false;
			lock.notify();
		}
	}

	@SuppressWarnings("unchecked")
	public void testUDPConsumesDiscreteTCP() throws Exception {

		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);

		logger.info("---------------------- testUDPConsumesDiscreteTCP BEGIN ---------------------- ");
		logger.info("Trying to consume the listDrivers service from the Device Driver from the TCP machine");

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("device", mapper.writeValueAsString(this.applicationContext.getGateway().getCurrentDevice()));

		try {
			Response response = this.applicationContext.getGateway().callService(
					this.applicationContext.getFactory().gateway().getDeviceManager().retrieveDevice("ProxyDevice"),
					"listDrivers", "uos.DeviceDriver", "deviceDriverImplIdTCPDevice", null, // No security needed 
					parameterMap // Informing the current device data to the remote device
			);

			assertNotNull(response);

			if (response.getError() == null || response.getError().isEmpty()) {
				logger.info("Let's see what we got: ");
				Map<String, Object> mapa = response.getResponseData();

				Map<String, JsonNode> jsonList = mapper.readValue(mapa.get("driverList").toString(), Map.class);
				for (Map.Entry<String, JsonNode> entry : jsonList.entrySet()) {
					logger.info(entry.getKey() + " : " + entry.getValue());
				}
			} else {
				logger.severe("Not possible to listDrivers from the TCP machine");
			}
		} catch (ServiceCallException e) {
			logger.log(Level.SEVERE, "Not possible to listDrivers from the TCP machine", e);
		}
		logger.info("---------------------- testUDPConsumesDiscreteTCP END ---------------------- ");
	}

	@SuppressWarnings("unchecked")
	public void testUDPConsumesDiscreteBluetooth() throws Exception {

		//Some time to the register finds us
		Thread.sleep(TIME_TO_LET_BE_FOUND);

		logger.info("---------------------- testUDPConsumesDiscreteBluetooth BEGIN ---------------------- ");
		logger.info("Trying to consume the listDrivers service from the Device Driver from the Bluetooth machine");

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("device", mapper.writeValueAsString(this.applicationContext.getGateway().getCurrentDevice()));

		try {
			Response response = this.applicationContext.getGateway().callService(
					this.applicationContext.getFactory().gateway().getDeviceManager().retrieveDevice("ProxyDevice"),
					"listDrivers", "uos.DeviceDriver", "deviceDriverImplIdBluetoothDevice", null, // No security needed 
					parameterMap // Informing the current device data to the remote device
			);

			assertNotNull(response);

			if (response != null && (response.getError() == null || response.getError().isEmpty())) {
				logger.info("Let's see what we got: ");
				Map<String, Object> mapa = response.getResponseData();

				Map<String, JsonNode> jsonList = mapper.readValue(mapa.get("driverList").toString(), Map.class);
				for (Map.Entry<String, JsonNode> entry : jsonList.entrySet()) {
					logger.info(entry.getKey() + " : " + entry.getValue());
				}
			} else {
				logger.severe("Not possible to listDrivers from the Bluetooth machine");
			}
		} catch (ServiceCallException e) {
			logger.log(Level.SEVERE, "Not possible to listDrivers from the TCP machine", e);
		}
		logger.info("---------------------- testUDPConsumesDiscreteBluetooth END ---------------------- ");
	}

}
