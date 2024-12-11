import React from 'react';
import { Link } from 'react-router-dom';

const NotFoundPage = () => (
  <div className="flex flex-col items-center justify-center h-screen text-center">
    <h1 className="text-4xl font-bold mb-4">404 - Page Not Found</h1>
    <p className="text-lg mb-6">Oops! The page you're looking for doesn't exist.</p>
    <Link to="/" className="text-blue-400 hover:underline">
      Go back to home
    </Link>
  </div>
);

export default NotFoundPage;
