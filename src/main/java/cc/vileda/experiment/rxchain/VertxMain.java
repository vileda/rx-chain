package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;
import cc.vileda.experiment.common.User;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.eventbus.EventBus;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import io.vertx.rxjava.ext.web.handler.BodyHandler;
import rx.Observable;

import static cc.vileda.experiment.common.Globals.CREATE_ADDRESS_COMMAND_ADDRESS;
import static cc.vileda.experiment.common.Globals.CREATE_USER_COMMAND_ADDRESS;

public class VertxMain {
	public static void main(String[] args) {
		new VertxMain().run(Vertx.vertx());
	}

	public void run(Vertx vertx) {
		EventBus eventBus = vertx.eventBus();
		EventStore eventStore = new EventStore(eventBus);
		new CreateUserProcess(eventBus);

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		router.post("/users").handler(routingContext -> {
			publishCommand(CREATE_USER_COMMAND_ADDRESS, eventStore, routingContext);
		});

		router.post("/addresses").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			CreateUserRequest createUserRequest = Json.decodeValue(routingContext.getBodyAsString(), CreateUserRequest.class);

			eventStore.publish(CREATE_ADDRESS_COMMAND_ADDRESS, createUserRequest.getAddress(), Address.class)
					.onErrorResumeNext(message -> {
						response.setStatusCode(500).end(message.getMessage());
						return Observable.empty();
					})
					.subscribe(address -> response.setStatusCode(200).end(Json.encode(address)));
		});

		server.requestHandler(router::accept).listen(8080);
	}

	private void publishCommand(String event, EventStore eventStore, RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		CreateUserRequest createUserRequest = Json.decodeValue(routingContext.getBodyAsString(), CreateUserRequest.class);

		eventStore.publish(event, createUserRequest, User.class)
				.onErrorResumeNext(message -> {
					response.setStatusCode(500).end(message.getMessage());
					return Observable.empty();
				})
				.subscribe(user -> response.setStatusCode(200).end(Json.encode(user)));
	}
}
