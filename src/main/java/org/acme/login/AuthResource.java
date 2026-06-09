package org.acme.login;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import io.vertx.ext.web.RoutingContext;

/**
 * 로그인, 회원가입, 세션 관리, 마이페이지 프로필 처리를 담당하는 인증 리소스 컨트롤러
 */
@Path("/") // 기본 최상위 경로 설정
public class AuthResource {

    @Inject
    RoutingContext context; // 세션 및 HTTP 컨텍스트 관리를 위한 Vert.x 라우팅 컨텍스트 주입

    // ==========================================
    // 1. 메인 및 기본 페이지 라우팅
    // ==========================================

    /**
     * GET / : 세션 유무를 판단하여 로그인 전/후 메인 페이지를 동적으로 분기 반환
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response mainPage() {
        String loginUser = context.session().get("loginUser");

        System.out.println("=== [GET /] 세션 ID : " + context.session().id());
        System.out.println("=== [GET /] loginUser : " + loginUser);

        // 세션 존재 여부에 따라 리소스 경로 다르게 지정
        String htmlPath = (loginUser != null)
            ? "META-INF/resources/login/main_after_login.html"
            : "META-INF/resources/main_index.html";

        InputStream html = getClass().getClassLoader().getResourceAsStream(htmlPath);
        return Response.ok(html).build();
    }

    /**
     * GET /login : 로그인 페이지 반환 (이미 로그인된 유저는 메인으로 리다이렉트)
     */
    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public Response loginPage() {
        String loginUser = context.session().get("loginUser");

        if (loginUser != null) {
            return Response.seeOther(URI.create("/after_login")).build();
        }

        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/login.html");
        return Response.ok(html).build();
    }

    /**
     * GET /after_login : 로그인 성공 후 진입하는 안전 구역 (인증 체크 필수)
     */
    @GET
    @Path("/after_login")
    @Produces(MediaType.TEXT_HTML)
    public Response afterLogin() {
        String loginUser = context.session().get("loginUser");

        System.out.println("=== [after_login] 세션 ID : " + context.session().id());
        System.out.println("=== [after_login] loginUser : " + loginUser);
        
        if (loginUser == null) {
            // 무단 접근 차단 -> 로그인 페이지로 강제 이동
            return Response.seeOther(URI.create("/login")).build();
        }

        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/main_after_login.html");
        return Response.ok(html).build();
    }

    // ==========================================
    // 2. 인증 처리 (로그인 / 로그아웃 / 회원가입)
    // ==========================================

    /**
     * POST /login_check : 프론트엔드 폼에서 전송된 아이디/비밀번호 검증
     */
    @POST
    @Path("/login_check")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) 
    public Response loginCheck(
        @FormParam("username") String username,
        @FormParam("password") String password) {

        User user = User.findByUsername(username); 
        
        // 유저가 없거나 패스워드(SHA-256 해시값)가 일치하지 않는 경우
        if (user == null || !user.password.equals(password)) { 
            return Response.seeOther(URI.create("/login?error=1")).build();
        }

        // 인증 성공 시 세션에 유저 아이디 바인딩
        context.session().put("loginUser", username);

        return Response.seeOther(URI.create("/after_login")).build();
    }

    /**
     * GET /logout : 현재 세션을 완전히 파기하고 로그아웃 처리
     */
    @GET
    @Path("/logout")
    public Response logout(@QueryParam("next") String next) {
        System.out.println("=== 로그아웃 전 세션 ID : " + context.session().id());
        context.session().destroy(); // 세션 무효화

        String redirect = (next != null && next.equals("login")) ? "/login" : "/";
        return Response.seeOther(URI.create(redirect)).build();
    }

    /**
     * GET /register : 회원가입 페이지 반환
     */
    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public Response registerPage() {
        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/register.html");
        return Response.ok(html).build();
    }

    /**
     * POST /register_check : 회원가입 정보 검증 및 DB 데이터 삽입
     */
    @POST
    @Path("/register_check")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response registerCheck(
        @FormParam("username") String username,
        @FormParam("password") String password, 
        @FormParam("email") String email,
        @FormParam("phone") String phone) {

        // ① 아이디 중복 체크
        if (User.findByUsername(username) != null) {
            return Response.seeOther(URI.create("/register?error=duplicate_username")).build();
        }

        // ② 이메일 중복 체크
        if (User.findByEmail(email) != null) {
            return Response.seeOther(URI.create("/register?error=duplicate_email")).build();
        }

        // ③ 신규 유저 데이터베이스 빌드 및 반영
        User newUser = new User();
        newUser.username = username;
        newUser.password = password; // 이미 클라이언트에서 해싱 완료된 값 수신
        newUser.email = email;
        newUser.phone = phone;
        newUser.persist();

        // ④ 가입 완료 라우팅
        return Response.seeOther(URI.create("/register_success")).build();
    }

    /**
     * GET /register_success : 회원가입 완료 알림 페이지 반환
     */
    @GET
    @Path("/register_success")
    @Produces(MediaType.TEXT_HTML)
    public Response registerSuccess() {
        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/register_success.html");
        return Response.ok(html).build();
    }

    // ==========================================
    // 3. 마이페이지 및 회원 정보 수정 (프로필 처리)
    // ==========================================

    /**
     * GET /profile : 프로필 메인 화면 (세션 데이터 바인딩 후 포워딩)
     */
    @GET
    @Path("/profile")
    @Produces(MediaType.TEXT_HTML)
    public Response profilePage() {
        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        User user = User.findByUsername(loginUser);

        // 템플릿 엔진 비사용 구조 보완용 세션 바인딩
        context.session().put("userEmail", user.email);
        context.session().put("userPhone", user.phone);
        context.session().put("profileImage", user.profileImage != null ? user.profileImage : "default.png");

        InputStream html = getClass().getClassLoader().getResourceAsStream("META-INF/resources/login/profile.html");
        return Response.ok(html).build();
    }

    /**
     * GET /profile/info : 비동기(AJAX) 요청 전용 유저 데이터 반환 엔드포인트
     */
    @GET
    @Path("/profile/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response profileInfo() {
        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.status(401).build();
        }

        User user = User.findByUsername(loginUser);
        return Response.ok(
            Map.of(
                "username", user.username,
                "email", user.email != null ? user.email : "",
                "phone", user.phone != null ? user.phone : "",
                "profileImage", user.profileImage != null ? user.profileImage : "default.png"
            )
        ).build();
    }

    /**
     * POST /profile/upload : 이미지 확장자 및 용량 검증 후 멀티파트 파일 업로드 처리
     */
    @POST
    @Path("/profile/upload")
    @Transactional
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response profileUpload(@RestForm("profileImage") FileUpload file) {
        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        try {
            // ① 확장자 유효성 검사
            String original = file.fileName();
            String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
            if (!ext.matches("jpg|jpeg|png|gif|webp")) {
                return Response.seeOther(URI.create("/profile?error=invalid_type")).build();
            }

            // ② 파일 허용 한도 체크 (5MB 제한)
            if (file.size() > 5 * 1024 * 1024) {
                return Response.seeOther(URI.create("/profile?error=too_large")).build();
            }

            // ③ 파일 유니크 네이밍 가공 및 운영체제 범용 경로 수립
            String newFileName = UUID.randomUUID() + "." + ext;
            
            // ⚠️ 주의: 실제 운영 배포 단계에서는 외부 절대 경로(예: System.getProperty("user.home") + "/uploads")를 사용하는 구조로 변경해야 병목/에러가 안 납니다.
            java.nio.file.Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads/profile");
            java.nio.file.Files.createDirectories(uploadDir);
            java.nio.file.Files.copy(file.uploadedFile(), uploadDir.resolve(newFileName), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // ④ 업로드 성공 시 매핑 테이블 내 경로 엔트리 수정 업데이트
            User user = User.findByUsername(loginUser);
            user.profileImage = newFileName;

            return Response.seeOther(URI.create("/profile")).build();
        } catch (Exception e) {
            return Response.seeOther(URI.create("/profile?error=upload_fail")).build();
        }
    }

    /**
     * POST /profile/update : 이메일 및 전화번호 연락처 변경 동기화 로직
     */
    @POST
    @Path("/profile/update")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response profileUpdate(
        @FormParam("email") String email,
        @FormParam("phone") String phone) {

        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        // 이메일 중복 검사 검증 체인 (단, 자기 자신의 기존 이메일은 패스)
        User found = User.findByEmail(email);
        if (found != null && !found.username.equals(loginUser)) {
            return Response.seeOther(URI.create("/profile?error=duplicate_email")).build();
        }

        User user = User.findByUsername(loginUser);
        user.email = email;
        user.phone = phone;

        return Response.seeOther(URI.create("/profile?success=updated")).build();
    }

    /**
     * POST /profile/password : 현재 암호 해시 검증 후 새로운 신규 암호로 전이 업데이트
     */
    @POST
    @Path("/profile/password")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response profilePassword(
        @FormParam("currentPassword") String currentPassword,
        @FormParam("newPassword") String newPassword) {

        String loginUser = context.session().get("loginUser");
        if (loginUser == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        User user = User.findByUsername(loginUser);
        // 클라이언트단에서 해싱되어 도달한 기존 패스워드 시그니처와 대조
        if (!user.password.equals(currentPassword)) {
            return Response.seeOther(URI.create("/profile?error=wrong_password")).build();
        }

        // 일치 확인 시 새 해시 코드로 데이터 덮어쓰기 완료
        user.password = newPassword;
        return Response.seeOther(URI.create("/profile?success=password_changed")).build();
    }

    // ==========================================
    // 4. [요구사항 반영] 다크/라이트 테마 세션 영속화 연동 API
    // ==========================================

    /**
     * POST /theme : 프론트엔드 모드 전환 시 세션에 현재 테마 상태 기록 저장
     */
    @POST
    @Path("/theme")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response saveThemeState(@FormParam("theme") String theme) {
        if (theme != null && (theme.equals("dark") || theme.equals("light"))) {
            context.session().put("themeMode", theme);
        }
        return Response.ok().build();
    }

    /**
     * GET /theme : 페이지 로드 시 세션에 저장된 테마 값을 프론트에 JSON 포맷으로 전달
     */
    @GET
    @Path("/theme")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThemeState() {
        String themeMode = context.session().get("themeMode");
        // 저장된 기록이 없을 시에는 디폴트 값 'dark' 반환 보장
        return Response.ok(Map.of("theme", themeMode != null ? themeMode : "dark")).build();
    }
}