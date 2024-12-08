package com.example.bureaucratic_system_backend.controller;

import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.service.EnrollmentDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enrollment")
public class EnrollmentController {

    @Autowired
    private EnrollmentDepartmentService enrollmentService;

    @PostMapping("/enroll")
    public String enrollCitizen(@RequestBody Citizen citizen) {
        boolean enrolled = enrollmentService.enrollCitizen(citizen);
        return enrolled ? "Citizen enrolled successfully" : "Citizen enrollment failed or already enrolled";
    }
}
