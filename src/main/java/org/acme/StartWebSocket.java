package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * 실시간 양방향 통신(웹소켓)을 처리하는 엔드포인트
 * (주의: 현재는 콘솔 출력만 작동하며, 클라이언트로 메시지를 다시 보내는 로직은 구현되지 않음)
 */
@ServerEndpoint("/start-websocket/{name}")
@ApplicationScoped
public class StartWebSocket {

    /**
     * 클라이언트(브라우저)가 웹소켓에 처음 연결되었을 때 실행됩니다.
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {
        System.out.println("onOpen> " + name + " 님이 접속했습니다.");
    }

    /**
     * 클라이언트가 웹소켓 연결을 끊고 나갔을 때 실행됩니다.
     */
    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {
        System.out.println("onClose> " + name + " 님이 퇴장했습니다.");
    }

    /**
     * 통신 중 에러가 발생했을 때 실행됩니다.
     */
    @OnError
    public void onError(Session session, @PathParam("name") String name, Throwable throwable) {
        System.out.println("onError> " + name + " 에러 발생: " + throwable.getMessage());
    }

    /**
     * 클라이언트로부터 메시지를 수신했을 때 실행됩니다.
     */
    @OnMessage
    public void onMessage(String message, @PathParam("name") String name) {
        System.out.println("onMessage> [" + name + "]: " + message);
        
        // 💡 팁: 실제 채팅 기능을 만들려면 여기서 session.getAsyncRemote().sendText(...) 등을 
        // 활용해 다른 사람들에게 메시지를 뿌려주어야 합니다.
    }
}