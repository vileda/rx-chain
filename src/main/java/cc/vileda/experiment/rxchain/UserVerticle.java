package cc.vileda.experiment.rxchain;

import cc.vileda.resx.EventStore;
import cc.vileda.resx.MongoEventStore;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;

public class UserVerticle extends AbstractVerticle {
	public void start() {
		EventBus eventBus = vertx.eventBus();
		EventStore eventStore = new MongoEventStore(vertx, eventBus);
		new CreateUserProcess(eventStore);
	}
}
