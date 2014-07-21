package org.unbiquitous.uos.core.deviceManager;

import java.util.Date;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.driverManager.DriverDao;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;

public class TestCompleteBasicAuthenticationBluetooth extends TestCase {

	private static Logger logger = UOSLogging.getLogger();
	protected UOS context;
	protected DeviceManager deviceManager;
	protected DeviceDao deviceDao;
	protected DriverDao remoteDriverDao;
	protected static long currentTest = 0;
	
	//
	protected UpDevice providerDevice;
	
	/** 
	 * Method executed before each test. It creates a new context of ubiquitOS and initializes the device
	 * for which the test will be executed.
	 * */
	protected void setUp() throws Exception {
		logger.info("\n");
		logger.info("============== Teste : "+currentTest+++" ==========================");
		logger.info("\n");
		
		context = new UOS();
		context.start("ubiquitos");
		
		deviceManager = context.getFactory().gateway().getDeviceManager();
		SmartSpaceGateway gateway = (SmartSpaceGateway) context.getGateway();
		deviceDao = gateway.getDeviceManager().getDeviceDao();
		remoteDriverDao = gateway.getDriverManager().getDriverDao();
		
		Thread.sleep(100);
	}

	
	/** 
	 * Method that tear down the context after each test execution.
	 * */
	protected void tearDown() throws Exception {
		context.stop();
		System.gc();
	}
	
	/** 
	 * Method to test successful authentication. It inserts the device to be authenticated in database and creates 
	 * an instance of session key database. Then creates the serviceCall and call authentication service. The expected
	 * result is successful authentication.
	 **/
	public void testSuccessfulAuthentication() throws Exception {
		
		Date initialTime = new Date();
		
		providerDevice = new UpDevice();
		// nome do celular
		providerDevice.setName("UnB Cel");
		providerDevice.setNetworks(context.getGateway().getCurrentDevice().getNetworks());
		// nome do celular
		providerDevice.getNetworks().get(0).setNetworkAddress("UnB Cel");
		// fazer networktype = Bluetooth
		providerDevice.getNetworks().get(0).setNetType("Bluetooth");
		
		String DEVICE_NAME = "UnB Cel";
		
		// inserts the device in devices database
		deviceDao.save(providerDevice);

		deviceManager.registerDevice(providerDevice);
		
		// check if the device is correctly stored
		UpDevice returned = deviceDao.find(DEVICE_NAME);
		assertNotNull("error while saving device", returned);
				
		// creates a new instance of devices database and inserts the device.
		br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB authenticationDao = new br.unb.unbiquitous.ubiquitos.uos.security.basic.AuthenticationDaoHSQLDB(); 
		if (authenticationDao.findByHashId("a52b0ce426ff8966a9c6fd5dbe24a25bf5aff4b5") == null){
			authenticationDao.insert("UnB Cel", "a52b0ce426ff8966a9c6fd5dbe24a25bf5aff4b5", "5f8d93682477592c1479ee7803ac44e1");
		} 
		
		System.out.println("antes do sleep");
		// esperar mensagem aqui
		Thread.sleep(300000);
		
		System.out.println("fim do sleep");
		Date finalTime = new Date();
		long totalTime = finalTime.getTime() - initialTime.getTime();
		logger.fine("Total run time: " + totalTime);
		
	}
}
