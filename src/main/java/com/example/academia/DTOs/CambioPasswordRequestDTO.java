package com.example.academia.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CambioPasswordRequestDTO {
    private String username;
    private String currentPassword;
    private String newPassword;
}
