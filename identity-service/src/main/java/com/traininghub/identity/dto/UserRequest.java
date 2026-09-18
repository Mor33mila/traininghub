package com.traininghub.identity.dto;

import com.traininghub.identity.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRequest {
    @NotBlank @Size(max = 80) private String username;
    @NotBlank @Size(max = 100) private String firstName;
    @NotBlank @Size(max = 100) private String lastName;
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, max = 100) private String password;
    @NotNull private UserRole role;

    public String getUsername() { return username; }
    public void setUsername(String value) { username = value; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { firstName = value; }
    public String getLastName() { return lastName; }
    public void setLastName(String value) { lastName = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPassword() { return password; }
    public void setPassword(String value) { password = value; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole value) { role = value; }
}