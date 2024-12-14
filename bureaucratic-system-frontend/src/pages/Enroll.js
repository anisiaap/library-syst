import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { auth } from '../firebaseconfig'; // Firebase Auth
import { getFirestore, collection, query, where, getDocs } from 'firebase/firestore';
import { useAuth } from "../components/AuthProvider"; // Firestore

const Enroll = () => {
    const [id, setId] = useState('');
    const [name, setName] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [modalMessage, setModalMessage] = useState(''); // For modal messages
    const [isModalVisible, setIsModalVisible] = useState(false); // Modal visibility
    const { role } = useAuth(); // Get the user and role from AuthContext
    const db = getFirestore();

    // Fetch citizen data for autofill
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const user = auth.currentUser;
                if (!user) {
                    setError('You are not logged in.');
                    setLoading(false);
                    return;
                }

                const userEmail = user.email;
                const usersCollection = collection(db, 'users');
                const q = query(usersCollection, where('email', '==', userEmail));
                const querySnapshot = await getDocs(q);

                if (!querySnapshot.empty) {
                    const userData = querySnapshot.docs[0].data();
                    setId(userData.id); // Autofill CNP
                    setName(userData.name); // Autofill Name
                } else {
                    setError('Profile not found.');
                }
            } catch (err) {
                setError('Error fetching profile: ' + err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchProfile();
    }, [db]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await axios.post('http://localhost:8080/api/citizens/enroll', { id, name }, {
                headers: {
                    Authorization: `${role}`, // Use the role as the token
                }
            });
            setModalMessage('Citizen enrolled successfully!');
            setIsModalVisible(true); // Show success modal
        } catch (err) {
            setModalMessage('Error: ' + err.message);
            setIsModalVisible(true); // Show error modal
        }
    };

    const closeModal = () => {
        setIsModalVisible(false);
        setModalMessage('');
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
            <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-md">
                <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
                    Enroll Citizen
                </h1>
                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label htmlFor="id" className="block text-gray-700 font-medium mb-2">
                            Citizen ID (CNP)
                        </label>
                        <input
                            id="id"
                            type="text"
                            value={id}
                            onChange={(e) => setId(e.target.value)}
                            className="w-full p-2 border border-gray-300 rounded shadow-sm focus:ring focus:ring-[#A87C5A]"
                            required
                            readOnly
                        />
                    </div>
                    <div>
                        <label htmlFor="name" className="block text-gray-700 font-medium mb-2">
                            Full Name
                        </label>
                        <input
                            id="name"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            className="w-full p-2 border border-gray-300 rounded shadow-sm focus:ring focus:ring-[#A87C5A]"
                            required
                            readOnly
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-[#A87C5A] text-white font-semibold py-2 px-4 rounded shadow-md hover:bg-[#8B5E3C]"
                    >
                        Enroll Citizen
                    </button>
                </form>
            </div>

            {/* Modal */}
            {isModalVisible && (
                <div
                    className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50"
                >
                    <div className="bg-white p-6 rounded shadow-lg max-w-sm w-full text-center">
                        <p className="text-lg font-medium mb-4">{modalMessage}</p>
                        <button
                            onClick={closeModal}
                            className="bg-[#A87C5A] text-white font-semibold py-2 px-4 rounded hover:bg-[#8B5E3C]"
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Enroll;
