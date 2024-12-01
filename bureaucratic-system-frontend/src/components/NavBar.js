import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Book, Home, FileText, UserPlus, Settings, Menu, X } from 'lucide-react';
import { auth } from '../firebaseconfig'; // Firebase Auth
import { signOut, onAuthStateChanged } from 'firebase/auth'; // Firebase Logout
import { getFirestore, doc, getDoc } from 'firebase/firestore'; // Firestore

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [userRole, setUserRole] = useState(null); // 'admin', 'citizen', or null
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate(); // For navigation after logout

  const db = getFirestore();

  useEffect(() => {
    const fetchUserRole = async (user) => {
      try {
        const userDoc = await getDoc(doc(db, 'users', user.uid));
        if (userDoc.exists()) {
          setUserRole(userDoc.data().role);
        } else {
          setUserRole(null);
        }
      } catch (error) {
        console.error('Error fetching user role:', error);
        setUserRole(null);
      } finally {
        setLoading(false);
      }
    };

    const unsubscribe = onAuthStateChanged(auth, (user) => {
      if (user) {
        fetchUserRole(user);
      } else {
        setUserRole(null);
        setLoading(false);
      }
    });

    return () => unsubscribe();
  }, [db]);

  const handleLogout = async () => {
    try {
      await signOut(auth); // Log out the user
      setUserRole(null); // Clear the role
      navigate('/login'); // Redirect to the login page
    } catch (error) {
      console.error('Error during logout:', error);
    }
  };

  // Define navigation items based on the role
  const navItems = [
    { name: 'Home', to: userRole === 'admin' ? '/admin-dashboard' : userRole === 'citizen' ? '/citizen-dashboard' : '/', icon: Home },
    ...(userRole === 'admin'
        ? [
          { name: 'Config', to: '/config', icon: Settings },
          { name: 'Books', to: '/books', icon: Book },
        ]
        : []),
    ...(userRole === 'citizen'
        ? [
          { name: 'Books', to: '/books', icon: Book },
          { name: 'Loan Request', to: '/loan', icon: FileText },
          { name: 'Enroll', to: '/enroll', icon: UserPlus },
          { name: 'Return Book', to: '/return', icon: UserPlus },
          { name: 'Pay Fee', to: '/pay-fee', icon: UserPlus },
        ]
        : []),
  ];

  if (loading) {
    return <p className="text-center text-gray-500">Loading...</p>;
  }

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

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center space-x-6">
              {navItems.map((item, index) => (
                  <NavItem key={item.name} item={item} index={index} />
              ))}
              {userRole && (
                  <button
                      onClick={handleLogout}
                      className="text-gray-700 font-medium hover:text-black transition duration-300"
                  >
                    Logout
                  </button>
              )}
            </div>

            {/* Mobile Menu Button */}
            <div className="md:hidden flex items-center">
              <motion.button
                  className="outline-none"
                  onClick={() => setIsOpen(!isOpen)}
                  whileHover={{ scale: 1.1 }}
                  whileTap={{ scale: 0.9 }}
              >
                <AnimatePresence mode="wait" initial={false}>
                  <motion.div
                      key={isOpen ? 'close' : 'open'}
                      initial={{ opacity: 0, rotate: -90 }}
                      animate={{ opacity: 1, rotate: 0 }}
                      exit={{ opacity: 0, rotate: 90 }}
                      transition={{ duration: 0.2 }}
                  >
                    {isOpen ? <X className="h-6 w-6 text-gray-700" /> : <Menu className="h-6 w-6 text-gray-700" />}
                  </motion.div>
                </AnimatePresence>
              </motion.button>
            </div>
          </div>
        </div>

        {/* Mobile Navigation */}
        <AnimatePresence>
          {isOpen && (
              <motion.div
                  className="md:hidden"
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  transition={{ duration: 0.3 }}
              >
                <div className="px-4 pt-2 pb-4 space-y-2">
                  {navItems.map((item, index) => (
                      <MobileNavItem key={item.name} item={item} setIsOpen={setIsOpen} index={index} />
                  ))}
                  {userRole && (
                      <button
                          onClick={handleLogout}
                          className="block px-3 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition duration-300"
                      >
                        Logout
                      </button>
                  )}
                </div>
              </motion.div>
          )}
        </AnimatePresence>
      </motion.nav>
  );
};

function NavItem({ item, index }) {
  return (
      <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3, delay: index * 0.1 }}
      >
        <Link
            to={item.to}
            className="relative group text-gray-700 font-medium hover:text-black transition duration-300"
        >
          <span>{item.name}</span>
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

function MobileNavItem({ item, setIsOpen, index }) {
  return (
      <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.3, delay: index * 0.1 }}
      >
        <Link
            to={item.to}
            className="block px-3 py-2 rounded-md text-gray-700 hover:bg-gray-100 transition duration-300"
            onClick={() => setIsOpen(false)}
        >
          <motion.div
              className="flex items-center"
              whileHover={{ x: 5 }}
              whileTap={{ scale: 0.95 }}
          >
            <item.icon className="h-5 w-5 mr-2" />
            {item.name}
          </motion.div>
        </Link>
      </motion.div>
  );
}

export default Navbar;