import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Home } from 'lucide-react';

const StatisticsNavBar = () => {
    const location = useLocation(); // Get the current path

    return (
        <motion.nav
            className="bg-white shadow-lg"
            initial={{ y: -100 }}
            animate={{ y: 0 }}
            transition={{ type: 'spring', stiffness: 100, damping: 15 }}
        >
            <div className="max-w-6xl mx-auto px-4">
                <div className="flex justify-between items-center py-4">
                    {/* Logo Section */}
                    <motion.div whileHover={{ scale: 1.05 }} whileTap={{ scale: 0.95 }}>
                        <Link to="/" className="flex items-center">
                            <span className="font-semibold text-gray-700 text-lg">MyLibrary</span>
                        </Link>
                    </motion.div>

                    {/* Conditional Navigation Items */}
                    <div className="hidden md:flex items-center space-x-6">
                        {location.pathname !== '/admin-dashboard' && (
                            <NavItem
                                name="Go Back to Admin Page"
                                to="http://localhost:3000/admin-dashboard"
                                icon={Home}
                            />
                        )}
                    </div>
                </div>
            </div>
        </motion.nav>
    );
};

function NavItem({ name, to, icon: Icon }) {
    return (
        <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
        >
            <Link
                to={to}
                className="relative group text-gray-700 font-medium hover:text-black transition duration-300 flex items-center"
            >
                <Icon className="mr-2 h-5 w-5" />
                <span>{name}</span>
                <motion.span
                    className="absolute bottom-0 left-0 w-full h-0.5 bg-black"
                    initial={{ scaleX: 0 }}
                    whileHover={{ scaleX: 1 }}
                    transition={{ duration: 0.3 }}
                />
            </Link>
        </motion.div>
    );
}

export default StatisticsNavBar;
