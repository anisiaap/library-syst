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
import WelcomeAdmin from "./pages/WelcomeAdmin";
import WelcomeClient from "./pages/WelcomeClient";
import AdminLogin from "./pages/AdminLogin";
import CitizenDashboard from "./pages/CitizenDashboard";
import { AuthProvider } from './components/AuthProvider.js';
import ReturnRequest from "./pages/ReturnBook";
import PayFee from "./pages/PayFee";
import AdminDashboard from "./pages/AdminDashboard"; // AuthProvider for managing authentication

function App() {
    return (
        <AuthProvider>
            {/* Background with Particles */}
            <ParticlesBackground />
            <div className="flex flex-col min-h-screen relative z-10 from-blue-500 to-purple-600 text-black">
                {/* Navbar */}
                <NavBar />

                {/* Main Content Area */}
                <main className="flex-grow">
                    <Routes>
                        {/* Public Routes */}
                        <Route path="/" element={<WelcomePage />} />
                        <Route path="/login" element={<Login />} />
                        <Route path="/signup" element={<Signup />} />
                        <Route path="/welcomeadmin" element={<WelcomeAdmin />} />
                        <Route path="/welcomeclient" element={<WelcomeClient />} />
                        <Route path="/loginadmin" element={<AdminLogin />} />

                        {/* Protected Routes */}
                        <Route
                            path="/books"
                            element={
                                <ProtectedRoute allowedRoles={['admin', 'citizen']}>
                                    <Books />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/citizen-dashboard"
                            element={
                                <ProtectedRoute allowedRoles={['citizen']}>
                                    <CitizenDashboard />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/admin-dashboard"
                            element={
                                <ProtectedRoute allowedRoles={['admin']}>
                                    <AdminDashboard />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/loan"
                            element={
                                <ProtectedRoute allowedRoles={['citizen']}>
                                    <LoanRequest />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/return"
                            element={
                                <ProtectedRoute allowedRoles={['citizen']}>
                                    <ReturnRequest />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/pay-fee"
                            element={
                                <ProtectedRoute allowedRoles={['citizen']}>
                                    <PayFee />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/enroll"
                            element={
                                <ProtectedRoute allowedRoles={['admin', 'citizen']}>
                                    <Enroll />
                                </ProtectedRoute>
                            }
                        />
                        <Route
                            path="/config"
                            element={
                                <ProtectedRoute allowedRoles={['admin']}>
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
                    <p>&copy; 2024 Bureaucratic Library System. All rights reserved.</p>
                </footer>
            </div>
        </AuthProvider>
    );
}

export default App;