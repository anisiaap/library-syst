import React from 'react';
import { Routes, Route } from 'react-router-dom';
import WelcomePage from './pages/WelcomePage';
import ParticlesBackground from './components/ParticlesBackground';
import NotFoundPage from './components/NotFoundPage';
import { AuthProvider } from './components/AuthProvider.js';
import BooksPage from "./pages/Books";
import UsersPage from "./pages/Users";
import RevenuePage from "./pages/Revenue";
import StatisticsNavbar from './components/StatisticsNavBar'; // Import the Navbar

function App() {
    return (
        <AuthProvider>
            {/* Background with Particles */}
            <ParticlesBackground />

            <div className="flex flex-col min-h-screen relative z-10 from-blue-500 to-purple-600 text-black">
                {/* Navbar */}
                <StatisticsNavbar />

                {/* Main Content Area */}
                <main className="flex-grow">
                    <Routes>
                        {/* Public Routes */}
                        <Route path="/" element={<WelcomePage />} />
                        <Route path="/users" element={<UsersPage />} />
                        <Route path="/books" element={<BooksPage />} />
                        <Route path="/revenue" element={<RevenuePage />} />

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
