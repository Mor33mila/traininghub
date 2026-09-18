package com.traininghub.participant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class ParticipantRequest {
    @NotBlank @Size(max = 100) private String firstName;
    @NotBlank @Size(max = 100) private String lastName;
    @NotBlank @Size(max = 16) private String taxCode;
    @NotNull private LocalDate birthDate;
    @NotBlank @Email private String email;
    @Size(max = 30) private String phone;
    @Size(max = 100) private String educationLevel;
    @Size(max = 100) private String employmentStatus;

    public String getFirstName() { return firstName; }
    public void setFirstName(String value) { firstName = value; }
    public String getLastName() { return lastName; }
    public void setLastName(String value) { lastName = value; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String value) { taxCode = value; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate value) { birthDate = value; }
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String value) { educationLevel = value; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String value) { employmentStatus = value; }
}