package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-api")
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public List<User> findAllUsers() {
        return userService.findAllUsers();
    }
}
