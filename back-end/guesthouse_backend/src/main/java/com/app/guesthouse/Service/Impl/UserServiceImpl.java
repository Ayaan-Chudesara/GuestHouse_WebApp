package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepo userRepo;

    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User maptoEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRole(dto.getRole());
        return user;
    }

    public UserDTO maptoDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());

        return dto;
    }

    @Override
    public List<UserDTO> getAllUsers(){
        List<User> userList = userRepo.findAll();
        List<UserDTO> users = new ArrayList<>();

        for(User u : userList){
            UserDTO dto = this.maptoDTO(u);
            users.add(dto);
        }
        return users;
    }

    @Override
    public UserDTO getUserById(Long id){
        User user = userRepo.findById(id).orElse(null);
        UserDTO dto = this.maptoDTO(user);
        return dto;
    }

    @Override
    public UserDTO saveUser(UserDTO dto){
        User user = this.maptoEntity(dto);
        User savedUser = userRepo.save(user);
        UserDTO savedUserDTO = this.maptoDTO(savedUser);
        return savedUserDTO;
    }

    @Override
    public void deleteUser(Long id){
        User user = userRepo.findById(id).orElse(null);
        userRepo.delete(user);
        
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO dto){
        User user = userRepo.findById(id).orElse(null);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        User updatedUser = userRepo.save(user);
        UserDTO updatedUserDTO = this.maptoDTO(updatedUser);
        return updatedUserDTO;
    }

}
