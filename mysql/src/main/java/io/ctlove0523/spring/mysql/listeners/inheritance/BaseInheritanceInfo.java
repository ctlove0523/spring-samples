package io.ctlove0523.spring.mysql.listeners.inheritance;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;

import org.hibernate.annotations.GenericGenerator;

@EntityListeners(value = InheritanceInfoListener.class)
public class BaseInheritanceInfo {
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	public String id;

	public String name;

	@PrePersist
	public void prePersist() {
		System.out.println("BaseInheritanceInfo：pre persist");
	}

	@PostPersist
	public void postPersist() {
		System.out.println("BaseInheritanceInfo：post persist");
	}

	String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}
}
