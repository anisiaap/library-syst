package com.example.bureaucratic_system_backend.service;

import com.example.bureaucratic_system_backend.model.Citizen;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CitizenService {

    public void addCitizen(Citizen citizen) throws Exception {
        Firestore db = FirestoreClient.getFirestore();
        Map<String, Object> citizenData = new HashMap<>();
        citizenData.put("id", citizen.getId());
        citizenData.put("name", citizen.getName());

        // Add citizen to Firestore 'citizens' collection
        db.collection("citizen").document(citizen.getId()).set(citizenData).get();
    }
}