package io.ctlove0523.spring.mysql.listeners.inheritance;

import javax.persistence.PostPersist;
import javax.persistence.PrePersist;

public class InheritanceInfoListener {

	@PrePersist
	public void prePersist(Object o) {
		if (o instanceof BaseInheritanceInfo) {
			System.out.println("BaseInheritanceInfo listener: pre persist");
		}
		if (o instanceof InheritanceInfo) {
			System.out.println("InheritanceInfo listener : pre persist");
		}
	}

	@PostPersist
	public void postPersist(Object o) {
		if (o instanceof BaseInheritanceInfo) {
			System.out.println("BaseInheritanceInfo listener : post persist");
		}
		if (o instanceof InheritanceInfo) {
			System.out.println("InheritanceInfo listener : post persist");
		}
	}
}
