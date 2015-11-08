package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.event.Event;
import cc.vileda.experiment.common.event.SourcedEvent;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

import static cc.vileda.experiment.common.Globals.ERROR_HEADER;
import static cc.vileda.experiment.common.Globals.HEADER_TRUE;

public class EventStore {
	private final List<Event> eventList = new ArrayList<>();
	private final EventBus eventBus;

	public EventStore(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public <T extends SourcedEvent> Observable<T> publish(String event, Object message, Class<T> clazz) {
		String messageJson = Json.encode(message);
		if(event.contains("command")) {
			return eventBus.sendObservable(event, messageJson)
					.flatMap(objectMessage -> {
						String messageBody = (String) objectMessage.body();
						if(!hasSendError(objectMessage)) {
							T entity = Json.decodeValue(messageBody, clazz);
							return Observable.just(entity);
						}
						return Observable.error(new RuntimeException(messageBody));
					});
		}
		else if(event.contains("event")) {
			eventBus.publish(event, messageJson);
			eventList.add(new Event<>(clazz, messageJson));
		}

		return Observable.never();
	}

	private boolean hasSendError(Message<Object> messageAsyncResult) {
		MultiMap headers = messageAsyncResult.headers();
		return HEADER_TRUE.equals(headers.get(ERROR_HEADER));
	}

	public <T> MessageConsumer<T> consumer(String address, Handler<Message<T>> handler) {
		return eventBus.consumer(address, handler);
	}

	public <T extends SourcedEvent> List<Event<T>> fetchEventsFor(Class<T> clazz) {
		List<Event<T>> events = new ArrayList<>();
		eventList.stream()
				.filter(event -> event.getClazz().equals(clazz))
				.forEach(events::add);
		return events;
	}
}
