package org.acme.common;

import org.acme.champion.Champion;
import org.acme.login.User;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

/**
 * 애플리케이션 시작 시 테스트용 초기 데이터를 데이터베이스에 자동으로 삽입하는 클래스
 */
@ApplicationScoped
public class DataSeeder {

    @Transactional
    void onStart(@Observes StartupEvent ev) { // Quarkus 구동 시 이벤트 감지

        // 1. 테스트용 User 데이터 초기화 (중복 방지)
        if (User.count() == 0) {
            User guest = new User();
            guest.username = "guest";
            // 암호화된 비밀번호 설정 (sha256 적용 결과)
            guest.password = "96CAE35CE8A9B0244178BF28E4966C2CE1B8385723A96A6B838858CDD6CA0A1E";
            guest.persist();
        }

        // 2. 챔피언 기본 데이터 초기화
        if (Champion.count() == 0) {
            persist("아트록스", "전사", "탑");
            persist("사일러스", "마법사", "정글/미드");
            persist("애니비아", "마법사", "미드");
            persist("브라이어", "전사", "정글");
            persist("잭스", "전사", "탑");
            persist("징크스", "원거리딜러", "원딜");
            persist("야스오", "전사", "미드/탑");
            persist("리신", "전사", "정글");
            persist("티모", "마법사", "탑");
            persist("케인", "암살자", "정글");
            persist("루시안", "원거리딜러", "원딜/미드");
        }
    }

    /**
     * 챔피언 객체를 생성하고 데이터베이스에 저장하는 헬퍼 메서드
     */
    private void persist(String name, String role, String line) {
        Champion c = new Champion();
        c.name = name;
        c.role = role;
        c.line = line;
        c.persist();
    }
}