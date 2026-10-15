package com.educmanager.dto;

import java.util.List;

/** Page de résultats (format stable, indépendant de Spring Data). [Benjamin – #03, 2026-10-15] Création. @author Benjamin */
public record PageResponse<T>(List<T> content, long totalElements, int page, int size) {
}
