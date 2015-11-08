package cc.vileda.experiment.rxchain;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import rx.Observable;

import static cc.vileda.experiment.common.Globals.ERROR_HEADER;
import static cc.vileda.experiment.common.Globals.HEADER_TRUE;

public class EventStore {
	private final EventBus eventBus;

	public EventStore(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public <T> Observable<T> publish(String event, Object message, Class<T> clazz) {
		if(event.contains("command")) {
			return eventBus.sendObservable(event, Json.encode(message))
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
			eventBus.publish(event, Json.encode(message));
			return Observable.never();
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

	public <T> MessageConsumer<T> consumer(String address) {
		return eventBus.consumer(address);
	}
}
