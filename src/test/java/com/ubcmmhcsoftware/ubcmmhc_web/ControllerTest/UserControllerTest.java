package com.ubcmmhcsoftware.ubcmmhc_web.ControllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ubcmmhcsoftware.ubcmmhc_web.Controller.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.test.web.client.MockRestServiceServer;


@RestClientTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockRestServiceServer mockServer;

    @Autowired
    UserController controller;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void userInfo() {

    }

}