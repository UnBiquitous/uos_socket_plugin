package org.unbiquitous.uos.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.unbiquitous.uos.core.adaptabitilyEngine.TestSuiteAdaptabilityEngine;
import org.unbiquitous.uos.core.connectionManager.NetworkDeviceFilteringTest;
import org.unbiquitous.uos.core.deviceManager.TestSuiteDeviceManager;
import org.unbiquitous.uos.core.driver.deviceDriver.TestSuiteDeviceDriver;
import org.unbiquitous.uos.core.driverManager.TestSuiteDriverManager;
import org.unbiquitous.uos.network.socket.TCPChannelManagerTest;
import org.unbiquitous.uos.network.socket.radar.MulticastRadarTest;
import org.unbiquitous.uos.network.socket.radar.PingRadarTest;

public class TestSuiteUbiquitosUos {
	public static Test suite() { 
        TestSuite suite = new TestSuite(TestSuiteUbiquitosUos.class.getName());

        suite.addTest(TestSuiteAdaptabilityEngine.suite());
        suite.addTest(TestSuiteDeviceDriver.suite());
        suite.addTest(TestSuiteDriverManager.suite());
        suite.addTest(TestSuiteDeviceManager.suite());
        suite.addTestSuite(NetworkDeviceFilteringTest.class);
        suite.addTestSuite(MulticastRadarTest.class);
        suite.addTestSuite(TCPChannelManagerTest.class);
        suite.addTestSuite(PingRadarTest.class);
        return suite; 
	}
}
