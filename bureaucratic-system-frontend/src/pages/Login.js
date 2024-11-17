import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { auth } from '../firebaseconfig'; // Import Firebase Auth
import { signInWithEmailAndPassword } from 'firebase/auth';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const token = await userCredential.user.getIdToken(); // Get the JWT token
      localStorage.setItem('authToken', token); // Save token to localStorage
      navigate('/books'); // Redirect to the books page
    } catch (err) {
      setError('Invalid email or password');
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen ">
      <h1 className="text-2xl font-bold mb-4">Login</h1>
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
          className="w-full bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600"
        >
          Login
        </button>
      </form>
    </div>
  );
};

export default Login;
