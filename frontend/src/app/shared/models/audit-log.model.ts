// [Benjamin – #07, 2026-10-15] Création. Miroir du document MongoDB AuditLog et de PageResponse.
export interface AuditLog {
  id: string;
  timestamp: string;
  username: string;
  action: string;
  resource: string;
  resourceId: string | null;
  httpMethod: string;
  path: string;
  status: number;
  ipAddress: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  page: number;
  size: number;
}
