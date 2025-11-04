package com.ubcmmhcsoftware.ubcmmhc_web;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Role;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Enum.RoleEnum;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.RoleRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

@SpringBootApplication
public class UbcmmhcWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(UbcmmhcWebApplication.class, args);
	}

    // Add test users to the database on startup
//    @Bean
//    CommandLineRunner initUsers(UserRepository repository) {
//        return args -> {
//            System.out.println("Creating test users...");
//            repository.save(new User());
//            repository.save(new User());
//            repository.save(new User());
//            System.out.println("Test users created!");
//        };
//    }

    // Pre ADD roles to database
//    @Bean
//    CommandLineRunner initRoles(RoleRepository roleRepository) {
//        return args -> {
//            if (roleRepository.existsByName(RoleEnum.ROLE_USER)) {
//                roleRepository.save(new Role(RoleEnum.ROLE_USER));
//            }
//            if (roleRepository.existsByName(RoleEnum.ROLE_ADMIN)) {
//                roleRepository.save(new Role(RoleEnum.ROLE_ADMIN));
//            }
//            if (roleRepository.existsByName(RoleEnum.ROLE_SUPERADMIN)) {
//                roleRepository.save(new Role(RoleEnum.ROLE_SUPERADMIN));
//            }
//        };
//    }

}
