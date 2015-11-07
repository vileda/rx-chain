package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsNot;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class CreateUserProcessTest {

	@Test
	public void testCreateUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.createUser(new CreateUserRequest("foo", "bar", null)).subscribe(testSubscriber);
		testSubscriber.assertNoErrors();
	}

	@Test
	public void testCreateAddress() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<Address> testSubscriber = new TestSubscriber<>();
		process.createAddress(new User(), new Address("", "city1", "")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testCreateForbiddenAddress() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<Address> testSubscriber = new TestSubscriber<>();
		process.createAddress(new User(), new Address()).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testAddKnownAdminUserToGroup() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.addUserToGroup(createUser("admin")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testAddKnownUserToGroup() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.addUserToGroup(createUser("user")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testAddUnknownUserToGroup() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.addUserToGroup(createUser("anon")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testSendMail() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		process.sendMail(createUser("admin", "foo@bar.de")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testRunCreateUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		Response response = process.run("admin");
		assertThat(response, instanceOf(SuccessResponse.class));
	}

	@Test
	public void testRunCreateCityFailingUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		CreateUserRequest createUserRequest = new CreateUserRequest(
				"admin",
				"user1@example.com",
				new Address("", "city2", "12345")
		);
		Response response = process.runCreateUser(createUserRequest);
		assertThat(response, instanceOf(ErrorResponse.class));
	}

	@Test
	public void testRunCreateUnknownFailingUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		Response response = process.run("anon");
		assertThat(response, instanceOf(ErrorResponse.class));
	}

	private User createUser(String name, String email) {
		return new User(null, name, email, null, null);
	}

	private User createUser(String name) {
		return createUser(name, null);
	}
}