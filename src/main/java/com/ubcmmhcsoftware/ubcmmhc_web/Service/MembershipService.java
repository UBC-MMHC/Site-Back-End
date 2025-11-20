package com.ubcmmhcsoftware.ubcmmhc_web.Service;

import com.ubcmmhcsoftware.ubcmmhc_web.DTO.MemberShipDto;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.Membership;
import com.ubcmmhcsoftware.ubcmmhc_web.Entity.User;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.MembershipRepository;
import com.ubcmmhcsoftware.ubcmmhc_web.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserRepository userRepository;

    private final MembershipRepository membershipRepository;

    @Transactional
    public ResponseEntity<?> RegisterMember(MemberShipDto memberShipDto) {
        User user = userRepository.findUserByEmail(memberShipDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(memberShipDto.getEmail()));

        if (membershipRepository.existsByUser(user)) {
            throw new RuntimeException("User is already a member");
        }

        if (StringUtils.hasText(memberShipDto.getFirstName())) {
            user.setName(memberShipDto.getFirstName());
        }

        if (StringUtils.hasText(memberShipDto.getStudentId())) {
            user.setStudentId(memberShipDto.getStudentId());
        }

        // TODO In future call the add to newsletter call
        if (memberShipDto.getNewsletter() != null) {
            user.setNewsletterSubscription(memberShipDto.getNewsletter());
        }

        // TODO In future call the add to instachat call
        String insta = memberShipDto.getInstagram();
        if (StringUtils.hasText(insta)) {
            user.setInstagram(insta);
            if (memberShipDto.getJoinInstaChat() != null) {
                user.setInstaChat(memberShipDto.getJoinInstaChat());
            }
        }

        Membership membership = new Membership();
        membership.setUser(user);

        userRepository.save(user);
        membershipRepository.save(membership);

        return ResponseEntity.ok().build();
    }

}
