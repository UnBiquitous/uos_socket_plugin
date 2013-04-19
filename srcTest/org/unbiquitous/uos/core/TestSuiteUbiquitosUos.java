package org.unbiquitous.uos.core;

import org.unbiquitous.uos.core.adaptabitilyEngine.TestSuiteAdaptabilityEngine;
import org.unbiquitous.uos.core.deviceManager.TestSuiteDeviceManager;
import org.unbiquitous.uos.core.driver.deviceDriver.TestSuiteDeviceDriver;
import org.unbiquitous.uos.core.driverManager.TestSuiteDriverManager;

import junit.framework.Test;
import junit.framework.TestSuite;

public class TestSuiteUbiquitosUos {
	public static Test suite() { 
        TestSuite suite = new TestSuite(TestSuiteUbiquitosUos.class.getName());

        suite.addTest(TestSuiteAdaptabilityEngine.suite());
        suite.addTest(TestSuiteDeviceDriver.suite());
        suite.addTest(TestSuiteDriverManager.suite());
        suite.addTest(TestSuiteDeviceManager.suite());
        return suite; 
	}
}
