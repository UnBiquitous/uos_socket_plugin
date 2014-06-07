package org.unbiquitous.uos.core.connectivityTest;

import java.util.logging.Logger;

import junit.framework.TestCase;

import org.unbiquitous.uos.core.UOS;
import org.unbiquitous.uos.core.UOSLogging;



public class TestPassiveTCP extends TestCase {

	private static Logger logger = UOSLogging.getLogger();

	protected UOS applicationContext;

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
		applicationContext = new UOS();
		applicationContext.start("br/unb/unbiquitous/ubiquitos/uos/connectivityTest/propertiesTCP");
		logger.info("***** ApplicationContextRadar started **********");		
		
	}
	

	protected synchronized void tearDown() throws Exception {
		Thread.sleep(TIME_BETWEEN_TESTS);
		logger.info("************** TEAR DOWN **************");
		applicationContext.stop();
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
