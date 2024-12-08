import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthProvider.js'; // Import the Auth Context

import { getFirestore, collection, query, where, getDocs } from 'firebase/firestore'; // Firestore

const LoanRequest = () => {
    const { user, role } = useAuth(); // Get the user and role from AuthContext
    const [citizenId, setCitizenId] = useState('');
    const [bookTitle, setBookTitle] = useState('');
    const [bookAuthor, setBookAuthor] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const db = getFirestore();

    useEffect(() => {
        const fetchCitizenId = async () => {
            try {
                if (!user) {
                    setError('You are not logged in.');
                    setLoading(false);
                    return;
                }

                // Fetch citizen ID based on the logged-in user
                const userEmail = user.email;
                const usersCollection = collection(db, 'users');
                const q = query(usersCollection, where('email', '==', userEmail));
                const querySnapshot = await getDocs(q);

                if (!querySnapshot.empty) {
                    const userData = querySnapshot.docs[0].data();
                    setCitizenId(userData.id); // Autofill the Citizen ID
                } else {
                    setError('Citizen profile not found.');
                }
            } catch (err) {
                setError('Error fetching citizen ID: ' + err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchCitizenId();
    }, [db, user]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!role || role !== 'citizen') {
            alert('You are not authorized to submit a loan request.');
            return;
        }


        try {

            const response = await axios.post(
                'http://localhost:8080/api/citizens/loan-request',
                {
                    citizenId,
                    bookTitle,
                    bookAuthor,
                },{
                    headers: {
                        Authorization: `${role}`, // Use Bearer if required
                    }
                },
            );
            alert('Request submitted: ' + response.data);
        } catch (err) {
            alert('Error: ' + (err.response?.data || err.message));
        }
    };

    if (loading) {
        return <p className="text-center text-gray-600">Loading...</p>;
    }

    if (error) {
        return <p className="text-center text-red-500">{error}</p>;
    }

    return (
        <div
            className="flex flex-col items-center justify-center min-h-screen"
            style={{
                background: 'linear-gradient(to bottom, white, #A87C5A)',
            }}
        >
            <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-2xl">
                <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
                    Loan Request
                </h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Citizen ID
                        </label>
                        <input
                            type="text"
                            value={citizenId}
                            readOnly
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm bg-gray-100 cursor-not-allowed"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Book Title
                        </label>
                        <input
                            type="text"
                            placeholder="Enter book title"
                            value={bookTitle}
                            onChange={(e) => setBookTitle(e.target.value)}
                            required
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Book Author
                        </label>
                        <input
                            type="text"
                            placeholder="Enter book author"
                            value={bookAuthor}
                            onChange={(e) => setBookAuthor(e.target.value)}
                            required
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm"
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-[#A87C5A] text-white font-semibold py-2 rounded shadow-md hover:bg-[#8B5E3C]"
                    >
                        Submit Request
                    </button>
                </form>
            </div>
        </div>
    );
};

export default LoanRequest;
