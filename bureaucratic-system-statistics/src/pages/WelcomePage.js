import React from 'react';
import { Link } from 'react-router-dom';

const WelcomePage = () => (
  <div className="flex flex-col items-center justify-center h-screen text-center  from-blue-500 to-purple-600 text-white">
      <img
          src = "/logo.png"
          alt="Books Icon"
          className="w-80 h-75 mb-4"
      />
      <h1 className="text-4xl font-bold mb-6">Bureaucratic Library System</h1>
      <h1 className="text-4xl font-bold mb-6">Statistics</h1>

    <p className="text-lg mb-8">Select a category below to explore the statistics!</p>
    <div className="flex space-x-4">
        {/* User charts button with brown text */}
        <Link
            to="/users"
            className="bg-[#A87C5A] text-white font-semibold py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
        >
            Users
        </Link>
        {/* Books charts button with brown background */}
        <Link
            to="/books"
            className="bg-[#A87C5A] text-white font-semibold py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
        >
            Books
        </Link>
        {/* Revenue charts button with brown background */}
        <Link
            to="/revenue"
            className="bg-[#A87C5A] text-white font-semibold py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
        >
            Revenue
        </Link>

    </div>
  </div>
);

export default WelcomePage;
