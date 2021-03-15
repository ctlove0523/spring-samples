package io.ctlove0523.spring.gateway.events;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EventHandlers {

	@EventListener()
	public void processEvent(RefreshRoutesEvent event) {
		System.out.println(event.getSource());
		System.out.println("begin to refresh routes");
	}
}
