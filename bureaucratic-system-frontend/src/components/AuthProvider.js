import React, { createContext, useState, useEffect, useContext } from 'react';
import { onAuthStateChanged, setPersistence, browserLocalPersistence } from 'firebase/auth';
import { getFirestore, doc, getDoc } from 'firebase/firestore';
import { auth } from '../firebaseconfig';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null); // Holds the authenticated user
    const [role, setRole] = useState(null); // Holds the role of the user
    const [loading, setLoading] = useState(true); // Tracks loading state

    useEffect(() => {
        // Set Firebase auth persistence to local
        const initializeAuth = async () => {
            try {
                await setPersistence(auth, browserLocalPersistence); // Ensures persistence across sessions
                const unsubscribe = onAuthStateChanged(auth, async (currentUser) => {
                    if (currentUser) {
                        setUser(currentUser);

                        // Fetch user role from Firestore
                        try {
                            const db = getFirestore();
                            const userDoc = await getDoc(doc(db, 'users', currentUser.uid));
                            if (userDoc.exists()) {
                                setRole(userDoc.data().role); // Set user role
                            } else {
                                console.warn('User document not found in Firestore.');
                                setRole(null);
                            }
                        } catch (error) {
                            console.error('Error fetching user role:', error);
                            setRole(null);
                        }
                    } else {
                        setUser(null);
                        setRole(null);
                    }
                    setLoading(false); // End loading
                });

                return unsubscribe;
            } catch (error) {
                console.error('Error initializing Firebase auth:', error);
            }
        };

        initializeAuth();
    }, []);

    if (loading) {
        return (
            <div className="flex items-center justify-center min-h-screen bg-gray-100">
                <p className="text-lg font-medium text-gray-600">Loading...</p>
            </div>
        );
    }

    return (
        <AuthContext.Provider value={{ user, role }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);