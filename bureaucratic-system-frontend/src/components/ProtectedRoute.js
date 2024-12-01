import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthProvider.js';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { user, role } = useAuth(); // Fetch authenticated user and role

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(role)) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default ProtectedRoute;