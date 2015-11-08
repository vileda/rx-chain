package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.Address;
import cc.vileda.experiment.common.CreateUserRequest;
import io.vertx.core.json.Json;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RestApiIT {

	@BeforeClass
	public static void beforeClass() {
		new VertxMain().run();
	}

	@Test
	public void testCreateUser() throws Exception {
		HttpResponse execute = createUser("user", "foo@fff.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(200));
		assertThat(IOUtils.readFully(execute.getEntity().getContent(), -1, false).length, is(36));
	}

	@Test
	public void testCreateUserWithSpamMail() throws Exception {
		HttpResponse execute = createUser("admin", "foo@trashmail.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
	}

	@Test
	public void testCreateUserWithUnknownName() throws Exception {
		HttpResponse execute = createUser("anon", "foo@bar.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
	}

	@Test
	public void testCreateUserWithForbiddenName() throws Exception {
		HttpResponse execute = createUser("vader", "foo@foo.com");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
	}

	@Test
	public void testCreateAddress() throws Exception {
		HttpResponse execute = createAddress("city1");
		assertThat(execute.getStatusLine().getStatusCode(), is(200));
		assertThat(IOUtils.readFully(execute.getEntity().getContent(), -1, false).length, is(36));
	}

	@Test
	public void testCreateForbiddenAddress() throws Exception {
		HttpResponse execute = createAddress("city2");
		assertThat(execute.getStatusLine().getStatusCode(), is(500));
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
}