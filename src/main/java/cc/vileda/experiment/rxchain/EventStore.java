package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.event.DistributedEvent;
import cc.vileda.experiment.common.command.Command;
import cc.vileda.experiment.common.event.Event;
import cc.vileda.experiment.common.event.FailedEvent;
import cc.vileda.experiment.common.event.SourcedEvent;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import rx.Observable;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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

	public <T extends SourcedEvent> Observable<T> publish(T message, Class<T> clazz) {
		return publish(message.getAddress(), message, clazz);
	}

	public <T extends Command, R> Observable<R> publish(T message, Class<R> clazz) {
		return publish(message.getAddress(), message, clazz);
	}

	public <T extends Command, R> Observable<R> publish(String address, T message, Class<R> clazz) {
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

	public <T extends SourcedEvent> Observable<T> publish(String address, T message, Class<T> clazz) {
		eventBus.publish(address, Json.encode(message));
		readEventsFromFile();
		eventList.add(new Event<>(clazz, Json.encode(message)));
		writeEventsToFile();
		return Observable.never();
	}

	private void writeEventsToFile()
	{
		try
		{
			FileWriter fileWriter = new FileWriter(new File("/tmp/eventstore.json"));
			fileWriter.write(Json.encode(eventList));
			fileWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public <T extends DistributedEvent> Observable<T> publish(String address, T message) {
		eventBus.publish(address, Json.encode(message));
		return Observable.never();
	}

	public <T extends FailedEvent> Observable<T> publish(String address, T message) {
		eventBus.publish(address, Json.encode(message));
		readEventsFromFile();
		eventList.add(new Event<>(message.getClass(), Json.encode(message)));
		writeEventsToFile();
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
		readEventsFromFile();
		eventList.stream()
				.filter(event -> event.getClazz().equals(clazz))
				.forEach(events::add);
		return events;
	}

	public List<Event> getEventList()
	{
		readEventsFromFile();
		return eventList;
	}

	private void readEventsFromFile()
	{
		try
		{
			final byte[] bytes = Files.readAllBytes(Paths.get("/tmp/eventstore.json"));
			final String eventsJson = new String(bytes, Charset.defaultCharset());
			JsonArray jsonArray = new JsonArray(eventsJson);
			eventList.clear();
			for (int i = 0; i < jsonArray.size(); i++)
			{
				eventList.add(Json.decodeValue(jsonArray.getJsonObject(i).encode(), Event.class));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
