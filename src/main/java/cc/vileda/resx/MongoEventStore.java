package cc.vileda.resx;

import cc.vileda.resx.command.Command;
import cc.vileda.resx.event.DistributedEvent;
import cc.vileda.resx.event.FailedEvent;
import cc.vileda.resx.event.PersistableEvent;
import cc.vileda.resx.event.SourcedEvent;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.ext.mongo.MongoClient;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

import static cc.vileda.experiment.common.Globals.ERROR_HEADER;
import static cc.vileda.experiment.common.Globals.HEADER_TRUE;

public class MongoEventStore implements EventStore
{
	private final EventBus eventBus;
	private final MongoClient mongoClient;

	public MongoEventStore(Vertx vertx, EventBus eventBus) {
		this.eventBus = eventBus;
		mongoClient = MongoClient.createShared(vertx, new JsonObject());
	}

	@Override public <T extends SourcedEvent> Observable<T> publish(T message, Class<T> clazz) {
		return publish(message.getAddress(), message, clazz);
	}

	@Override public <T extends Command, R> Observable<R> publish(T message, Class<R> clazz) {
		return publish(message.getAddress(), message, clazz);
	}

	@Override public <T extends Command, R> Observable<R> publish(String address, T message, Class<R> clazz) {
		return eventBus.sendObservable(address, Json.encode(message))
				.flatMap(objectMessage -> {
					String messageBody = (String) objectMessage.body();
					if(!hasSendError(objectMessage)) {
						R entity = Json.decodeValue(messageBody, clazz);
						return Observable.just(entity);
					}
					return Observable.error(new RuntimeException(messageBody));
				});
	}

	@Override public <T extends SourcedEvent> Observable<T> publish(String address, T message, Class<T> clazz) {
		eventBus.publish(address, Json.encode(message));
		PersistableEvent<T> tPersistableEvent = new PersistableEvent<>(clazz, Json.encode(message));
		insert(tPersistableEvent);
		return Observable.never();
	}

	@Override public <T extends FailedEvent> Observable<T> publish(String address, T message) {
		eventBus.publish(address, Json.encode(message));
		PersistableEvent<? extends FailedEvent> persistableEvent = new PersistableEvent<>(message.getClass(), Json.encode(message));
		insert(persistableEvent);
		return Observable.never();
	}

	private boolean hasSendError(Message<Object> messageAsyncResult) {
		MultiMap headers = messageAsyncResult.headers();
		return HEADER_TRUE.equals(headers.get(ERROR_HEADER));
	}

	@Override public <T extends DistributedEvent> void consumer(Class<T> event, Handler<Message<String>> handler) {
		try
		{
			eventBus.consumer(event.newInstance().getAddress(), handler);
		}
		catch (InstantiationException | IllegalAccessException ignored) { }
	}

	@Override public <T extends Aggregate> Observable<T> load(String id, Class<T> aggregateClass) {
		try
		{
			T aggregate;
			aggregate = aggregateClass.newInstance();

			return getPersistableEventList().flatMap(persistableEvents -> {
				persistableEvents.stream()
						.filter(event -> !(FailedEvent.class.isAssignableFrom(event.getClazz())))
						.forEach(event -> {
							try {
								final Class<? extends SourcedEvent> clazz = event.getClazz();
								final SourcedEvent o = Json.decodeValue(event.getPayload(), clazz);
								if(id.equals(o.getId())) aggregate.apply(o);
							} catch (Exception ignored) { }
						});
				return Observable.just(aggregate);
			}).doOnError(Observable::error);
		}
		catch (InstantiationException | IllegalAccessException e) { return Observable.error(e); }
	}

	@Override public Observable<List<PersistableEvent<? extends SourcedEvent>>> getPersistableEventList()
	{
		return mongoClient.findObservable("events", new JsonObject())
				.map(jsonObjects -> {
					List<PersistableEvent<? extends SourcedEvent>> events = new ArrayList<>();
					for (JsonObject event : jsonObjects) {
						try {
							//noinspection unchecked
							Class<? extends SourcedEvent> clazz = (Class<? extends SourcedEvent>) Class.forName(event.getString("clazz"));
							PersistableEvent<? extends SourcedEvent> persistableEvent = new PersistableEvent<>(clazz, event.getJsonObject("payload").encode());
							events.add(persistableEvent);
						} catch (ClassNotFoundException ignored) { }
					}
					return events;
				});
	}

	public Observable<List<PersistableEvent<? extends SourcedEvent>>> getPersistableEventList(JsonObject query)
	{
		return mongoClient.findObservable("events", query)
				.map(jsonObjects -> {
					List<PersistableEvent<? extends SourcedEvent>> events = new ArrayList<>();
					for (JsonObject event : jsonObjects) {
						try {
							//noinspection unchecked
							Class<? extends SourcedEvent> clazz = (Class<? extends SourcedEvent>) Class.forName(event.getString("clazz"));
							PersistableEvent<? extends SourcedEvent> persistableEvent = new PersistableEvent<>(clazz, event.getJsonObject("payload").encode());
							events.add(persistableEvent);
						} catch (ClassNotFoundException ignored) { }
					}
					return events;
				});
	}

	private <T extends PersistableEvent<? extends SourcedEvent>> Observable<String> insert(T event) {
		JsonObject document = new JsonObject();
		document.put("clazz", event.getClazz().getCanonicalName());
		document.put("payload", new JsonObject(event.getPayload()));
		return mongoClient.insertObservable("events", document);
	}
}
