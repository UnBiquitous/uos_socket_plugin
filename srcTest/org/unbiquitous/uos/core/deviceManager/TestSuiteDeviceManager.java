package org.unbiquitous.uos.core.deviceManager;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteDeviceManager {
	public static Test suite() { 
        TestSuite suite = new TestSuite(TestSuiteDeviceManager.class.getName());

        suite.addTestSuite(TestDeviceManager.class);
        return suite; 
	}
}
