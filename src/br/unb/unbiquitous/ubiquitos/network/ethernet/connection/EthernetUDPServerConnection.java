package br.unb.unbiquitous.ubiquitos.network.ethernet.connection;

import java.io.IOException;
import java.net.InetSocketAddress;

import br.unb.unbiquitous.ubiquitos.network.ethernet.EthernetDevice;
import br.unb.unbiquitous.ubiquitos.network.ethernet.udp.UdpAccept;
import br.unb.unbiquitous.ubiquitos.network.ethernet.udp.UdpChannel;
import br.unb.unbiquitous.ubiquitos.network.model.connection.ServerConnection;

/**
 * this class represents the ethernet server connection.
 * 
 * @author Lucas Lins
 */
public class EthernetUDPServerConnection extends ServerConnection {
	
	/************************************
	 * ATTRIBUTES
	 ************************************/

	private UdpChannel udpChannel;
	
	/************************************
	 * CONSTRUCTOR
	 ************************************/
	
	public EthernetUDPServerConnection(EthernetDevice serverDevice) throws IOException{
		super(serverDevice);
		udpChannel = UdpChannel.openChannel(serverDevice.getPort());
	}
	
	/************************************
	 * PUBLIC METHODS
	 ************************************/
	
	/**
	 * accept a client connection and return the {@link EthernetUDPClientConnection} from this action. 
	 */
	public EthernetUDPClientConnection accept() throws IOException{
		UdpAccept udpAccept = udpChannel.accept();
		return new EthernetUDPClientConnection(udpChannel, (InetSocketAddress)udpAccept.getSocketAddress(), udpAccept);
	}
	
	/**
	 * close the socket connection.
	 */
	public void closeConnection() throws IOException {
		udpChannel.closeServer();
	}
	
}
