package br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.events;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.UosEventListener;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;

public class DummyEventListener implements UosEventListener {

	private Notify lastEvent;
	
	private int lasteventCount = 0;
	
	@Override
	public void handleEvent(Notify event) {
		//stores the last recieved event
		lastEvent = event;
		lasteventCount++;
	}

	/**
	 * @return the lastEvent
	 */
	public Notify getLastEvent() {
		return lastEvent;
	}

	/**
	 * @return the lasteventCount
	 */
	public int getLastEventCount() {
		return lasteventCount;
	}

}
