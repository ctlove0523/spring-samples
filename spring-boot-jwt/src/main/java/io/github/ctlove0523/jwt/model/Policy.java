package io.github.ctlove0523.jwt.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author chentong
 */
@Getter
@Setter
@ToString
public class Policy {
	private String name;
	private String type;
	private String url;
	private String action;

	@JsonProperty("allowed_roles")
	private List<String> allowedRoles;
}
