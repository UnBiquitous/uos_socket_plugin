package br.unb.unbiquitous.ubiquitos.network.ethernet.udp;

import java.net.SocketAddress;

public class UdpAccept {

	private SocketAddress socketAddress;
	private UdpInputStream inputStream;
	private UdpOutputStream outputStream;
	
	protected UdpAccept(SocketAddress socketAddress, UdpInputStream inputStream, UdpOutputStream outputStream){
		this.socketAddress = socketAddress;
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}
	
	public SocketAddress getSocketAddress() {
		return socketAddress;
	}

	public UdpInputStream getInputStream() {
		return inputStream;
	}

	public UdpOutputStream getOutputStream() {
		return outputStream;
	}
}
