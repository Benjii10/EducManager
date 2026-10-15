package com.educmanager.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tests de génération / validation des tokens JWT. [Benjamin – #06, 2026-10-15] Création. @author Benjamin */
class JwtUtilsTest {

    private static final String SECRET = "un-secret-de-test-suffisamment-long-0123456789";

    @Test
    void genereUnTokenLisible() {
        JwtUtils jwt = new JwtUtils(SECRET, 60_000);
        String token = jwt.generateToken("alice@test.fr", "ADMIN");

        assertThat(jwt.validateToken(token)).isTrue();
        assertThat(jwt.getUsernameFromToken(token)).isEqualTo("alice@test.fr");
        assertThat(jwt.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    void refuseUnTokenSigneAvecUnAutreSecret() {
        String token = new JwtUtils(SECRET, 60_000).generateToken("alice@test.fr", "ADMIN");
        JwtUtils other = new JwtUtils("un-autre-secret-tout-aussi-long-9876543210", 60_000);

        assertThat(other.validateToken(token)).isFalse();
    }

    @Test
    void refuseUnTokenExpire() {
        JwtUtils jwt = new JwtUtils(SECRET, -1_000);
        String token = jwt.generateToken("alice@test.fr", "ADMIN");

        assertThat(jwt.validateToken(token)).isFalse();
    }

    @Test
    void refuseUnTokenModifie() {
        JwtUtils jwt = new JwtUtils(SECRET, 60_000);
        String token = jwt.generateToken("alice@test.fr", "STUDENT");
        String[] parts = token.split("\\.");
        String forged = parts[0] + "." + parts[1].substring(0, parts[1].length() - 2) + "AA." + parts[2];

        assertThat(jwt.validateToken(forged)).isFalse();
        assertThat(jwt.validateToken("pas-un-token")).isFalse();
    }

    @Test
    void refuseUnSecretTropCourt() {
        assertThatThrownBy(() -> new JwtUtils("court", 60_000)).isInstanceOf(IllegalStateException.class);
    }
}
