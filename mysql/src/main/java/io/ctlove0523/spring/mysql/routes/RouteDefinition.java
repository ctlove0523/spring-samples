package io.ctlove0523.spring.mysql.routes;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import io.ctlove0523.spring.mysql.listeners.AuditListener;
import org.hibernate.annotations.GenericGenerator;

/**
 * @author chentong
 */
@Entity
@Table(name = "t_route_definition")
@EntityListeners(value = AuditListener.class)
public class RouteDefinition {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	private String id;

	private String uri;

	private int routeOrder;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public int getRouteOrder() {
		return routeOrder;
	}

	public void setRouteOrder(int routeOrder) {
		this.routeOrder = routeOrder;
	}
}
