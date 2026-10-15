package com.educmanager.controller;

import com.educmanager.entity.Course;
import com.educmanager.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API des cours. Lecture : tout utilisateur connecté ; écriture : REFERENTE / ADMIN (SecurityConfig).
 *
 * <p>[Benjamin – #03, 2026-10-15] Ajout de GET /{id}, PUT /{id}, DELETE /{id} et de la validation (@Valid).
 * GET et POST existants (Manar) inchangés dans leur contrat.</p>
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return service.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course getCourse(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public Course createCourse(@Valid @RequestBody Course course) {
        return service.createCourse(course);
    }

    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable Long id, @Valid @RequestBody Course course) {
        return service.updateCourse(id, course);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        service.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
