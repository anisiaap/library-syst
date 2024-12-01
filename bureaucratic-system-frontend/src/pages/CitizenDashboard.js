import React, { useState, useEffect } from 'react';
import { auth } from '../firebaseconfig'; // Firebase Auth
import { getFirestore, collection, query, where, getDocs } from 'firebase/firestore'; // Firestore

const CitizenDashboard = () => {
    const [profile, setProfile] = useState(null);
    const [membership, setMembership] = useState(null);
    const [borrowHistory, setBorrowHistory] = useState([]);
    const [feesHistory, setFeesHistory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showBorrows, setShowBorrows] = useState(false);
    const [showFees, setShowFees] = useState(false);

    const db = getFirestore();

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
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
                    setProfile(userData);

                    const membershipsCollection = collection(db, 'memberships');
                    const membershipQuery = query(membershipsCollection, where('citizenId', '==', userData.id));
                    const membershipSnapshot = await getDocs(membershipQuery);

                    if (!membershipSnapshot.empty) {
                        const membershipData = membershipSnapshot.docs[0].data();
                        setMembership(membershipData);

                        // Fetch borrow history
                        const borrowsCollection = collection(db, 'borrows');
                        const borrowsQuery = query(borrowsCollection, where('membershipId', '==', membershipData.id));
                        const borrowsSnapshot = await getDocs(borrowsQuery);
                        const borrowsData = borrowsSnapshot.docs.map(doc => doc.data());
                        setBorrowHistory(borrowsData);

                        // Fetch fees history
                        const feesCollection = collection(db, 'fees');
                        const feesQuery = query(feesCollection, where('membershipId', '==', membershipData.id));
                        const feesSnapshot = await getDocs(feesQuery);
                        const feesData = feesSnapshot.docs.map(doc => doc.data());
                        setFeesHistory(feesData);
                    }
                } else {
                    setError('Profile not found.');
                }
            } catch (err) {
                setError('Error fetching data: ' + err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [db]);

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
            <div className="bg-white rounded-lg shadow-lg p-6 w-96 mb-6">
                <h1 className="text-2xl font-bold text-gray-800 mb-4">Citizen Dashboard</h1>
                <div className="text-gray-700">
                    <p className="mb-2"><strong>Name:</strong> {profile.name}</p>
                    <p className="mb-2"><strong>ID (CNP):</strong> {profile.id}</p>
                    <p className="mb-2"><strong>Email:</strong> {profile.email}</p>
                </div>
            </div>

            {membership && (
                <div className="bg-white rounded-lg shadow-lg p-6 w-96 mb-6">
                    <h2 className="text-xl font-bold text-gray-800 mb-4">Membership Details</h2>
                    <div className="text-gray-700">
                        <p className="mb-2"><strong>Membership ID:</strong> {membership.id}</p>
                        <p className="mb-2"><strong>Issue Date:</strong> {membership.issueDate}</p>
                    </div>
                </div>
            )}

            {!membership && (
                <div className="bg-white rounded-lg shadow-lg p-6 w-96 mb-6">
                    <h2 className="text-xl font-bold text-gray-800 mb-4">Membership Details</h2>
                    <p className="text-gray-700">No membership found.</p>
                </div>
            )}

            <div className="flex space-x-4 mt-6">
                <button
                    onClick={() => setShowBorrows(true)}
                    className="bg-[#A87C5A] text-white py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
                >
                    View Borrow History
                </button>
                <button
                    onClick={() => setShowFees(true)}
                    className="bg-[#A87C5A] text-white py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
                >
                    View Fees History
                </button>
            </div>

            {showBorrows && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                    <div className="bg-white rounded-lg shadow-lg p-6 w-96">
                        <h2 className="text-xl font-bold text-gray-800 mb-4">Borrow History</h2>
                        {borrowHistory.length > 0 ? (
                            <ul className="text-gray-700">
                                {borrowHistory.map((borrow, index) => (
                                    <li key={index} className="mb-2">
                                        <strong>Borrow ID:</strong> {borrow.id} <br />
                                        <strong>Book ID:</strong> {borrow.bookId} <br />
                                        <strong>Borrow Date:</strong> {borrow.borrowDate} <br />
                                        <strong>Due Date:</strong> {borrow.dueDate} <br />
                                        <strong>Return Date:</strong> {borrow.returnDate || 'Not returned'}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="text-gray-700">No borrow history found.</p>
                        )}
                        <button
                            onClick={() => setShowBorrows(false)}
                            className="mt-4 bg-[#A87C5A] text-white py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}

            {showFees && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                    <div className="bg-white rounded-lg shadow-lg p-6 w-96">
                        <h2 className="text-xl font-bold text-gray-800 mb-4">Fees History</h2>
                        {feesHistory.length > 0 ? (
                            <ul className="text-gray-700">
                                {feesHistory.map((fee, index) => (
                                    <li key={index} className="mb-2">
                                        <strong>Fee ID:</strong> {fee.id} <br />
                                        <strong>Amount:</strong> {fee.amount} <br />
                                        <strong>Borrow ID:</strong> {fee.borrowId} <br />
                                        <strong>Paid:</strong> {fee.paid ? 'Yes' : 'No'}
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="text-gray-700">No fees history found.</p>
                        )}
                        <button
                            onClick={() => setShowFees(false)}
                            className="mt-4 bg-[#A87C5A] text-white py-2 px-6 rounded shadow-md hover:bg-[#8B5E3C]"
                        >
                            Close
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CitizenDashboard;