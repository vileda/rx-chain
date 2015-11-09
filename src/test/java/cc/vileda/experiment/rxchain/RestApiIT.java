package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.Json;
import io.vertx.rxjava.core.Vertx;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static cc.vileda.experiment.common.Globals.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RestApiIT {
	@BeforeClass
	public static void beforeClass() throws InterruptedException {
		VertxOptions options = new VertxOptions();
		Vertx.clusteredVertx(options, res -> {
			if (res.succeeded()) {
				new RestApiMain().run(res.result());
				new UserVerticleMain().run(res.result());
			} else {
				System.out.println("Failed: " + res.cause());
			}
		});
		Thread.sleep(6000);
	}

	@Test
	public void testCreateAddress() throws Exception {
		HttpResponse execute = createAddress("city1");
		assertThat(execute.getStatusLine().getStatusCode(), is(200));
		assertThat(IOUtils.toString(execute.getEntity().getContent()), CoreMatchers.containsString("id"));
	}

	@Test
	public void testCreateForbiddenAddress() throws Exception {
		HttpResponse execute = createAddress("city2");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
		assertThat(IOUtils.toString(execute.getEntity().getContent()),
				CoreMatchers.containsString(ERR_MSG_FORBIDDEN_CITY));
	}

	@Test
	public void testCreateUserAndDuplicate() throws Exception {
		HttpResponse execute = createUser("user", "foo@fff.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(200));
		assertThat(IOUtils.toString(execute.getEntity().getContent()), CoreMatchers.containsString("id"));

		execute = createUser("user", "foo@fff.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
		assertThat(IOUtils.toString(execute.getEntity().getContent()),
				CoreMatchers.containsString(ERR_MSG_NAME_IS_TAKEN));
	}

	@Test
	public void testCreateUserWithSpamMail() throws Exception {
		HttpResponse execute = createUser("admin", "foo@trashmail.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
		assertThat(IOUtils.toString(execute.getEntity().getContent()),
				CoreMatchers.containsString(ERR_MSG_EMAIL_NOT_ALLOWED));
	}

	@Test
	public void testCreateUserWithUnknownName() throws Exception {
		HttpResponse execute = createUser("anon", "foo@bar.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
		assertThat(IOUtils.toString(execute.getEntity().getContent()),
				CoreMatchers.containsString(ERR_MSG_NO_GROUP_FOUND_FOR_USER));
	}

	@Test
	public void testCreateUserWithForbiddenName() throws Exception {
		HttpResponse execute = createUser("vader", "foo@foo.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
		assertThat(IOUtils.toString(execute.getEntity().getContent()),
				CoreMatchers.containsString(ERR_MSG_NAME_NOT_ALLOWED));
	}

	@Test
	public void testFetchUserEvents() throws Exception {
		HttpResponse execute = get("/users");
		assertThat(execute.getStatusLine().getStatusCode(), is(200));
		System.out.println(IOUtils.toString(execute.getEntity().getContent()));
	}

	private HttpResponse createAddress(String city) throws IOException {
		Address address = new Address("", city, "12345");
		return createByPost("/addresses", new CreateUserRequest("admin", "foo@dsaf.com", address));
	}

	private HttpResponse createUser(String name, String email) throws IOException {
		return createByPost("/users", new CreateUserRequest(name, email, null));
	}

	private HttpResponse createByPost(String path, Object entity) throws IOException {
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost("http://localhost:8080" + path);
		String admin = Json.encode(entity);
		httpPost.setEntity(new StringEntity(admin));
		return httpclient.execute(httpPost);
	}

	private HttpResponse get(String path) throws IOException {
		HttpClient httpclient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet("http://localhost:8080" + path);
		return httpclient.execute(httpGet);
	}
}