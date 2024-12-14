package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Citizen;
import com.example.bureaucratic_system_backend.model.Membership;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EnrollmentDepartmentService {

    private final FirebaseService firebaseService;

    @Autowired
    public EnrollmentDepartmentService(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    public boolean isCitizenEnrolled(Citizen citizen) {
        String existingMembership = FirebaseService.getMembershipIdById(citizen.getId());
        return existingMembership != null;
    }

    public boolean addCitizen(Citizen citizen) {
        try {
            System.out.println("Enrolling citizen: " + citizen.getName());
            Membership newMembership = new Membership("M" + System.currentTimeMillis(),
                    LocalDate.now().toString(), citizen.getId());
            firebaseService.addMembership(newMembership);
            System.out.println("Citizen " + citizen.getName() + " enrolled with membership ID: " + newMembership.getMembershipNumber());
            return true;
        } catch (Exception e) {
            System.err.println("Enrollment failed for citizen " + citizen.getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean enrollCitizen(Citizen citizen) {
        if (isCitizenEnrolled(citizen)) {
            System.out.println("Citizen " + citizen.getName() + " is already enrolled.");
            return false;
        }
        return addCitizen(citizen);
    }
}
