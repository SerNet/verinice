package sernet.hui.common.connect;

public class PropertyChangedEvent {
	private Entity entity;
	private Property property;
	private Object source;
	

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Property getProperty() {
		return property;
	}

	public PropertyChangedEvent(Entity entity, Property property, Object source) {
		super();
		this.entity = entity;
		this.property = property;
		this.source = source;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

}
