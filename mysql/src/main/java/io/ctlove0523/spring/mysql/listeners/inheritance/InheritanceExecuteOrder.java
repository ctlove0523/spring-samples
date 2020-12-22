package io.ctlove0523.spring.mysql.listeners.inheritance;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InheritanceExecuteOrder {
	@Autowired
	private InheritanceInfoRepository repository;

	@PostConstruct
	public void init() {
		InheritanceInfo inheritanceInfo = new InheritanceInfo();
		inheritanceInfo.setLevel(1);
		inheritanceInfo.setName("test");

		repository.save(inheritanceInfo);
	}
}
