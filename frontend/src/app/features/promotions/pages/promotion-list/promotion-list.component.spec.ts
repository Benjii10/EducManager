import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { CursusService } from '../../../cursus/services/cursus.service';
import { PromotionService } from '../../services/promotion.service';
import { PromotionListComponent } from './promotion-list.component';

// [Benjamin – #09, 2026-10-15] Création.
describe('PromotionListComponent', () => {
  let promotions: jasmine.SpyObj<PromotionService>;

  beforeEach(() => {
    promotions = jasmine.createSpyObj<PromotionService>('PromotionService', ['getAll', 'create', 'update', 'delete']);
    promotions.getAll.and.returnValue(of([]));
    promotions.create.and.returnValue(of({ id: 1, name: 'P', startDate: '2027-01-04', endDate: '2027-06-30', cursusId: 1 }));
    const cursus = jasmine.createSpyObj<CursusService>('CursusService', ['getAll']);
    cursus.getAll.and.returnValue(of([{ id: 1, name: 'CDA', filiereId: 1 }]));
    TestBed.configureTestingModule({
      imports: [PromotionListComponent],
      providers: [
        { provide: PromotionService, useValue: promotions },
        { provide: CursusService, useValue: cursus },
      ],
    });
  });

  it('bloque des dates inversées sans appeler l\'API', () => {
    const c = TestBed.createComponent(PromotionListComponent).componentInstance;
    c.form = { name: 'P', startDate: '2027-06-30', endDate: '2027-01-04', cursusId: 1 };

    expect(c.datesInvalides).toBeTrue();
    c.save();
    expect(promotions.create).not.toHaveBeenCalled();
  });

  it('crée une promotion valide puis recharge la liste', () => {
    const c = TestBed.createComponent(PromotionListComponent).componentInstance;
    c.form = { name: 'P', startDate: '2027-01-04', endDate: '2027-06-30', cursusId: 1 };

    c.save();

    expect(promotions.create).toHaveBeenCalled();
    expect(c.success).toBe('Promotion créée.');
  });
});
