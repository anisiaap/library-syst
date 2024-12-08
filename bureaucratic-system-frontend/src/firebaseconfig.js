import { initializeApp } from "firebase/app";
import { getAuth, setPersistence, browserLocalPersistence } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "AIzaSyDjjNG1ipqcgJIpY_sroO50AaEsPUMtlTQ",
  authDomain: "librarysyst-66e8b.firebaseapp.com",
  databaseURL: "https://librarysyst-66e8b-default-rtdb.firebaseio.com",
  projectId: "librarysyst-66e8b",
  storageBucket: "librarysyst-66e8b.appspot.com",
  messagingSenderId: "743103631141",
  appId: "1:743103631141:web:3fc0f63c35dabd32de94cc",
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);
setPersistence(auth, browserLocalPersistence)
    .then(() => {
      // Now the user will stay logged in even after refreshing the page
    })
    .catch((error) => {
      console.error('Error setting persistence:', error);
    });
