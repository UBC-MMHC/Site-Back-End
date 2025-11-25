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
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.UUID;

@SpringBootApplication
@EnableAsync
public class UbcmmhcWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(UbcmmhcWebApplication.class, args);
	}

    // Pre ADD roles to database
    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            if(!roleRepository.existsByName(RoleEnum.ROLE_USER)) {
                roleRepository.save(new Role(RoleEnum.ROLE_USER));
                roleRepository.save(new Role(RoleEnum.ROLE_ADMIN));
                roleRepository.save(new Role(RoleEnum.ROLE_SUPERADMIN));
            }
        };
    }

}
