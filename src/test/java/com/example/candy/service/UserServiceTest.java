package com.example.candy.service;

import com.example.candy.controller.user.dto.UserInfoResponseDto;
import com.example.candy.domain.candy.CandyHistory;
import com.example.candy.domain.candy.EventType;
import com.example.candy.domain.user.User;
import com.example.candy.service.user.UserService;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private UserService userService;

    private String email;
    private String password;
    private String parentPassword;
    private String name;
    private String phone;
    private String birth;

    @BeforeAll
    void setUp() {
        email = "test@gmail.com";
        password = "1234";
        parentPassword = "abcd";
        name = "한글";
        phone = "01012345678";
        birth = "19950302";
    }

    @Test
    @Transactional
    void 사용자_추가_and_캔디_초기화() {
        User user = userService.join(email, true, password, parentPassword, name, phone, birth);
        assertNotNull(user);
        assertEquals(user.getEmail(), email);
        assertEquals(user.getCandyHistories().get(0).getEventType(), EventType.INIT);
    }

    @Test
    @Transactional
    void 사용자_추가_when_이메일체크X() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.join(email, false, password, parentPassword, name, phone, birth);
        });
    }

    @Test
    @Transactional
    void 비밀번호_변경() throws NotFoundException {
        User user = userService.join(email, true, password, parentPassword, name, phone, birth);
        userService.changePassword(user.getId(), "abcd", password);
        User user2 = userService.findById(user.getId()).get();
        assertEquals("abcd", user2.getPassword());
    }

    @Test
    @Transactional
    void 유저정보_변경() throws NotFoundException {
        User user = userService.join(email, true, password, parentPassword, name, phone, birth);
        UserInfoResponseDto savedUser = userService.changeUserInfo(user.getId(), "lee", "010-1111-1111", "123456");
        assertEquals(user.getPhone(), savedUser.getPhone());
    }
}