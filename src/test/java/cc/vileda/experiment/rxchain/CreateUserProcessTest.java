package cc.vileda.experiment.rxchain;

import cc.vileda.experiment.common.*;
import cc.vileda.experiment.common.command.CreateAddressCommand;
import cc.vileda.experiment.common.command.CreateUserCommand;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class CreateUserProcessTest {

	@Test
	public void testCreateUser() throws Exception {
		CreateUserProcess process = new CreateUserProcess();

		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.createUser(new CreateUserCommand("foo", "bar")).subscribe(testSubscriber);
		testSubscriber.assertNoErrors();
	}

	@Test
	public void testCreateAddress() throws Exception {
		CreateUserProcess process = new CreateUserProcess();

		TestSubscriber<Address> testSubscriber = new TestSubscriber<>();
		process.createAddress(new CreateAddressCommand("city1", "")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testCreateForbiddenAddress() throws Exception {
		CreateUserProcess process = new CreateUserProcess();

		TestSubscriber<Address> testSubscriber = new TestSubscriber<>();
		process.createAddress(new CreateAddressCommand("", "")).subscribe(testSubscriber);
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

		TestSubscriber<User> testSubscriber = new TestSubscriber<>();
		process.sendMail(createUser("admin", "foo@bar.de")).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testThrowIfSpamEmail() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserCommand> testSubscriber = new TestSubscriber<>();

		CreateUserCommand createUserRequest = createUserRequest("user1@trashmail.com");
		process.throwIfSpamEmail(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testDoNotThrowIfNotSpamEmail() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserCommand> testSubscriber = new TestSubscriber<>();

		CreateUserCommand createUserRequest = createUserRequest("user1@example.com");
		process.throwIfSpamEmail(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testThrowIfForbiddenName() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserCommand> testSubscriber = new TestSubscriber<>();

		CreateUserCommand createUserRequest = createUserRequest("vader", "foo@bar.cc");
		process.throwIfForbiddenName(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testDoNotThrowIfAllowedName() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserCommand> testSubscriber = new TestSubscriber<>();

		CreateUserCommand createUserRequest = createUserRequest("admin", "foo@bar.cc");
		process.throwIfForbiddenName(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	@Test
	public void testThrowIfNameTaken() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		process.getUserController().save("admin", "foo@bar.tld");

		TestSubscriber<CreateUserCommand> testSubscriber = new TestSubscriber<>();

		CreateUserCommand createUserRequest = createUserRequest("admin", "foo@bar.cc");
		process.throwIfNameTaken(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), not(0));
	}

	@Test
	public void testDoNotThrowIfNameNotTaken() throws Exception {
		CreateUserProcess process = new CreateUserProcess();
		TestSubscriber<CreateUserCommand> testSubscriber = new TestSubscriber<>();

		CreateUserCommand createUserRequest = createUserRequest("admin", "foo@bar.cc");
		process.throwIfNameTaken(createUserRequest).subscribe(testSubscriber);
		assertThat(testSubscriber.getOnErrorEvents().size(), is(0));
	}

	private CreateUserCommand createUserRequest(String email) {
		return createUserRequest("admin", email);
	}

	private CreateUserCommand createUserRequest(String name, String email, String city) {
		return new CreateUserCommand(
				name,
				email
		);
	}

	private CreateUserCommand createUserRequest(String name, String email) {
		return createUserRequest(name, email, "city2");
	}

	private User createUser(String name, String email) {
		return new User(null, name, email, null, null);
	}

	private User createUser(String name) {
		return createUser(name, null);
	}
}