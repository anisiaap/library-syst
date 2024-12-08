import React from 'react';
import { Link } from 'react-router-dom';

const WelcomeClient = () => (
    <div className="flex flex-col items-center justify-center h-screen text-center  from-blue-500 to-purple-600 text-white">
        <h1 className="text-4xl font-bold mb-6">Welcome to the our</h1>
        <h1 className="text-4xl font-bold mb-6">Bureaucratic Library System</h1>

        <p className="text-lg mb-8">Explore the app by logging in or signing up to get started!</p>
        <div className="flex space-x-4">
            <Link
                to="/login"
                className="bg-white text-[#A87C5A] font-semibold py-2 px-6 rounded shadow-md hover:bg-brown-100"
            >
                Login
            </Link>
            {/* Client button with brown background */}
            <Link
                to="/signup"
                className="bg-[#A87C5A] text-white font-semibold py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
            >
                Sign Up
            </Link>
        </div>
    </div>
);

export default WelcomeClient;
