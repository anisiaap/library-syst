import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthProvider.js'; // Import the Auth Context
import { getFirestore, collection, query, where, getDocs } from 'firebase/firestore'; // Firestore

const ReturnRequest = () => {
    const { user, role } = useAuth(); // Get the user and role from AuthContext
    const [membershipId, setMembershipId] = useState('');
    const [bookTitle, setBookTitle] = useState('');
    const [bookAuthor, setBookAuthor] = useState('');
    const [borrowedBooks, setBorrowedBooks] = useState([]); // List of borrowed books with titles and authors
    const [suggestions, setSuggestions] = useState([]); // Filtered suggestions for dropdown
    const [showDropdown, setShowDropdown] = useState(false); // Control dropdown visibility
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [modalMessage, setModalMessage] = useState(''); // Modal message
    const [isModalVisible, setIsModalVisible] = useState(false); // Modal visibility state

    const db = getFirestore();

    useEffect(() => {
        const fetchMembershipAndBorrowedBooks = async () => {
            try {
                if (!user) {
                    setError('You are not logged in.');
                    setLoading(false);
                    return;
                }

                const userEmail = user.email;

                // Step 1: Fetch user's citizen ID
                const usersCollection = collection(db, 'users');
                const userQuery = query(usersCollection, where('email', '==', userEmail));
                const userSnapshot = await getDocs(userQuery);

                if (!userSnapshot.empty) {
                    const userData = userSnapshot.docs[0].data();
                    console.log('User Data:', userData);
                    const citizenId = userData.id;

                    // Step 2: Fetch user's membership ID
                    const membershipsCollection = collection(db, 'memberships');
                    const membershipQuery = query(membershipsCollection, where('citizenId', '==', citizenId));
                    const membershipSnapshot = await getDocs(membershipQuery);

                    if (!membershipSnapshot.empty) {
                        const membershipData = membershipSnapshot.docs[0].data();
                        console.log('Membership Data:', membershipData);
                        setMembershipId(membershipData.id);

                        // Step 3: Fetch borrowed books (not returned)
                        const borrowsCollection = collection(db, 'borrows');
                        const borrowsQuery = query(
                            borrowsCollection,
                            where('membershipId', '==', membershipData.id),
                            where('returnDate', '==', null)
                        );
                        const borrowsSnapshot = await getDocs(borrowsQuery);

                        // Step 4: Cross-reference books using bookId
                        const borrowedBooks = await Promise.all(
                            borrowsSnapshot.docs.map(async (borrowDoc) => {
                                const borrowData = borrowDoc.data();
                                const bookQuery = query(
                                    collection(db, 'books'),
                                    where('id', '==', borrowData.bookId)
                                );
                                const bookSnapshot = await getDocs(bookQuery);

                                if (!bookSnapshot.empty) {
                                    const bookData = bookSnapshot.docs[0].data();
                                    return {
                                        bookTitle: bookData.name,
                                        bookAuthor: bookData.author,
                                        borrowDate: borrowData.borrowDate,
                                        dueDate: borrowData.dueDate,
                                        bookId: borrowData.bookId,
                                    };
                                } else {
                                    console.error('No book found for bookId:', borrowData.bookId);
                                    return null;
                                }
                            })
                        );

                        const validBooks = borrowedBooks.filter((book) => book !== null);
                        setBorrowedBooks(validBooks);
                        setSuggestions(validBooks); // Initialize suggestions
                    } else {
                        console.error('No membership found for citizen ID:', citizenId);
                        setError('Membership profile not found.');
                    }
                } else {
                    console.error('No user found for email:', userEmail);
                    setError('Citizen profile not found.');
                }
            } catch (err) {
                console.error('Error fetching borrowed books:', err.message);
                setError('Error fetching data: ' + err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchMembershipAndBorrowedBooks();
    }, [db, user]);

    const handleBookTitleChange = (e) => {
        const value = e.target.value;
        setBookTitle(value);

        // Filter books for suggestions
        if (value) {
            const filteredBooks = borrowedBooks.filter(
                (book) => book.bookTitle && book.bookTitle.toLowerCase().includes(value.toLowerCase())
            );
            setSuggestions(filteredBooks);
        } else {
            setSuggestions(borrowedBooks); // Show all borrowed books if input is cleared
        }
    };

    const handleFocus = () => {
        setShowDropdown(true); // Show dropdown on focus
    };

    const handleBlur = () => {
        setTimeout(() => setShowDropdown(false), 200); // Add delay to allow selection
    };

    const handleSelectBook = (book) => {
        setBookTitle(book.bookTitle);
        setBookAuthor(book.bookAuthor); // Auto-complete the author
        setShowDropdown(false); // Hide suggestions
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!role || role !== 'citizen') {
            setModalMessage('You are not authorized to submit a return request.');
            setIsModalVisible(true);
            return;
        }

        try {
            await axios.post(
                'http://localhost:8080/api/returns/return-book',
                { membershipId, bookTitle, bookAuthor },
                { headers: { Authorization: `Bearer ${role}` } }
            );
            setModalMessage('Return processed successfully! Check Borrow History and Fee History!');
        } catch (err) {
            const errorMessage =
                err.response?.data?.message ||
                err.message ||
                'An unknown error occurred.';
            setModalMessage(`Error: ${errorMessage}`);
        } finally {
            setIsModalVisible(true);
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
            <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-2xl">
                <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
                    Return Request
                </h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Membership ID
                        </label>
                        <input
                            type="text"
                            value={membershipId}
                            readOnly
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm bg-gray-100 cursor-not-allowed"
                        />
                    </div>
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700">
                            Book Title
                        </label>
                        <input
                            type="text"
                            placeholder="Enter book title"
                            value={bookTitle}
                            onChange={handleBookTitleChange}
                            onFocus={handleFocus}
                            onBlur={handleBlur}
                            required
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm"
                        />
                        {showDropdown && suggestions.length > 0 && (
                            <ul className="absolute bg-white border border-gray-300 rounded shadow-lg mt-1 max-h-48 overflow-auto w-full z-10">
                                {suggestions.map((book, index) => (
                                    <li
                                        key={index}
                                        className="p-2 hover:bg-gray-200 cursor-pointer"
                                        onClick={() => handleSelectBook(book)}
                                    >
                                        {book.bookTitle} - {book.bookAuthor}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Book Author
                        </label>
                        <input
                            type="text"
                            value={bookAuthor}
                            readOnly
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm bg-gray-100 cursor-not-allowed"
                        />
                    </div>
                    <button
                        type="submit"
                        className="w-full bg-[#A87C5A] text-white font-semibold py-2 rounded shadow-md hover:bg-[#8B5E3C]"
                    >
                        Submit Return
                    </button>
                </form>
            </div>

            {/* Modal */}
            {isModalVisible && (
                <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
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

export default ReturnRequest;
