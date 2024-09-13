package org.zerock.myapp;

import lombok.Data;
import lombok.ToString;


@Data
public class Entity2 {
	
	private String name;
	
	@ToString.Exclude
	private Entity1 entity;
	

} // end class
