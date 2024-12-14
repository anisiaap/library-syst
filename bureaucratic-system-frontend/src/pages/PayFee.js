import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthProvider.js'; // Import Auth Context
import { getFirestore, collection, query, where, getDocs } from 'firebase/firestore'; // Firestore

const PayFee = () => {
    const { user, role } = useAuth(); // Get the user role from AuthContext
    const [membershipId, setMembershipId] = useState('');
    const [borrowId, setBorrowId] = useState('');
    const [fees, setFees] = useState([]); // List of unpaid fees
    const [suggestions, setSuggestions] = useState([]); // Filtered suggestions for dropdown
    const [showDropdown, setShowDropdown] = useState(false); // Control dropdown visibility
    const [feeDetails, setFeeDetails] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const db = getFirestore();

    useEffect(() => {
        const fetchFees = async () => {
            try {
                if (!user) {
                    setError('You are not logged in.');
                    return;
                }

                const userEmail = user.email;

                // Fetch citizen's ID
                const usersCollection = collection(db, 'users');
                const userQuery = query(usersCollection, where('email', '==', userEmail));
                const userSnapshot = await getDocs(userQuery);

                if (!userSnapshot.empty) {
                    const citizenId = userSnapshot.docs[0].data().id;

                    // Fetch membership ID
                    const membershipsCollection = collection(db, 'memberships');
                    const membershipQuery = query(membershipsCollection, where('citizenId', '==', citizenId));
                    const membershipSnapshot = await getDocs(membershipQuery);

                    if (!membershipSnapshot.empty) {
                        const membershipData = membershipSnapshot.docs[0].data();
                        setMembershipId(membershipData.id);

                        // Fetch unpaid fees
                        const feesCollection = collection(db, 'fees');
                        const feesQuery = query(feesCollection, where('membershipId', '==', membershipData.id), where('paid', '==', 'No'));
                        const feesSnapshot = await getDocs(feesQuery);

                        const feesData = await Promise.all(
                            feesSnapshot.docs.map(async (feeDoc) => {
                                const feeData = feeDoc.data();

                                // Fetch book details using borrowId
                                const borrowsCollection = collection(db, 'borrows');
                                const borrowQuery = query(borrowsCollection, where('id', '==', feeData.borrowId));
                                const borrowSnapshot = await getDocs(borrowQuery);

                                if (!borrowSnapshot.empty) {
                                    const borrowData = borrowSnapshot.docs[0].data();

                                    // Fetch book details
                                    const booksCollection = collection(db, 'books');
                                    const bookQuery = query(booksCollection, where('id', '==', borrowData.bookId));
                                    const bookSnapshot = await getDocs(bookQuery);

                                    if (!bookSnapshot.empty) {
                                        const bookData = bookSnapshot.docs[0].data();

                                        return {
                                            feeId: feeData.id,
                                            borrowId: feeData.borrowId,
                                            amount: feeData.amount,
                                            returnDate: borrowData.dueDate,
                                            bookTitle: bookData.name,
                                            bookAuthor: bookData.author,
                                        };
                                    }
                                }
                                return null;
                            })
                        );

                        setFees(feesData.filter((fee) => fee !== null));
                        setSuggestions(feesData.filter((fee) => fee !== null)); // Initialize suggestions
                    } else {
                        setError('Membership not found.');
                    }
                } else {
                    setError('User not found.');
                }
            } catch (err) {
                setError('Error fetching fees: ' + err.message);
            }
        };

        fetchFees();
    }, [db, user]);

    const handleBorrowIdChange = (e) => {
        const value = e.target.value;
        setBorrowId(value);

        if (value) {
            const filteredFees = fees.filter((fee) =>
                fee.bookTitle.toLowerCase().includes(value.toLowerCase())
            );
            setSuggestions(filteredFees);
        } else {
            setSuggestions(fees); // Reset to all fees
        }
    };

    const handleFocus = () => {
        setShowDropdown(true); // Show dropdown
    };

    const handleBlur = () => {
        setTimeout(() => setShowDropdown(false), 200); // Delay to allow selection
    };

    const handleSelectFee = (fee) => {
        setBorrowId(fee.borrowId);
        setFeeDetails(fee); // Set fee details
        setShowDropdown(false); // Hide suggestions
    };

    const handlePayFee = async () => {
        if (!feeDetails) {
            setError('No fee details available to pay.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            await axios.post(
                `http://localhost:8080/api/citizens/mark-as-paid/${borrowId}`,
                {}, // Empty body since no additional data is required
                {
                    headers: {
                        Authorization: `${role}`,
                    },
                }
            );
            setSuccess('Fee paid successfully.');
            setFeeDetails(null); // Clear details after payment
        } catch (err) {
            setError('Failed to pay fee: ' + (err.response?.data || err.message));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="flex flex-col items-center justify-center min-h-screen"
            style={{
                background: 'linear-gradient(to bottom, white, #A87C5A)',
            }}
        >
            <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-2xl">
                <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
                    Pay Fee
                </h1>
                <div className="space-y-4">
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700">
                            Borrow ID / Book Title
                        </label>
                        <input
                            type="text"
                            value={borrowId}
                            onChange={handleBorrowIdChange}
                            onFocus={handleFocus}
                            onBlur={handleBlur}
                            placeholder="Enter Borrow ID or Book Title"
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm"
                        />
                        {showDropdown && suggestions.length > 0 && (
                            <ul className="absolute bg-white border border-gray-300 rounded shadow-lg mt-1 max-h-48 overflow-auto w-full z-10">
                                {suggestions.map((fee, index) => (
                                    <li
                                        key={index}
                                        className="p-2 hover:bg-gray-200 cursor-pointer"
                                        onClick={() => handleSelectFee(fee)}
                                    >
                                        {fee.bookTitle} - {fee.bookAuthor} (Due: {fee.returnDate}) - ${fee.amount}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                    <button
                        onClick={handlePayFee}
                        className="w-full bg-[#A87C5A] text-white font-semibold py-2 rounded shadow-md hover:bg-[#8B5E3C]"
                        disabled={loading}
                    >
                        {loading ? 'Processing...' : 'Pay Fee'}
                    </button>
                </div>

                {feeDetails && (
                    <div className="mt-6 p-4 bg-gray-100 rounded shadow-sm">
                        <h2 className="text-lg font-bold text-gray-800 mb-4">Fee Details</h2>
                        <p>
                            <strong>Book Title:</strong> {feeDetails.bookTitle}
                        </p>
                        <p>
                            <strong>Author:</strong> {feeDetails.bookAuthor}
                        </p>
                        <p>
                            <strong>Return Date:</strong> {feeDetails.returnDate}
                        </p>
                        <p>
                            <strong>Amount:</strong> ${feeDetails.amount}
                        </p>
                    </div>
                )}

                {error && <p className="mt-4 text-red-500">{error}</p>}
                {success && <p className="mt-4 text-green-500">{success}</p>}
            </div>
        </div>
    );
};

export default PayFee;
