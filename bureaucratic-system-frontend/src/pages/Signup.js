import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebaseconfig'; // Import Firebase Auth
import { createUserWithEmailAndPassword } from 'firebase/auth';

const Signup = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleSignup = async (e) => {
    e.preventDefault();
    try {
      await createUserWithEmailAndPassword(auth, email, password);
      navigate('/login'); // Redirect to login page after successful signup
    } catch (err) {
      setError('Failed to create an account. Please try again.');
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen">
      <h1 className="text-2xl font-bold mb-4">Sign Up</h1>
      {error && <p className="text-red-500 mb-4">{error}</p>}
      <form onSubmit={handleSignup} className="bg-white p-6 rounded shadow-md">
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
          className="w-full bg-purple-500 text-white py-2 px-4 rounded hover:bg-purple-600"
        >
          Sign Up
        </button>
      </form>
    </div>
  );
};

export default Signup;
