package org.unbiquitous.uos.core.adaptabilityEngine.events;

import org.unbiquitous.uos.core.adaptabitilyEngine.UosEventListener;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;

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
