package com.example.bossbot.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{
    private UserRepository userRepository;

    @Override
    @Transactional
    public List<UserDto> getUsers() {
        List<User> users = userRepository.findAll();
        List <UserDto> userDtos = users.stream().map(UserDto::new).toList();

        return userDtos;
    }
}
