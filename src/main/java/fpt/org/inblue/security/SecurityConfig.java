package fpt.org.inblue.security;

import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private static final String ADMIN = "ADMIN";
    private static final String STAFF = "STAFF";
    private static final String USER = "USER";
    private static final String MENTOR = "MENTOR";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${frontend.url}")
    private String frontendUrl;

    private final Oauth2Handler oauth2Handler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailService customUserDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();

                    corsConfiguration.setAllowedOriginPatterns(Arrays.asList("*"));

                    corsConfiguration.setAllowedMethods(
                            Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(httpBasic -> httpBasic.disable())
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        .requestMatchers(
                                "/api/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/test/**",
                                "/ws-chat/**",
                                "/api/payments/webhook/**",
                                "/api/payments/webhook",
                                "/api/sessions/webhooks/dailyco")
                        .permitAll()
//                        .requestMatchers(HttpMethod.POST, "/api/users")
//                        .permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/job-descriptions/toggle/**", "/api/companies/toggle/**")
//                        .hasRole(ADMIN)
//                        .requestMatchers(
//                                "/api/users/schedule",
//                                "/api/users/upload-cv",
//                                "/api/users/change-password",
//                                "/api/sessions/make-payment")
//                        .hasRole(USER)
//                        .requestMatchers(
//                                "/api/sessions/update-status",
//                                "/api/sessions/check-webhook",
//                                "/api/sessions/reactivate-webhook")
//                        .hasRole(ADMIN)
//                        .requestMatchers("/api/mentors/schedule", "/api/mentors/*/change-password")
//                        .hasRole(MENTOR)
//                        .requestMatchers(HttpMethod.GET, "/api/job-descriptions", "/api/job-descriptions/**")
//                        .permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/companies", "/api/companies/**")
//                        .permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/applications/*/competency-chart")
//                        .permitAll()
//                        .requestMatchers(
//                                "/api/admin/analytics/**",
//                                "/api/dashboard/**",
//                                "/api/users",
//                                "/api/users/*",
//                                "/api/users/find-by-id/**",
//                                "/api/mentors/toggle/**",
//                                "/api/question-banks/**",
//                                "/api/question-categories/**",
//                                "/api/templates/**")
//                        .hasRole(ADMIN)
//                        .requestMatchers(
//                                HttpMethod.POST,
//                                "/api/mentors",
//                                "/api/job-descriptions",
//                                "/api/companies",
//                                "/api/kiosks",
//                                "/api/kiosks/schedule",
//                                "/api/rounds/generate-whiteboard-question")
//                        .hasRole(ADMIN)
//                        .requestMatchers(
//                                HttpMethod.PUT,
//                                "/api/mentors/**",
//                                "/api/job-descriptions/**",
//                                "/api/companies/**",
//                                "/api/kiosks/**",
//                                "/api/rounds/**",
//                                "/api/application-details/*/assign-mentor",
//                                "/api/application-details/*/assign-mentors")
//                        .hasRole(ADMIN)
//                        .requestMatchers(HttpMethod.DELETE, "/api/job-descriptions/**", "/api/companies/**")
//                        .hasRole(ADMIN)
//                        .requestMatchers("/api/admin/**", "/api/email-submissions/**")
//                        .hasAnyRole(STAFF, ADMIN)
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                "/api/application-details/reviewer",
//                                "/api/application-details/*",
//                                "/api/application-details/application/**",
//                                "/api/users/*/schedule",
//                                "/api/mentors/*/schedule",
//                                "/api/posts",
//                                "/api/posts/published")
//                        .hasAnyRole(STAFF, ADMIN)
//                        .requestMatchers(
//                                HttpMethod.POST,
//                                "/api/application-details/hr-score",
//                                "/api/application-details/code-review/evaluate")
//                        .hasAnyRole(STAFF, ADMIN)
//                        .requestMatchers("/api/posts/change-status/**")
//                        .hasAnyRole(STAFF, ADMIN)
//                        .requestMatchers(HttpMethod.GET, "/api/posts/feed", "/api/posts/*")
//                        .hasAnyRole(USER, MENTOR, STAFF)
//                        .requestMatchers(HttpMethod.POST, "/api/posts", "/api/posts/likes", "/api/posts/comments")
//                        .hasAnyRole(USER, MENTOR, STAFF)
//                        .requestMatchers(HttpMethod.PUT, "/api/posts/comments/**")
//                        .hasAnyRole(USER, MENTOR, STAFF)
//                        .requestMatchers(HttpMethod.DELETE, "/api/posts/likes/**", "/api/posts/comments/**")
//                        .hasAnyRole(USER, MENTOR, STAFF)
//                        .requestMatchers("/api/messages/**", "/api/mentor-feedbacks/**")
//                        .hasAnyRole(USER, MENTOR)
//                        .requestMatchers(
//                                HttpMethod.GET,
//                                "/api/mentors",
//                                "/api/mentors/*",
//                                "/api/mentor-reviews/**",
//                                "/api/sessions",
//                                "/api/sessions/*",
//                                "/api/sessions/*/by-user")
//                        .hasAnyRole(USER, MENTOR)
//                        .requestMatchers(HttpMethod.PUT, "/api/sessions")
//                        .hasAnyRole(USER, MENTOR)
//                        .requestMatchers(HttpMethod.POST, "/api/sessions/join-session", "/api/mentor-reviews")
//                        .hasAnyRole(USER, MENTOR)
//                        .requestMatchers(HttpMethod.PUT, "/api/mentor-reviews")
//                        .hasAnyRole(USER, MENTOR)
//                        .requestMatchers(
//                                "/api/candidate-profiles/**",
//                                "/api/applications/**",
//                                "/api/application-details/submit",
//                                "/api/application-details/*/select-mentor",
//                                "/api/code-review-problems/**",
//                                "/api/interview-sessions/**",
//                                "/api/v1/interview/**",
//                                "/api/interview-analysis/**",
//                                "/api/v1/proctoring/**",
//                                "/api/jd-purchases/**",
//                                "/api/payments/**",
//                                "/api/kiosk-bookings/**",
//                                "/api/kiosk/enter/**",
//                                "/api/sessions/create-session",
//                                "/api/sessions/create-for-round",
//                                "/api/sessions/make-payment")
//                        .hasRole(USER)
                        .anyRequest()
                        .authenticated())
                .oauth2Login(
                        oauth2 -> oauth2.userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                                .successHandler(oauth2Handler))
                .securityContext(context -> context.requireExplicitSave(false));
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailService);

        authProvider.setHideUserNotFoundExceptions(false);

        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
