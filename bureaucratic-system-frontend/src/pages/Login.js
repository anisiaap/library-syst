import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebaseconfig'; // Firebase Auth
import { signInWithEmailAndPassword } from 'firebase/auth';
import { getFirestore, doc, getDoc } from 'firebase/firestore'; // Firestore

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const db = getFirestore(); // Initialize Firestore

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // Step 1: Authenticate user
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const userId = userCredential.user.uid;

      // Step 2: Fetch user data from Firestore
      const userDoc = await getDoc(doc(db, 'users', userId));
      if (!userDoc.exists()) {
        throw new Error('User does not exist.');
      }

      const userData = userDoc.data();
      if (userData.role !== 'citizen') {
        throw new Error('Access denied: You are not a citizen.');
      }

      // Step 3: Successful login, navigate to citizen dashboard
      navigate('/citizen-dashboard');
    } catch (err) {
      setError('Invalid email, password, or role.');
    }
  };

  return (
      <div className="flex flex-col items-center justify-center min-h-screen">
        <h1 className="text-2xl font-bold mb-4">Login as Citizen</h1>
        {error && <p className="text-red-500 mb-4">{error}</p>}
        <form onSubmit={handleLogin} className="bg-white p-6 rounded shadow-md">
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
              onChange={(e) => setPassword(e.target.value)}
              required
              className="block w-full mb-4 p-2 border border-gray-300 rounded"
          />
          <button
              type="submit"
              className="bg-[#A87C5A] text-white font-semibold py-2 px-20 rounded shadow-md hover:bg-[#8B5E3C]"
          >
            Login
          </button>
        </form>
      </div>
  );
};

export default Login;