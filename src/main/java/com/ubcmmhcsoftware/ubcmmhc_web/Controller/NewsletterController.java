package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.DTO.SubscribeToNewsletterRequest;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/newsletter")
public class NewsletterController {
    private final NewsletterService newsletterService;

    @PostMapping("/add-email")
    public ResponseEntity<?> login(@RequestBody SubscribeToNewsletterRequest request) {
        newsletterService.addEmail(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body("Email subscribed to the newsletter");
    }
}
