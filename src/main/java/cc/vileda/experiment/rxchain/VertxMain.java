package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;
import cc.vileda.experiment.common.User;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import static cc.vileda.experiment.common.Globals.*;

public class VertxMain {
	public static void main(String[] args) {
		new VertxMain().run();
	}

	public void run() {
		Vertx vertx = Vertx.vertx();

		EventBus eventBus = vertx.eventBus();
		new CreateUserProcess(eventBus);

		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);
		router.route().handler(BodyHandler.create());

		router.post("/users").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			CreateUserRequest createUserRequest = Json.decodeValue(routingContext.getBodyAsString(), CreateUserRequest.class);
			eventBus.send(CREATE_USER_COMMAND_ADDRESS, Json.encode(createUserRequest), messageAsyncResult -> {
				MultiMap headers = messageAsyncResult.result().headers();
				if(HEADER_TRUE.equals(headers.get(ERROR_HEADER))) {
					response.setStatusCode(500).end((String) messageAsyncResult.result().body());
					return;
				}
				User user = Json.decodeValue((String) messageAsyncResult.result().body(), User.class);
				response.setStatusCode(200).end(user.getId());
			});
		});

		router.post("/addresses").handler(routingContext -> {
			HttpServerResponse response = routingContext.response();
			CreateUserRequest createUserRequest = Json.decodeValue(routingContext.getBodyAsString(), CreateUserRequest.class);
			eventBus.send(CREATE_ADDRESS_COMMAND_ADDRESS, Json.encode(createUserRequest.getAddress()), messageAsyncResult -> {
				MultiMap headers = messageAsyncResult.result().headers();
				if(HEADER_TRUE.equals(headers.get(ERROR_HEADER))) {
					response.setStatusCode(500).end((String) messageAsyncResult.result().body());
					return;
				}
				Address address = Json.decodeValue((String) messageAsyncResult.result().body(), Address.class);
				response.setStatusCode(200).end(address.getId());
			});
		});

		server.requestHandler(router::accept).listen(8080);
	}
}
