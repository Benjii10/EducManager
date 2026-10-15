package com.educmanager.security;

import com.educmanager.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration Spring Security de l'API EducManager.
 *
 * <p>[Benjamin – #01, 2026-10-15] Réécriture complète (voir docs/JOURNAL_DEV.md) :</p>
 * <ul>
 *   <li>API sans état (stateless) : l'identité vient uniquement du token JWT ;</li>
 *   <li>le {@link JwtFilter} est branché DANS la chaîne Spring Security
 *       (avant, il était seulement déclaré en @Component et s'exécutait après la sécurité) ;</li>
 *   <li>les routes sont protégées (avant : {@code anyRequest().permitAll()}) ;</li>
 *   <li>{@code @EnableMethodSecurity} active les {@code @PreAuthorize} des contrôleurs ;</li>
 *   <li>les mots de passe sont hachés avec BCrypt ({@link #passwordEncoder()}).</li>
 * </ul>
 *
 * <p>Matrice des droits (rôles de {@link com.educmanager.enums.Role}) :</p>
 * <pre>
 *  /api/auth/login, Swagger, console H2  → public
 *  GET  filières / cursus / cours        → tout utilisateur connecté
 *  POST/PUT/DELETE filières/cursus/cours → REFERENTE ou ADMIN
 *  /api/student, /api/enrollment         → connecté + règles @PreAuthorize du contrôleur
 *  /api/users, /api/audit                → ADMIN
 *  tout le reste                         → connecté
 * </pre>
 *
 * <p>[Benjamin – #03, 2026-10-15] Ajout des promotions dans le référentiel, de /api/users et
 * /api/audit (ADMIN), d'un {@link UserDetailsService} relisant le compte en base, et des origines
 * CORS configurables ({@code app.cors.allowed-origins}).</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_URLS = {
            "/api/auth/login",
            "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
            "/h2-console/**",
            "/api/health",
            "/error"
    };

    private static final String[] REFERENTIEL_URLS = {
            "/api/filieres/**", "/api/cursus/**", "/api/courses/**", "/api/cursus-courses/**",
            "/api/promotions/**"
    };

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}")
    private List<String> allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtils jwtUtils,
                                            UserDetailsService userDetailsService) throws Exception {

        http
            .cors(Customizer.withDefaults())
            // CSRF inutile : pas de cookie de session, le token est envoyé dans l'en-tête Authorization
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // La console H2 s'affiche dans une iframe : on l'autorise uniquement depuis la même origine
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(PUBLIC_URLS).permitAll()
                    // Vérification d'accès à un cours : simple lecture, ouverte à tout utilisateur connecté
                    .requestMatchers(HttpMethod.POST, "/api/cursus-courses/can-access").authenticated()
                    .requestMatchers("/api/users/**", "/api/audit/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, REFERENTIEL_URLS).authenticated()
                    .requestMatchers(REFERENTIEL_URLS).hasAnyRole("REFERENTE", "ADMIN")
                    .anyRequest().authenticated())
            // Pas de token (ou token invalide) → 401 au lieu d'une page de login
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(new JwtFilter(jwtUtils, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Charge un compte depuis la base (utilisé par JwtFilter). Déclarer ce bean évite aussi
     * que Spring Boot crée un utilisateur par défaut avec un mot de passe affiché dans les logs.
     */
    @Bean
    UserDetailsService userDetailsService(UserAccountRepository repository) {
        return username -> repository.findByUsername(username)
                .map(u -> User.withUsername(u.getUsername())
                        .password(u.getPasswordHash())
                        .roles(u.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    /** Hachage des mots de passe (BCrypt, salage automatique). */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
