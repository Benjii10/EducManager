package com.educmanager.audit;

import com.educmanager.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Journal d'audit (MongoDB simulé par des mocks). [Benjamin – #06, 2026-10-15] Création. @author Benjamin */
class AuditServiceTest {

    private final AuditLogRepository repository = mock(AuditLogRepository.class);
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-15T10:00:00Z"), ZoneId.of("UTC"));

    @Test
    void horodateEtEnregistreLEvenement() {
        AuditService service = new AuditService(repository, mongoTemplate, clock, true);
        AuditLog log = AuditLog.builder().action("CREATE").build();

        service.record(log);

        assertThat(log.getTimestamp()).isEqualTo(Instant.parse("2026-10-15T10:00:00Z"));
        verify(repository).save(log);
    }

    @Test
    void uneErreurMongoNeRemontePasALAppelant() {
        when(repository.save(any())).thenThrow(new RuntimeException("Mongo down"));
        AuditService service = new AuditService(repository, mongoTemplate, clock, true);

        assertThatCode(() -> service.record(AuditLog.builder().action("CREATE").build())).doesNotThrowAnyException();
    }

    @Test
    void nEnregistreRienSiDesactive() {
        new AuditService(repository, mongoTemplate, clock, false).record(AuditLog.builder().build());
        verify(repository, never()).save(any());
    }

    @Test
    void rechercheBorneLaTailleDePage() {
        when(mongoTemplate.count(any(Query.class), eq(AuditLog.class))).thenReturn(3L);
        when(mongoTemplate.find(any(Query.class), eq(AuditLog.class))).thenReturn(List.of(new AuditLog()));
        AuditService service = new AuditService(repository, mongoTemplate, clock, true);

        PageResponse<AuditLog> page = service.search("admin", "login_failure", -1, 1000);

        assertThat(page.size()).isEqualTo(100);
        assertThat(page.page()).isZero();
        assertThat(page.totalElements()).isEqualTo(3L);
    }

    @Test
    void traduitLaMethodeHttpEnAction() {
        assertThat(AuditInterceptor.actionOf("POST")).isEqualTo("CREATE");
        assertThat(AuditInterceptor.actionOf("PUT")).isEqualTo("UPDATE");
        assertThat(AuditInterceptor.actionOf("DELETE")).isEqualTo("DELETE");
    }
}
