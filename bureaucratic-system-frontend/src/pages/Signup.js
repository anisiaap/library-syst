import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebaseconfig'; // Firebase Auth
import { createUserWithEmailAndPassword } from 'firebase/auth';
import { getFirestore, collection, query, where, getDocs, setDoc, doc } from 'firebase/firestore'; // Firestore

const Signup = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [name, setName] = useState('');
    const [cnp, setCnp] = useState('');
    const [error, setError] = useState(null);
    const [cnpValid, setCnpValid] = useState(false);
    const [passwordValid, setPasswordValid] = useState(false);
    const navigate = useNavigate();

    const db = getFirestore(); // Initialize Firestore

    // Validate CNP
    const validateCnp = (cnp) => {
        console.log('Validating CNP:', cnp); // Debug message
        const isValid = /^\d{13}$/.test(cnp); // CNP must be exactly 13 digits
        setCnpValid(isValid);
        console.log('CNP valid:', isValid); // Debug message
        return isValid;
    };

    // Validate Password
    const validatePassword = (password) => {
        console.log('Validating password:', password); // Debug message
        const isValid = password.length >= 8 && /[!@#$%^&*]/.test(password); // Example: 8+ characters and at least 1 special character
        setPasswordValid(isValid);
        console.log('Password valid:', isValid); // Debug message
        return isValid;
    };

    const handleSignup = async (e) => {
        e.preventDefault();
        console.log('Starting signup process'); // Debug message
        if (!validateCnp(cnp)) {
            console.error('Invalid CNP'); // Debug message
            setError('CNP must have exactly 13 digits.');
            return;
        }
        if (!validatePassword(password)) {
            console.error('Invalid Password'); // Debug message
            setError('Password does not meet the requirements.');
            return;
        }

        try {
            // Step 1: Check if the CNP is already used
            console.log('Checking if CNP is already in use'); // Debug message
            const citizensCollection = collection(db, 'users');
            const q = query(citizensCollection, where('id', '==', cnp));
            const querySnapshot = await getDocs(q);

            if (!querySnapshot.empty) {
                console.error('CNP is already in use'); // Debug message
                setError('CNP is already in use by another account.');
                return;
            }

            // Step 2: Create the user in Firebase Authentication
            console.log('Creating user in Firebase Authentication'); // Debug message
            const userCredential = await createUserWithEmailAndPassword(auth, email, password);
            const userId = userCredential.user.uid;

            // Step 3: Save citizen data in Firestore
            console.log('Saving citizen data in Firestore'); // Debug message
            await setDoc(doc(db, 'users', userId), {
                id: cnp,
                name: name,
                email: email,
                role: 'citizen',
            });
            console.log('Saving citizen profile in the citizen collection'); // Debug message
            await setDoc(doc(db, 'citizen', cnp), {
                id: cnp,
                name: name,
            });

            // Redirect to citizen dashboard
            console.log('Signup successful, redirecting to dashboard'); // Debug message
            navigate('/citizen-dashboard');
        } catch (err) {
            console.error('Error during signup:', err.message); // Debug message
            setError('Failed to create an account. Please try again.');
        }
    };

    return (
        <div className="flex flex-col items-center justify-center min-h-screen">
            <h1 className="text-2xl font-bold mb-4">Citizen Sign Up</h1>
            {error && <p className="text-red-500 mb-4">{error}</p>}
            <form onSubmit={handleSignup} className="bg-white p-6 rounded shadow-md">
                <input
                    type="text"
                    placeholder="Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    required
                    className="block w-full mb-4 p-2 border border-gray-300 rounded"
                />
                <input
                    type="text"
                    placeholder="CNP (ID)"
                    value={cnp}
                    onChange={(e) => {
                        setCnp(e.target.value);
                        validateCnp(e.target.value);
                    }}
                    required
                    className={`block w-full mb-4 p-2 border ${
                        cnpValid ? 'border-green-500' : 'border-red-500'
                    } rounded`}
                />
                <ul className="text-sm mb-4">
                    <li className={cnpValid ? 'text-green-500' : 'text-red-500'}>
                        Must be exactly 13 digits
                    </li>
                </ul>
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="block w-full mb-4 p-2 border border-gray-300 rounded"
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => {
                        setPassword(e.target.value);
                        validatePassword(e.target.value);
                    }}
                    required
                    className={`block w-full mb-4 p-2 border ${
                        passwordValid ? 'border-green-500' : 'border-red-500'
                    } rounded`}
                />
                <ul className="text-sm mb-4">
                    <li className={password.length >= 8 ? 'text-green-500' : 'text-red-500'}>
                        At least 8 characters
                    </li>
                    <li className={/[!@#$%^&*]/.test(password) ? 'text-green-500' : 'text-red-500'}>
                        At least 1 special character (!@#$%^&*)
                    </li>
                </ul>
                <button
                    type="submit"
                    className={`bg-[#A87C5A] text-white font-semibold py-2 px-20 rounded shadow-md hover:bg-[#8B5E3C] ${
                        cnpValid && passwordValid ? '' : 'opacity-50 cursor-not-allowed'
                    }`}
                    disabled={!cnpValid || !passwordValid}
                >
                    Sign Up
                </button>
            </form>
        </div>
    );
};

export default Signup;