package com.digitalhelper.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String kakaoId;

    private String nickname;
    private String email;

    protected User() {}

    public User(String kakaoId, String nickname, String email) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getKakaoId() { return kakaoId; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }

    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
    }
}
