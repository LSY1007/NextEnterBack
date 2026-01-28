package org.zerock.nextenter.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * HTML 이메일 전송
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("nextenter.service@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML 이메일 전송 성공: {}", to);
        } catch (MessagingException e) {
            log.error("HTML 이메일 전송 실패: {}", to, e);
            throw new RuntimeException("HTML 이메일 전송 실패", e);
        }
    }

    /**
     * 회원탈퇴 인증코드 이메일
     */
    public void sendWithdrawalVerificationEmail(String to, String userName, String verificationCode) {
        String subject = "NextEnter 회원탈퇴 인증코드";
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 30px;">
                    <h2 style="color: #ef4444;">회원탈퇴 인증코드</h2>
                    <p>안녕하세요, <strong>%s</strong>님!</p>
                    <p>회원탈퇴를 위한 인증코드입니다.</p>
                    <br>
                    <div style="background-color: #f3f4f6; padding: 20px; border-radius: 5px; text-align: center;">
                        <p style="margin: 0; color: #666; font-size: 14px;">인증코드</p>
                        <h1 style="margin: 10px 0; color: #ef4444; letter-spacing: 5px;">%s</h1>
                    </div>
                    <br>
                    <p style="color: #666; font-size: 14px;">
                        ⚠️ 이 인증코드는 10분간 유효합니다.<br>
                        ⚠️ 회원탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.<br>
                        ⚠️ 본인이 요청하지 않았다면 이 메일을 무시하세요.
                    </p>
                </div>
            </body>
            </html>
            """, userName, verificationCode);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 회원가입 환영 이메일
     */
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "NextEnter 가입을 환영합니다!";
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 30px;">
                    <h2 style="color: #2563eb;">NextEnter에 오신 것을 환영합니다!</h2>
                    <p>안녕하세요, <strong>%s</strong>님!</p>
                    <p>NextEnter 회원가입을 완료해주셔서 감사합니다.</p>
                    <p>이제 다양한 채용 정보와 AI 기반 이력서 매칭 서비스를 이용하실 수 있습니다.</p>
                    <br>
                    <a href="http://localhost:5173" 
                       style="background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                        지금 시작하기
                    </a>
                </div>
            </body>
            </html>
            """, userName);

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * 비밀번호 변경 인증코드 이메일
     */
    public void sendPasswordChangeVerificationEmail(String to, String userName, String verificationCode) {
        String subject = "NextEnter 비밀번호 변경 인증코드";
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 10px; padding: 30px;">
                    <h2 style="color: #2563eb;">비밀번호 변경 인증코드</h2>
                    <p>안녕하세요, <strong>%s</strong>님!</p>
                    <p>비밀번호 변경을 위한 인증코드입니다.</p>
                    <br>
                    <div style="background-color: #f3f4f6; padding: 20px; border-radius: 5px; text-align: center;">
                        <p style="margin: 0; color: #666; font-size: 14px;">인증코드</p>
                        <h1 style="margin: 10px 0; color: #2563eb; letter-spacing: 5px;">%s</h1>
                    </div>
                    <br>
                    <p style="color: #666; font-size: 14px;">
                        ⚠️ 이 인증코드는 10분간 유효합니다.<br>
                        ⚠️ 본인이 요청하지 않았다면 이 메일을 무시하세요.
                    </p>
                </div>
            </body>
            </html>
            """, userName, verificationCode);

        sendHtmlEmail(to, subject, htmlContent);
    }
}