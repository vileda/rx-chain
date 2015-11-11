package cc.vileda.resx;

import cc.vileda.resx.command.Command;
import cc.vileda.resx.event.DistributedEvent;
import cc.vileda.resx.event.FailedEvent;
import cc.vileda.resx.event.PersistableEvent;
import cc.vileda.resx.event.SourcedEvent;
import io.vertx.core.Handler;
import io.vertx.rxjava.core.eventbus.Message;
import rx.Observable;

import java.util.List;

public interface EventStore
{
	<T extends SourcedEvent> Observable<T> publish(T message, Class<T> clazz);

	<T extends Command, R> Observable<R> publish(T message, Class<R> clazz);

	<T extends Command, R> Observable<R> publish(String address, T message, Class<R> clazz);

	<T extends SourcedEvent> Observable<T> publish(String address, T message, Class<T> clazz);

	<T extends FailedEvent> Observable<T> publish(String address, T message);

	<T extends DistributedEvent> void consumer(Class<T> event, Handler<Message<String>> handler);

	<T extends Aggregate> Observable<T> load(String id, Class<T> aggregateClass);

	Observable<List<PersistableEvent<? extends SourcedEvent>>> getPersistableEventList();
}
