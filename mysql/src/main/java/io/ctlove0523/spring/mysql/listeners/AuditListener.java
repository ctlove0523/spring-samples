package io.ctlove0523.spring.mysql.listeners;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditListener {
	private static final Logger log = LoggerFactory.getLogger(AuditListener.class);

	@PrePersist
	public void prePersist(Object o) {
		System.out.println("pre persist");
	}

	@PreRemove
	public void preRemove(Object o) {
		System.out.println("pre remove");
	}

	@PostPersist
	public void postPersist(Object o) {
		System.out.println("post persist");
	}

	@PostRemove
	public void postRemove(Object o) {
		System.out.println("post remove");
	}

	@PreUpdate
	public void preUpdate(Object o) {
		System.out.println("pre update");
	}

	@PostUpdate
	public void postUpdate(Object o) {
		System.out.println("post Update");
	}

	@PostLoad
	public void postLoad(Object o) {
		System.out.println("post load");
	}
}
