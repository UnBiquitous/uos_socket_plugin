package org.unbiquitous.uos.core.deviceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;

public class TestDeviceManager extends TestCase {

	private static Logger logger = UOSLogging.getLogger();
	
	private static final String TEST_VALID_NET_ADDR_2 = "ValidDummyNetAddr2";

	private static final String TEST_VALID_NET_TYPE_2 = "ValidDummyNetType2";

	private static final String TEST_VALID_NET_ADDR_1 = "ValidDummyNetAddr1";

	private static final String TEST_VALID_NET_TYPE_1 = "ValidDummyNetType1";

	private static final String TEST_VALID_DEVICE_NAME = "TestValidDummyName";

	protected UOS applicationContext;
	
	protected DeviceManager deviceManager;
	
	protected DeviceDao deviceDao;
	
	protected static long currentTest = 0;

	private static final int TIME_BETWEEN_TESTS = 500;
	
	private Object lock = Object.class; 
	private boolean isOnTest = false;
	
	@Override
	protected void setUp() throws Exception {
		
		synchronized (lock) {
			if (isOnTest){
				logger.info("====== Waiting Lock Release ("+lock.hashCode()+") ======");
				lock.wait();
			}
			logger.info("====== Locked ("+lock.hashCode()+") "+isOnTest+" ======");
			isOnTest = true;
		}
		
		logger.info("\n");
		logger.info("============== Teste : "+currentTest+++" ==========================");
		logger.info("\n");
		
		applicationContext = new UOS();
		applicationContext.init("org/unbiquitous/uos/core/deviceManager/prop");
		
		deviceManager = applicationContext.getFactory().gateway().getDeviceManager();
		SmartSpaceGateway gateway = (SmartSpaceGateway) applicationContext.getGateway();
		deviceDao = gateway.getDeviceManager().getDeviceDao();
		Thread.sleep(TIME_BETWEEN_TESTS);
	}
	
	@Override
	protected void tearDown() throws Exception {
		applicationContext.tearDown();
		logger.info("============== Teste : "+(currentTest-1)+" ========================== End");
		synchronized (lock) {
			if (!isOnTest){
				logger.info("====== Waiting Lock Release ("+lock.hashCode()+") ======");
				lock.wait();
			}
			logger.info("====== UnLocked ("+lock.hashCode()+") "+isOnTest+"  ======");
			isOnTest = false;
			lock.notify();
		}
		Thread.sleep(TIME_BETWEEN_TESTS);
	}
	
	public void testRegisterCurrentDevice() throws Exception {
		UpDevice returned = deviceDao.find("LocalDummyDevice");
		assertNotNull(returned);
		assertEquals(applicationContext.getGateway().getCurrentDevice(),returned);
	}
	
	public void testRegisterADevice() throws Exception {
		
		UpDevice device = new UpDevice();
		device.setName(TEST_VALID_DEVICE_NAME);
		
		List<UpNetworkInterface> networks =  new ArrayList<UpNetworkInterface>();
		
		UpNetworkInterface uni = new UpNetworkInterface();
		uni.setNetType(TEST_VALID_NET_TYPE_1);
		uni.setNetworkAddress(TEST_VALID_NET_ADDR_1);
		
		networks.add(uni);
		
		uni = new UpNetworkInterface();
		uni.setNetType(TEST_VALID_NET_TYPE_2);
		uni.setNetworkAddress(TEST_VALID_NET_ADDR_2);
		
		networks.add(uni);
		
		device.setNetworks(networks);
		
		deviceManager.registerDevice(device);
	
		UpDevice returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNotNull(returned);
		assertEquals(device,returned);
		
	}
	
}
