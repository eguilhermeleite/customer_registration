package com.anna.domain;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tbl_user")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	 @Id   
	 @EqualsAndHashCode.Include
	 private String cpf;
	 private String name;
	 private String email;

}