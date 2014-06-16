package org.unbiquitous.uos.network.socket;

import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.TestCase;

import org.junit.Test;
import org.unbiquitous.uos.core.network.cache.CacheController;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;
import org.unbiquitous.uos.network.socket.channelManager.TCPChannelManager;

public class TCPChannelManagerTest extends TestCase {

	private final int defaultPort = 14984;
	private final int controlPort = 14895;
	private final String portRange = "14896-14899";
	private CacheController cacheController = new CacheController();
	private ServerSocket server;
	private TCPChannelManager mng;
	
	public void setUp() throws Exception{
		mng = new TCPChannelManager(defaultPort, controlPort, portRange, cacheController);
	}
	
	public void tearDown() throws Exception{
		server.close();
	}
	
	public void test_opensAConnectionOnTheAddress() throws Exception{
		final boolean[] opened = openServerSocket(defaultPort);
		
		ClientConnection conn = mng.openActiveConnection("localhost");
		assertThat(conn).isNotNull();
		assertThat(opened[0]).isTrue();
	}
	
	@Test(expected=NetworkException.class) 
	public void rejectsStrangeAdresses() throws Exception{
		final boolean[] opened = openServerSocket(defaultPort);
		
		ClientConnection conn = mng.openActiveConnection("localhost:1:2:3");
		assertThat(conn).isNotNull();
		assertThat(opened[0]).isTrue();
	}
	
	public void test_opensAConnectionOnTheAddressAndPort() throws Exception{
		final boolean[] opened = openServerSocket(14896);
		
		ClientConnection conn = mng.openActiveConnection("localhost:14896");
		assertThat(conn).isNotNull();
		assertThat(opened[0]).isTrue();
	}
	
	public void test_opensACachedConnectionOnTheAddressAndPort() throws Exception{
		final boolean[] opened = openServerSocket(14896);
		
		ClientConnection conn1 = mng.openActiveConnection("localhost:14896");
		ClientConnection conn2 = mng.openActiveConnection("localhost:14896");
		assertThat(conn1).isNotNull().isSameAs(conn2);
		
		assertThat(opened[0]).isTrue();
	}
	
	public void test_doNotOpenAConnectionOnTheDefaultPortWhenInRange() throws Exception{
		final boolean[] opened = openServerSocket(defaultPort);
		
		ClientConnection conn = mng.openActiveConnection("localhost:14896");
		assertThat(conn).isNull();
		assertThat(opened[0]).isFalse();
	}
	
	public void test_opensAConnectionOnTheDefaultPortIfItsNotOnTheRightRange() throws Exception{
		final boolean[] opened = openServerSocket(defaultPort);
		
		ClientConnection conn = mng.openActiveConnection("localhost:12345");
		assertThat(conn).isNotNull();
		assertThat(opened[0]).isTrue();
	}

	private boolean[] openServerSocket(final int defaultPort) {
		final boolean opened[] = {false};
		new Thread(new Runnable() {
			public void run() {
				try {
					server = new ServerSocket(defaultPort);
					Socket conn = server.accept();
					opened[0] = true;
					conn.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
		return opened;
	}
	
}
