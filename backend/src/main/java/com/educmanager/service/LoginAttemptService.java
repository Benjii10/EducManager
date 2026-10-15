package com.educmanager.service;

import com.educmanager.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protection contre la force brute sur le login (OWASP A07) :
 * après {@code maxAttempts} échecs pour un même identifiant, le compte est bloqué
 * pendant {@code blockDuration} (réponse HTTP 429).
 *
 * <p>Stockage en mémoire : suffisant pour une instance unique ; avec plusieurs instances,
 * il faudrait un stockage partagé (Redis...).</p>
 *
 * <p>[Benjamin – #03, 2026-10-15] Création.</p>
 *
 * @author Benjamin
 */
@Service
public class LoginAttemptService {

    private record Attempts(int count, Instant firstFailure) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration blockDuration;
    private final Clock clock;

    @Autowired
    public LoginAttemptService(@Value("${app.security.login.max-attempts:5}") int maxAttempts,
                               @Value("${app.security.login.block-minutes:15}") long blockMinutes,
                               Clock clock) {
        this.maxAttempts = maxAttempts;
        this.blockDuration = Duration.ofMinutes(blockMinutes);
        this.clock = clock;
    }

    /** Lève une exception 429 si l'identifiant est bloqué. */
    public void checkNotBlocked(String username) {
        Attempts a = attempts.get(key(username));
        if (a == null) {
            return;
        }
        if (isExpired(a)) {
            attempts.remove(key(username));
            return;
        }
        if (a.count() >= maxAttempts) {
            throw new TooManyRequestsException("Trop de tentatives échouées. Réessayez dans "
                    + blockDuration.toMinutes() + " minutes.");
        }
    }

    public void loginFailed(String username) {
        attempts.compute(key(username), (k, a) ->
                (a == null || isExpired(a)) ? new Attempts(1, clock.instant()) : new Attempts(a.count() + 1, a.firstFailure()));
    }

    public void loginSucceeded(String username) {
        attempts.remove(key(username));
    }

    private boolean isExpired(Attempts a) {
        return a.firstFailure().plus(blockDuration).isBefore(clock.instant());
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }
}
