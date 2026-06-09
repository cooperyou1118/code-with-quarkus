package org.acme.login;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

/**
 * 회원(User) 정보를 데이터베이스에 매핑하는 JPA 엔티티 클래스
 */
@Entity
@Table(name = "users") // 데이터베이스 예약어(User) 충돌을 막기 위해 테이블명을 "users"로 강제 지정
public class User extends PanacheEntity {

    // ==========================================
    // 1. 데이터베이스 컬럼 정의
    // ==========================================

    @Column(unique = true, nullable = false) // [중요] 아이디 중복 방지 및 필수값 지정
    public String username;

    @Column(nullable = false)
    public String password; // SHA-256로 암호화된 해시값 저장

    @Column(unique = true) // 이메일 중복 방지
    public String email;

    public String phone; // 연락처

    public String profileImage; // 저장된 프로필 사진 파일명 (UUID 기반)

    // ==========================================
    // 2. 데이터베이스 편의 조회 메서드
    // ==========================================

    /**
     * 아이디(username)로 특정 유저를 검색합니다.
     */
    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }

    /**
     * 이메일(email)로 특정 유저를 검색합니다.
     */
    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}