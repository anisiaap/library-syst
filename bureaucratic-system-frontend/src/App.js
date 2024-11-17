import React from 'react';
import { Routes, Route } from 'react-router-dom';
import NavBar from './components/NavBar';
import Books from './pages/Books';
import LoanRequest from './pages/LoanRequest';
import Enroll from './pages/Enroll';
import Config from './pages/Config';
import Login from './pages/Login';
import Signup from './pages/Signup';
import WelcomePage from './pages/WelcomePage';
import ProtectedRoute from './components/ProtectedRoute';
import ParticlesBackground from './components/ParticlesBackground';
import NotFoundPage from './components/NotFoundPage';

function App() {
  return (
    <>
      {/* Background with Particles */}
      <ParticlesBackground />
      <div className="flex flex-col min-h-screen relative z-10  from-blue-500 to-purple-600 text-white">
        {/* Navbar */}
        <NavBar />
        
        {/* Main Content Area */}
        <main className="flex-grow">
          <Routes>
            {/* Public Routes */}
            <Route path="/" element={<WelcomePage />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />

            {/* Protected Routes */}
            <Route
              path="/books"
              element={
                <ProtectedRoute>
                  <Books />
                </ProtectedRoute>
              }
            />
            <Route
              path="/loan"
              element={
                <ProtectedRoute>
                  <LoanRequest />
                </ProtectedRoute>
              }
            />
            <Route
              path="/enroll"
              element={
                <ProtectedRoute>
                  <Enroll />
                </ProtectedRoute>
              }
            />
            <Route
              path="/config"
              element={
                <ProtectedRoute>
                  <Config />
                </ProtectedRoute>
              }
            />

            {/* 404 Not Found Route */}
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>

        {/* Footer */}
        <footer className="py-4 text-center bg-opacity-50 text-gray-100">
          <p>&copy; 2024 Your App Name. All rights reserved.</p>
        </footer>
      </div>
    </>
  );
}

export default App;
