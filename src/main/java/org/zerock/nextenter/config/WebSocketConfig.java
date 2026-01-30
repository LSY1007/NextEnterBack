package org.zerock.nextenter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic"); // 클라이언트가 구독할 prefix
        config.setApplicationDestinationPrefixes("/app"); // 클라이언트가 메시지를 보낼 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*") // CORS 설정
                .addInterceptors(new org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                            org.springframework.http.server.ServerHttpResponse response,
                            org.springframework.web.socket.WebSocketHandler wsHandler,
                            java.util.Map<String, Object> attributes) throws Exception {
                        System.out.println("============== WebSocket Handshake Attempt ==============");
                        System.out.println("URI: " + request.getURI());
                        System.out.println("Headers: " + request.getHeaders());
                        return super.beforeHandshake(request, response, wsHandler, attributes);
                    }
                })
                .withSockJS(); // SockJS 폴백 옵션 활성화
    }
}
