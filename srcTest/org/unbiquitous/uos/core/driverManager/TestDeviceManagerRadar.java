package org.unbiquitous.uos.core.driverManager;

import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.deviceManager.DeviceDao;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

public class TestDeviceManagerRadar extends TestCase {


	private static Logger logger = UOSLogging.getLogger();
	
	private static final String TEST_VALID_DEVICE_NAME = "LocalDummyDevicePassive";

	protected UOS applicationContextRadar;
	
	protected UOS applicationContextPassive;
	
	protected DeviceManager deviceManager;
	
	protected DeviceDao deviceDao;
	
	protected DriverDao remoteDriverDao;
	
	protected DriverManager driverManager;

	private static final int TIME_BETWEEN_TESTS = 500;
	
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
		
		applicationContextPassive = new UOS();
		applicationContextPassive.init("br/unb/unbiquitous/ubiquitos/uos/deviceManager/propPassive");
		
		applicationContextRadar = new UOS();
		applicationContextRadar.init("br/unb/unbiquitous/ubiquitos/uos/deviceManager/propRadar");
		
		deviceManager = applicationContextRadar.getFactory().gateway().getDeviceManager();
		SmartSpaceGateway gateway = (SmartSpaceGateway) applicationContextRadar.getGateway();
		deviceDao = gateway.getDeviceManager().getDeviceDao();
		remoteDriverDao = gateway.getDriverManager().getDriverDao();
		driverManager = applicationContextRadar.getFactory().gateway().getDriverManager();
		
		reinsertCurrentDevice(remoteDriverDao, deviceDao, applicationContextRadar,"LocalDummyDevice", "localhost");
		SmartSpaceGateway passiveGateway = (SmartSpaceGateway) applicationContextPassive.getGateway();
		reinsertCurrentDevice(
				passiveGateway.getDriverManager().getDriverDao(), 
				passiveGateway.getDeviceManager().getDeviceDao(),
				applicationContextPassive,"LocalDummyDevicePassive", "0.0.0.0");
		
		deviceDao.save(applicationContextRadar.getGateway().getCurrentDevice());
		
		
	}

	private void reinsertCurrentDevice(
			DriverDao remoteDriverDao, 
			DeviceDao deviceDao, 
			UOS applicationContext, 
			String deviceName, 
			String deviceAddr) {
		List<DriverModel> returnedDrivers =  remoteDriverDao.list(null, deviceName);
		
		if (returnedDrivers != null){
			for (DriverModel rdd : returnedDrivers){
				remoteDriverDao.delete(rdd.id(), deviceName);
			}
		}
		deviceDao.delete(deviceName);
		
		applicationContext.getGateway().getCurrentDevice().getNetworks().get(0).setNetworkAddress(deviceAddr);
	}
	
	@Override
	protected synchronized void tearDown() throws Exception {
		applicationContextPassive.tearDown();
		applicationContextRadar.tearDown();
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
	
	public synchronized void testFindADevice() throws Exception {
		
		RadarLocalhost.forceDeviceJoin();
		
		UpDevice device = new UpDevice();
		device.setName(TEST_VALID_DEVICE_NAME);
		
		UpDevice returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNotNull("Objeto Não Cadastrado",returned);
		assertEquals("Objeto Não Compatível",device.getName(),returned.getName());
		assertNotNull("Objeto Sem Redes",returned.getNetworks());
		assertEquals("Objeto Não Compatível",1,returned.getNetworks().size());
		
		List<DriverModel> returnedDrivers =  remoteDriverDao.list( null, TEST_VALID_DEVICE_NAME);
		assertNotNull("Objeto Não Compatível",returnedDrivers);
		assertEquals("Objeto Não Compatível",3,returnedDrivers.size());
		for (DriverModel rdd : returnedDrivers){
			remoteDriverDao.delete(rdd.id(), TEST_VALID_DEVICE_NAME);
		}
		deviceDao.delete(TEST_VALID_DEVICE_NAME);
		
		returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNull("Objeto Não Cadastrado",returned);
	}
	
	public synchronized void testFindADriverFromADevice() throws Exception {
		
		RadarLocalhost.forceDeviceJoin();
		
		UpDevice device = new UpDevice();
		device.setName(TEST_VALID_DEVICE_NAME);
		
		UpDevice returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNotNull("Objeto Não Cadastrado",returned);
		assertEquals("Objeto Não Compatível",device.getName(),returned.getName());
		assertNotNull("Objeto Sem Redes",returned.getNetworks());
		assertEquals("Objeto Não Compatível",1,returned.getNetworks().size());
		
		List<DriverModel> returnedDrivers =  remoteDriverDao.list(null, TEST_VALID_DEVICE_NAME);
		assertNotNull("Objeto Não Compatível",returnedDrivers);
		assertEquals("Objeto Não Compatível",3,returnedDrivers.size());
		
		//Find a Driver
		
		List<DriverData> lisDrivers = driverManager.listDrivers("StreamDriver", null) ;//remoteDriverDao.findByDriver("StreamDriver");
		assertNotNull("Driver não encontrado",lisDrivers);
		assertFalse("Driver não encontrado",lisDrivers.isEmpty());
		assertEquals("Lista de Driver incompatível",1,lisDrivers.size());
		assertEquals("Driver incompatível","StreamDriver",lisDrivers.get(0).getDriver().getName());
		
		//Delete Data
		for (DriverModel rdd : returnedDrivers){
			remoteDriverDao.delete(rdd.id(), TEST_VALID_DEVICE_NAME);
		}
		deviceDao.delete(TEST_VALID_DEVICE_NAME);
		
		returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNull("Objeto Não Cadastrado",returned);
	}
	
	public synchronized void testRemoveADevice() throws Exception{
		
		RadarLocalhost.forceDeviceJoin();
		
		UpDevice returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNotNull("Objeto Não Cadastrado",returned);
		
		RadarLocalhost.forceDeviceLeft();
		
		returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNull("Objeto Cadastrado",returned);
	}
	
	public synchronized void testDoubleJoin() throws Exception {
		
		RadarLocalhost.forceDeviceJoin();
		
		UpDevice device = new UpDevice();
		device.setName(TEST_VALID_DEVICE_NAME);
		
		UpDevice returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNotNull("Objeto Não Cadastrado",returned);
		assertEquals("Objeto Não Compatível",device.getName(),returned.getName());
		assertNotNull("Objeto Sem Redes",returned.getNetworks());
		assertEquals("Objeto Não Compatível",1,returned.getNetworks().size());
		
		RadarLocalhost.forceDeviceJoin();
		
		
		List<DriverModel> returnedDrivers =  remoteDriverDao.list(null, TEST_VALID_DEVICE_NAME);
		assertNotNull("Objeto Não Compatível",returnedDrivers);
		assertEquals("Objeto Não Compatível",3,returnedDrivers.size());
		for (DriverModel rdd : returnedDrivers){
			remoteDriverDao.delete(rdd.id(), TEST_VALID_DEVICE_NAME);
		}
		deviceDao.delete(TEST_VALID_DEVICE_NAME);
		
		returned = deviceDao.find(TEST_VALID_DEVICE_NAME);
		
		assertNull("Objeto Não Cadastrado",returned);
	}
	
}
