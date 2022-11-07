package com.hanghae.baedalfriend.service;

import com.hanghae.baedalfriend.dto.requestdto.LoginRequestDto;
import com.hanghae.baedalfriend.dto.requestdto.MemberRequestDto;
import com.hanghae.baedalfriend.dto.requestdto.TokenDto;
import com.hanghae.baedalfriend.dto.responsedto.MemberResponseDto;
import com.hanghae.baedalfriend.dto.responsedto.ResponseDto;
import com.hanghae.baedalfriend.domain.Member;
import com.hanghae.baedalfriend.jwt.TokenProvider;
import com.hanghae.baedalfriend.repository.MemberRepository;
import com.hanghae.baedalfriend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public ResponseDto<?> createMember(MemberRequestDto requestDto) {

        //이메일 중복 체크
        if (null != isPresentMember(requestDto.getEmail())) {
            return ResponseDto.fail("DUPLICATED_EMAIL",
                    "중복된 이메일 입니다.");
        }

        //패스워드 일치 체크
        if(!Objects.equals(requestDto.getPasswordConfirm(), requestDto.getPassword())){
            return ResponseDto.fail("PASSWORDCONFIRM_FAIL",
                    "패스워드가 일치하지 않습니다.");
        }

        Member member = Member.builder()
                .nickname(requestDto.getNickname())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .email(requestDto.getEmail())
                .address(requestDto.getAddress())
                .profileURL(requestDto.getProfileURL())
                .role(requestDto.getRole())
                .build();
        memberRepository.save(member);
        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .email(member.getEmail())
                        .address(member.getAddress())
                        .profileURL(member.getProfileURL())
                        .role(member.getRole())
                        .build()
        );
    }

    // 로그인
    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = isPresentMember(requestDto.getEmail());

        // null값 사용자 유효성 체크
        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.");
        }

        // 비밀번호 사용자 유효성 체크
        if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) {
            return ResponseDto.fail("INVALID_MEMBER", "사용자를 찾을 수 없습니다.");
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);

        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .email(member.getEmail())
                        .address(member.getAddress())
                        .role(member.getRole())
                        .profileURL(member.getProfileURL())
                        .build()
        );
    }

    @Transactional
    public Member isPresentMember(String email) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        return optionalMember.orElse(null);
    }

    // 로그아웃
    public ResponseDto<?> logout(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("Refresh_Token"))) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        Member member = (Member) tokenProvider.getMemberFromAuthentication();

        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "사용자를 찾을 수 없습니다.");
        }
        return tokenProvider.deleteRefreshToken(member);
    }

    // 헤더에 담기는 토큰
    private void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("Refresh_Token", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
    }

    // 리이슈
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        if (!tokenProvider.validateToken(request.getHeader("Refresh_Token"))) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Member member = refreshTokenRepository.findByValue(request.getHeader("Refresh_Token")).get().getMember();
        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);
        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .address(member.getAddress())
                        .profileURL(member.getProfileURL())
                        .role(member.getRole())
                        .email(member.getEmail())
                        .build()
        );
    }
}