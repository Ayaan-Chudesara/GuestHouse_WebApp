package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO saveUser(UserDTO dto);
    void deleteUser(Long id);
    UserDTO updateUser(Long id, UserDTO dto);
    String registerUser(UserDTO userDTO);
}
