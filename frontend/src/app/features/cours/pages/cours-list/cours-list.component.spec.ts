import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthService } from '../../../../core/auth/auth.service';
import { CourseService } from '../../services/course.service';
import { CoursListComponent } from './cours-list.component';

// [Benjamin – #09, 2026-10-15] Création.
describe('CoursListComponent', () => {
  let fixture: ComponentFixture<CoursListComponent>;
  let auth: jasmine.SpyObj<AuthService>;
  let courses: jasmine.SpyObj<CourseService>;

  function setup(canEdit: boolean): void {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['hasRole']);
    auth.hasRole.and.returnValue(canEdit);
    courses = jasmine.createSpyObj<CourseService>('CourseService', ['getAll', 'create', 'update', 'delete']);
    courses.getAll.and.returnValue(of([
      { id: 1, code: 'JAVA-01', titre: 'Java et POO', dureeJours: 10 },
      { id: 2, code: 'NG-01', titre: 'Angular', dureeJours: 8 },
    ]));
    TestBed.configureTestingModule({
      imports: [CoursListComponent],
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: CourseService, useValue: courses },
      ],
    });
    fixture = TestBed.createComponent(CoursListComponent);
    fixture.detectChanges();
  }

  it('affiche les cours de l\'API', () => {
    setup(true);
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows.length).toBe(2);
    expect(rows[0].textContent).toContain('Java et POO');
  });

  it('cache le formulaire et les boutons pour un formateur (lecture seule)', () => {
    setup(false);
    expect(fixture.nativeElement.querySelector('form')).toBeNull();
    expect(fixture.nativeElement.textContent).not.toContain('Supprimer');
  });

  it('filtre par titre ou code', () => {
    setup(true);
    fixture.componentInstance.searchQuery = 'ng-';
    expect(fixture.componentInstance.filteredCourses.map((c) => c.code)).toEqual(['NG-01']);
  });

  it('refuse un formulaire sans code ni titre', () => {
    setup(true);
    fixture.componentInstance.form = { code: ' ', titre: '', dureeJours: 1 };
    fixture.componentInstance.save();
    expect(courses.create).not.toHaveBeenCalled();
    expect(fixture.componentInstance.error).toContain('obligatoires');
  });
});
