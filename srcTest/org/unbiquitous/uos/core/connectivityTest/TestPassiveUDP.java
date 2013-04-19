package org.unbiquitous.uos.core.connectivityTest;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.UOSApplicationContext;

import junit.framework.TestCase;

public class TestPassiveUDP extends TestCase {

	private static Logger logger = Logger.getLogger(TestPassiveUDP.class);

	protected UOSApplicationContext applicationContext;

	private static final int TIME_BETWEEN_TESTS = 1000;
	
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
		
		logger.info("***** Starting applicationContextRadar **********");
		applicationContext = new UOSApplicationContext();
		applicationContext.init("br/unb/unbiquitous/ubiquitos/uos/connectivityTest/propertiesUDP");
		logger.info("***** ApplicationContextRadar started **********");		
		
	}
	

	protected synchronized void tearDown() throws Exception {
		Thread.sleep(TIME_BETWEEN_TESTS);
		logger.info("************** TEAR DOWN **************");
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
	
	
	
	public void testWaitingForBeingConsumed(){
		
		logger.info("----------- testWaitingForBeingConsumed BEGIN ----------------");
		
		while(true){
			try {
				Thread.sleep(5000000);
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		logger.info("----------- testWaitingForBeingConsumed END ----------------");
		
	}
	
}
