// [Benjamin – #07, 2026-10-15] Création. Miroir des DTO Java PromotionDto / PromotionRequest.
export interface Promotion {
  id?: number;
  name: string;
  startDate: string; // AAAA-MM-JJ
  endDate: string;
  cursusId: number;
  cursusName?: string;
  enrolledCount?: number;
}
