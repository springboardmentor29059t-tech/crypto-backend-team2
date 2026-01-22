package com.crypto.portfolio.dto;

public class UserDto {
    private Long id;
    private String fullName;
    private String email;

    private String profileImage;

    public UserDto(Long id, String fullName, String email, String profileImage) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.profileImage = profileImage;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImage() {
        return profileImage;
    }
}
