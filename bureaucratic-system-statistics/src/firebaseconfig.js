// Import necessary Firebase services
import { initializeApp } from "firebase/app";
import { getAuth, setPersistence, browserLocalPersistence } from "firebase/auth";
import { getFirestore } from "firebase/firestore";

// Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyDjjNG1ipqcgJIpY_sroO50AaEsPUMtlTQ",
    authDomain: "librarysyst-66e8b.firebaseapp.com",
    databaseURL: "https://librarysyst-66e8b-default-rtdb.firebaseio.com",
    projectId: "librarysyst-66e8b",
    storageBucket: "librarysyst-66e8b.appspot.com",
    messagingSenderId: "743103631141",
    appId: "1:743103631141:web:3fc0f63c35dabd32de94cc",
};
// Initialize Firebase App
const app = initializeApp(firebaseConfig);

// Initialize Firebase Auth
export const auth = getAuth(app);

// Initialize Firestore
export const db = getFirestore(app);

// Set Auth Persistence