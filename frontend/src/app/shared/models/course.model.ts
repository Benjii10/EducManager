// [Benjamin – #07, 2026-10-15] Aligné sur l'entité Java Course (champs fictifs formateur/etudiants retirés).
export interface Course {
  id?: number;
  code: string;
  titre: string;
  description?: string | null;
  dureeJours?: number | null;
}
