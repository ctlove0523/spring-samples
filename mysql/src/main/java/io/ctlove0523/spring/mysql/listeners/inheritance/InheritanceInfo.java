package io.ctlove0523.spring.mysql.listeners.inheritance;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "t_inheritance_info")
@EntityListeners(value = InheritanceInfoListener.class)
public class InheritanceInfo extends BaseInheritanceInfo {
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	public String id;

	public String name;

	private int level;

	@PrePersist
	public void prePersist() {
		System.out.println("InheritanceInfo：pre persist");
	}

	@PostPersist
	public void postPersist() {
		System.out.println("InheritanceInfo：post persist");
	}

	int getLevel() {
		return level;
	}

	void setLevel(int level) {
		this.level = level;
	}
}
