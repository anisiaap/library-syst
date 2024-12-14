import React, { useState, useEffect } from 'react';
import { auth } from '../firebaseconfig'; // Firebase Auth
import { getFirestore, collection, query, where, getDocs } from 'firebase/firestore'; // Firestore

const CitizenDashboard = () => {
    const [profile, setProfile] = useState(null);
    const [membership, setMembership] = useState(null);
    const [borrowHistory, setBorrowHistory] = useState([]);
    const [feesHistory, setFeesHistory] = useState([]);
    const [books, setBooks] = useState([]); // Store all books
    const [borrows, setBorrows] = useState([]); // Store all borrows
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

                // Fetch user profile
                const usersCollection = collection(db, 'users');
                const userQuery = query(usersCollection, where('email', '==', userEmail));
                const userSnapshot = await getDocs(userQuery);

                if (!userSnapshot.empty) {
                    const userData = userSnapshot.docs[0].data();
                    setProfile(userData);

                    // Fetch membership
                    const membershipsCollection = collection(db, 'memberships');
                    const membershipQuery = query(membershipsCollection, where('citizenId', '==', userData.id));
                    const membershipSnapshot = await getDocs(membershipQuery);

                    if (!membershipSnapshot.empty) {
                        const membershipData = membershipSnapshot.docs[0].data();
                        setMembership(membershipData);

                        // Fetch all books
                        const booksCollection = collection(db, 'books');
                        const booksSnapshot = await getDocs(booksCollection);
                        const booksData = booksSnapshot.docs.map(doc => ({
                            ...doc.data(),
                            id: doc.id, // Ensure `id` is included
                        }));
                        setBooks(booksData);

                        // Fetch all borrows
                        const borrowsCollection = collection(db, 'borrows');
                        const borrowsQuery = query(borrowsCollection, where('membershipId', '==', membershipData.id));
                        const borrowsSnapshot = await getDocs(borrowsQuery);
                        const borrowsData = borrowsSnapshot.docs.map(doc => ({
                            ...doc.data(),
                            id: doc.id, // Ensure `id` is included
                        }));
                        setBorrows(borrowsData);

                        // Map borrow history to include book details
                        const borrowsWithBooks = borrowsData.map(borrow => {
                            const book = booksData.find(book => book.id === borrow.bookId);
                            return {
                                ...borrow,
                                bookTitle: book?.name || 'Unknown Book',
                                bookAuthor: book?.author || 'Unknown Author',
                            };
                        });
                        setBorrowHistory(borrowsWithBooks);

                        // Fetch fees history and include related book details
                        const feesCollection = collection(db, 'fees');
                        const feesQuery = query(feesCollection, where('membershipId', '==', membershipData.id));
                        const feesSnapshot = await getDocs(feesQuery);
                        const feesData = feesSnapshot.docs.map(feeDoc => {
                            const fee = feeDoc.data();
                            const borrow = borrowsData.find(borrow => borrow.id === fee.borrowId);
                            const book = booksData.find(book => book.id === borrow?.bookId);
                            return {
                                ...fee,
                                bookTitle: book?.name || 'Unknown Book',
                                bookAuthor: book?.author || 'Unknown Author',
                                paid: fee.paid || false, // Ensure `paid` is a boolean
                            };
                        });

                        console.log('Fees Data:', feesData); // Debugging output
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
            {/* User and Membership Details */}
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

            {/* Borrow and Fees History */}
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
                            <ul className="space-y-4">
                                {borrowHistory.map((borrow, index) => (
                                    <li key={index} className="p-4 border border-gray-300 rounded-lg shadow-sm">
                                        <h3 className="text-lg font-semibold text-gray-800">
                                            {borrow.bookTitle}
                                        </h3>
                                        <p className="text-gray-600 mb-2">
                                            <strong>Author:</strong> {borrow.bookAuthor}
                                        </p>
                                        <p className="text-gray-600">
                                            <strong>Expected Return Date:</strong> {borrow.dueDate || 'Unknown'}
                                        </p>
                                        <div className="mt-2 text-sm text-gray-500">
                                            <p><strong>Borrow Date:</strong> {borrow.borrowDate}</p>
                                            <p>
                                                <strong>Return Date:</strong>{' '}
                                                {borrow.returnDate || 'Not returned yet'}
                                            </p>
                                        </div>
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

            {/* Fees History */}
            {showFees && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                    <div className="bg-white rounded-lg shadow-lg p-6 w-96">
                        <h2 className="text-xl font-bold text-gray-800 mb-4">Fees History</h2>
                        {feesHistory.length > 0 ? (
                            <ul className="space-y-4">
                                {feesHistory.map((fee, index) => (
                                    <li key={index} className="p-4 border border-gray-300 rounded-lg shadow-sm">
                                        <h3 className="text-lg font-semibold text-gray-800">
                                            {fee.bookTitle}
                                        </h3>
                                        <p className="text-gray-600 mb-2">
                                            <strong>Author:</strong> {fee.bookAuthor}
                                        </p>
                                        <p className="text-gray-600">
                                            <strong>Fee Amount:</strong> ${fee.amount}
                                        </p>
                                        <div className="mt-2 text-sm text-gray-500">
                                            <p><strong>Fee ID:</strong> {fee.id}</p>
                                            <p><strong>Paid:</strong> {fee.paid}</p>
                                        </div>
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
