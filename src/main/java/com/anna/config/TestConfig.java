package com.anna.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.anna.domain.User;
import com.anna.repository.UserRepository;

@Configuration
@Profile("test")
public class TestConfig implements CommandLineRunner {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public void run(String... args) throws Exception {
		
		User user1 = new User("22117214860", "Edvaldo Leite", "eguilhermeleite@gmail.com");
		User user2 = new User("27158787885", "Luciene Leite", "lsilvaleite14@gmail.com");
		userRepository.saveAll(Arrays.asList(user1,user2));
		
	}
	

}
