package com.traininghub.participant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "participants", indexes = {
        @Index(name = "idx_participants_last_name", columnList = "last_name"),
        @Index(name = "idx_participants_active", columnList = "active")
})
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotBlank @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @NotBlank @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @NotBlank @Column(name = "tax_code", nullable = false, unique = true, length = 16)
    private String taxCode;
    @NotNull @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;
    @NotBlank @Email @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(length = 30)
    private String phone;
    @Column(name = "education_level", length = 100)
    private String educationLevel;
    @Column(name = "employment_status", length = 100)
    private String employmentStatus;
    @NotNull @Column(nullable = false)
    private Boolean active = true;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEducationLevel() { return educationLevel; }
    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}