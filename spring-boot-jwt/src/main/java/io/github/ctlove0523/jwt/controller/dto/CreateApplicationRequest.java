package io.github.ctlove0523.jwt.controller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationRequest {
	private String name;
	private String description;
}
