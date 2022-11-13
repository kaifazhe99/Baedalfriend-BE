package com.hanghae.baedalfriend.controller;

import com.hanghae.baedalfriend.dto.requestdto.EventRequestDto;
import com.hanghae.baedalfriend.dto.responsedto.ResponseDto;
import com.hanghae.baedalfriend.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RequestMapping("/v1")
@RestController
public class EventController {

    private final EventService eventService;

    //공지사항(이벤트) 등록
    @PostMapping("/events")
    public ResponseDto<?> createEvent(@RequestBody EventRequestDto requestDto, HttpServletRequest request) {
        return eventService.createEvent(requestDto, request);
    }

    //공지사항(이벤트) 전체 조회
    @GetMapping("/events")
    public ResponseDto<?> getAllEvents(Pageable pageable) {
        return eventService.getAllEvents(pageable);
    }

    //공지사항(이벤트) 1개 조회
    @GetMapping("/events/{eventId}")
    public ResponseDto<?> getEvent(@PathVariable Long eventId) {
        return eventService.getEvent(eventId);
    }

    //공지사항(이벤트) 수정
    @PutMapping("/events/{eventId}")
    public ResponseDto<?> updateEvent(@PathVariable Long eventId,@RequestBody EventRequestDto requestDto, HttpServletRequest request) {
        return eventService.updateEvent(eventId, requestDto, request);
    }

    //공지사항(이벤트) 삭제
    @DeleteMapping("/events/{eventId}")
    public ResponseDto<?> deleteEvent(@PathVariable Long eventId, HttpServletRequest request) {
        return eventService.deleteEvent(eventId, request);
    }

}