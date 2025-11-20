package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MemberShipDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/membership")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/register")
    public ResponseEntity<?> registerMembership(@RequestBody MemberShipDto memberShipDto) {
        return membershipService.RegisterMember(memberShipDto);
    }
}
