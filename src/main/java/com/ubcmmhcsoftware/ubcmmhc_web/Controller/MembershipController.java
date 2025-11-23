package com.ubcmmhcsoftware.ubcmmhc_web.Controller;

import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MemberShipDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/membership")
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping("/register")
    public ResponseEntity<?> registerMembership(@RequestBody MemberShipDto memberShipDto) {
        membershipService.registerMember(memberShipDto);
        return ResponseEntity.ok(Map.of("message", "Membership registered successfully"));
    }
}
