package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * 서버 정상 구동 여부를 간단하게 확인하기 위한 헬스체크용 엔드포인트
 */
@Path("/hello")
public class GreetingResource {

    /**
     * GET /hello : 서버가 살아있는지 문자열을 반환하여 확인합니다.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Quarkus REST Server is running successfully!"; // 프로젝트 통일감을 위해 메시지 수정
    }
}