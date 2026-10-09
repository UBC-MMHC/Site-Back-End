package com.ubcmmhcsoftware.newsletter.controller;

import com.ubcmmhcsoftware.newsletter.dto.SubscribeToNewsletterRequest;
import com.ubcmmhcsoftware.newsletter.service.NewsletterService;
import jakarta.validation.Valid;
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
    public ResponseEntity<String> addEmail(@Valid @RequestBody SubscribeToNewsletterRequest request) {
        newsletterService.addEmail(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body("Email subscribed to the newsletter");
    }
}
