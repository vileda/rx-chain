package cc.vileda.experiment.common;

public final class Globals {
	public static final String CREATE_USER_COMMAND_ADDRESS = "command.user.create";
	public static final String CHANGE_USER_EMAIL_COMMAND_ADDRESS = "command.user.change.email";
	public static final String CREATE_ADDRESS_COMMAND_ADDRESS = "command.address.create";

	public static final String USER_CREATED_EVENT_ADDRESS = "event.user.created";
	public static final String USER_EMAIL_CHANGED_EVENT_ADDRESS = "event.user.changed.email";
	public static final String ADDRESS_CREATED_EVENT_ADDRESS = "event.address.created";
	public static final String CREATING_USER_FAILED_EVENT_ADDRESS = "event.user.created.failed";
	public static final String CREATING_ADDRESS_FAILED_EVENT_ADDRESS = "event.address.created.failed";
	public static final String ERROR_HEADER = "error";
	public static final String HEADER_TRUE = "true";
	public static final String ERR_MSG_EMAIL_NOT_ALLOWED = "email not allowed";
	public static final String ERR_MSG_NAME_NOT_ALLOWED = "name not allowed";
	public static final String ERR_MSG_NAME_IS_TAKEN = "name is taken";
	public static final String ERR_MSG_FORBIDDEN_CITY = "forbidden city";
	public static final String ERR_MSG_NO_GROUP_FOUND_FOR_USER = "no group found for user ";
}
