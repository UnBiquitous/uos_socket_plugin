package org.unbiquitous.uos.core.driver.deviceDriver;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteDeviceDriver {
	public static Test suite() { 
        TestSuite suite = new TestSuite(TestSuiteDeviceDriver.class.getName());

        suite.addTestSuite(TestDeviceDriverListDrivers.class);
        suite.addTestSuite(TestEncodeDecode.class);
        return suite; 
	}
}
