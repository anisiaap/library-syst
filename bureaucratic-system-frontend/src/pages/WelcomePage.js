import React from 'react';
import { Link } from 'react-router-dom';

const WelcomePage = () => (
  <div className="flex flex-col items-center justify-center h-screen text-center  from-blue-500 to-purple-600 text-white">
    <h1 className="text-4xl font-bold mb-6">Welcome to the Demo App</h1>
    <p className="text-lg mb-8">Explore the app by logging in or signing up to get started!</p>
    <div className="flex space-x-4">
      <Link
        to="/login"
        className="bg-white text-blue-500 font-semibold py-2 px-6 rounded shadow-md hover:bg-blue-100"
      >
        Login
      </Link>
      <Link
        to="/signup"
        className="bg-blue-500 text-white font-semibold py-2 px-6 rounded shadow-md hover:bg-blue-600"
      >
        Sign Up
      </Link>
    </div>
  </div>
);

export default WelcomePage;
