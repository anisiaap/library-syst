import firebase from 'firebase/app';
import 'firebase/firestore'; // For Firestore
import 'firebase/auth'; // For Firebase Auth if needed

// Firebase configuration object (replace with your own keys)
const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_AUTH_DOMAIN",
  projectId: "YOUR_PROJECT_ID",
  storageBucket: "YOUR_STORAGE_BUCKET",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID",
};

// Initialize Firebase
const firebaseApp = !firebase.apps.length ? firebase.initializeApp(firebaseConfig) : firebase.app();

// Initialize Firestore
const db = firebaseApp.firestore();

// Initialize Auth (if needed)
const auth = firebaseApp.auth();

export { db, auth };