package br.unb.unbiquitous.ubiquitos.uos;

import junit.framework.Test;
import junit.framework.TestSuite;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.TestSuiteAdaptabilityEngine;
import br.unb.unbiquitous.ubiquitos.uos.deviceManager.TestSuiteDeviceManager;
import br.unb.unbiquitous.ubiquitos.uos.driver.deviceDriver.TestSuiteDeviceDriver;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.TestSuiteDriverManager;

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
