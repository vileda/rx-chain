package cc.vileda.experiment.common.event;

public class Event<T> {
	private Class<T> clazz;
	private String payload;

	public Event(Class<T> clazz, String payload) {
		this.clazz = clazz;
		this.payload = payload;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public String getPayload() {
		return payload;
	}
}
