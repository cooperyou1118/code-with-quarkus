package org.acme.login;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

/**
 * 애플리케이션 전역 세션(Session) 유지 및 보안 설정을 담당하는 클래스
 */
@ApplicationScoped // [필수 추가] Quarkus(CDI) 컨테이너가 이 빈을 관리하고 이벤트를 감지할 수 있도록 지정
public class SessionConfig {

    @Inject 
    Vertx vertx; // Vert.x 인스턴스 자동 주입 (메모리 기반 로컬 세션 저장소를 만들기 위해 필요)

    /**
     * HTTP 라우터가 초기화될 때 전역 세션 핸들러를 가로채서 설정합니다.
     */
    public void init(@Observes Router router) {
        router.route().handler(
            SessionHandler
                .create(LocalSessionStore.create(vertx)) // 서버 메모리를 활용한 로컬 세션 저장소 생성
                .setSessionTimeout(60 * 60 * 1000L)      // 세션 유지 시간 설정: 1시간 (60분 * 60초 * 1000밀리초)
                .setCookieHttpOnlyFlag(true)             // 보안 설정: XSS 해킹 공격 방지를 위해 프론트(JS)에서 쿠키 접근 원천 차단
        );
    }
}