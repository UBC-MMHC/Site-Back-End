package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api")
public class CsrfController {

    @GetMapping("/csrf-token")
    public ResponseEntity<?> getCsrfToken() {
        return ResponseEntity.status(HttpStatus.OK).build();

    }
}
