package com.ubcmmhcsoftware.ubcmmhc_web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@Slf4j
public class UbcmmhcWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(UbcmmhcWebApplication.class, args);
    }

    // @Bean
    // CommandLineRunner initRoles(RoleRepository roleRepository, UserRepository
    // userRepository,
    // AppProperties appProperties) {
    // return args -> {
    // if (!roleRepository.existsByName((RoleEnum.ROLE_BLOG_EDITOR))) {
    // // roleRepository.save(new Role(RoleEnum.ROLE_USER));
    // // roleRepository.save(new Role(RoleEnum.ROLE_ADMIN));
    // // roleRepository.save(new Role(RoleEnum.ROLE_SUPERADMIN));
    // roleRepository.save(new Role(RoleEnum.ROLE_BLOG_EDITOR));
    // roleRepository.save(new Role(RoleEnum.ROLE_BLOG_MANAGER));
    // log.info("Initialized roles in database");
    // }
    //
    // userRepository.findUserByEmail("jefef358@gmail.com").ifPresent(user -> {
    // roleRepository.findByName(RoleEnum.ROLE_ADMIN).ifPresent(adminRole -> {
    // if (!user.getUser_roles().contains(adminRole)) {
    // user.getUser_roles().add(adminRole);
    // userRepository.save(user);
    // log.info("Promoted {} to ADMIN role", "jefef358@gmail.com");
    // }
    // });
    // });
    //
    // };
    // }
}
