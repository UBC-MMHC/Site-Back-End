package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CsrfController {

    @GetMapping
    public ResponseEntity<?> getCsrfToken() {
        return ResponseEntity.status(HttpStatus.OK).build();

    }
}
