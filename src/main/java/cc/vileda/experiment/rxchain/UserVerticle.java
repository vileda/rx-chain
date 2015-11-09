package cc.vileda.experiment.rxchain;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.EventBus;

public class UserVerticle extends AbstractVerticle {
	private CreateUserProcess createUserProcess;

	public void start() {
		EventBus eventBus = vertx.eventBus();
		EventStore eventStore = new EventStore(vertx, eventBus);
		createUserProcess = new CreateUserProcess(eventStore);
	}
}
