package com.educmanager.service;

import com.educmanager.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests de la protection anti force brute. [Benjamin – #06, 2026-10-15] Création. @author Benjamin */
class LoginAttemptServiceTest {

    /** Horloge réglable pour simuler le passage du temps. */
    static class MutableClock extends Clock {
        Instant now = Instant.parse("2026-10-15T10:00:00Z");
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void bloqueApresLeNombreMaximumDEchecs() {
        LoginAttemptService service = new LoginAttemptService(3, 15, new MutableClock());

        service.loginFailed("Bob@Test.fr");
        service.loginFailed("bob@test.fr");
        assertThatCode(() -> service.checkNotBlocked("bob@test.fr")).doesNotThrowAnyException();

        service.loginFailed("bob@test.fr");
        assertThatThrownBy(() -> service.checkNotBlocked("BOB@test.fr"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void debloqueApresLaDureeDeBlocage() {
        MutableClock clock = new MutableClock();
        LoginAttemptService service = new LoginAttemptService(2, 15, clock);
        service.loginFailed("a@b.fr");
        service.loginFailed("a@b.fr");

        clock.now = clock.now.plus(Duration.ofMinutes(16));

        assertThatCode(() -> service.checkNotBlocked("a@b.fr")).doesNotThrowAnyException();
    }

    @Test
    void unSuccesRemetLeCompteurAZero() {
        LoginAttemptService service = new LoginAttemptService(2, 15, new MutableClock());
        service.loginFailed("a@b.fr");
        service.loginSucceeded("a@b.fr");
        service.loginFailed("a@b.fr");

        assertThatCode(() -> service.checkNotBlocked("a@b.fr")).doesNotThrowAnyException();
    }
}
