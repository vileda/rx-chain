package cc.vileda.resx.event;

import lombok.Setter;

import java.util.UUID;

@Setter
public class PersistableEvent<T> {
	private String id;
	private Class<T> clazz;
	private String payload;

	public PersistableEvent()
	{
		id = UUID.randomUUID().toString();
	}

	public PersistableEvent(Class<T> clazz, String payload) {
		this();
		this.clazz = clazz;
		this.payload = payload;
	}

	public Class<T> getClazz() {
		return clazz;
	}

	public String getPayload() {
		return payload;
	}

	public String getId() {
		return id;
	}
}
