//package com.example.bureaucratic_system_backend.controller;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.UserRecord;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/roles")
//public class RoleController {
//
//    @PostMapping("/assign-default-role")
//    public String assignDefaultRole(@RequestParam String userId) {
//        try {
//            // Assign 'client' role using custom claims
//            FirebaseAuth.getInstance().setCustomUserClaims(userId, Map.of("role", "client"));
//            return "Default role 'client' assigned to user ID: " + userId;
//        } catch (Exception e) {
//            return "Error assigning role: " + e.getMessage();
//        }
//    }
//    @PostMapping("/assign-admin-role")
//    public String assignAdminRole(@RequestParam String userId) {
//        try {
//            // Assign 'client' role using custom claims
//            FirebaseAuth.getInstance().setCustomUserClaims(userId, Map.of("role", "admin"));
//            return "Default role 'admin' assigned to user ID: " + userId;
//        } catch (Exception e) {
//            return "Error assigning role: " + e.getMessage();
//        }
//    }
//    @PostMapping("/assign-client-role")
//    public String assignClientRole(@RequestParam String userId) {
//        try {
//            // Assign 'client' role using custom claims
//            FirebaseAuth.getInstance().setCustomUserClaims(userId, Map.of("role", "client"));
//            return "Default role 'client' assigned to user ID: " + userId;
//        } catch (Exception e) {
//            return "Error assigning role: " + e.getMessage();
//        }
//    }
//}