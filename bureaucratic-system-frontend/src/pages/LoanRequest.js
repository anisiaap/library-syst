import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthProvider.js'; // Import the Auth Context
import { getFirestore, collection, getDocs } from 'firebase/firestore'; // Firestore

const LoanRequest = () => {
    const { user, role } = useAuth(); // Get the user and role from AuthContext
    const [citizenId, setCitizenId] = useState('');
    const [bookTitle, setBookTitle] = useState('');
    const [bookAuthor, setBookAuthor] = useState('');
    const [books, setBooks] = useState([]); // List of all available books
    const [suggestions, setSuggestions] = useState([]); // Filtered book suggestions
    const [showDropdown, setShowDropdown] = useState(false); // Control dropdown visibility
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [modalMessage, setModalMessage] = useState(''); // For modal messages
    const [isModalVisible, setIsModalVisible] = useState(false); // Modal visibility

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
                const querySnapshot = await getDocs(usersCollection);

                const userDoc = querySnapshot.docs.find(doc => doc.data().email === userEmail);
                if (userDoc) {
                    setCitizenId(userDoc.data().id); // Autofill the Citizen ID
                } else {
                    setError('Citizen profile not found.');
                }
            } catch (err) {
                setError('Error fetching citizen ID: ' + err.message);
            } finally {
                setLoading(false);
            }
        };

        const fetchBooks = async () => {
            try {
                // Fetch all available books from the database
                const booksCollection = collection(db, 'books');
                const booksSnapshot = await getDocs(booksCollection);
                const availableBooks = booksSnapshot.docs
                    .map(doc => doc.data())
                    .filter(book => book.available); // Only fetch available books
                setBooks(availableBooks);
                setSuggestions(availableBooks); // Initially set suggestions to all available books
            } catch (err) {
                console.error('Error fetching books:', err);
            }
        };

        fetchCitizenId();
        fetchBooks();
    }, [db, user]);

    const handleBookTitleChange = (e) => {
        const value = e.target.value;
        setBookTitle(value);

        // Filter books for suggestions
        if (value) {
            const filteredBooks = books.filter(book =>
                book.name.toLowerCase().includes(value.toLowerCase())
            );
            setSuggestions(filteredBooks);
        } else {
            setSuggestions(books); // Show all books if input is cleared
        }
    };

    const handleFocus = () => {
        // Show the dropdown when the input is focused
        setShowDropdown(true);
    };

    const handleBlur = () => {
        // Hide the dropdown when the input loses focus
        setTimeout(() => setShowDropdown(false), 200); // Add a delay to allow item selection
    };

    const handleSelectBook = (book) => {
        setBookTitle(book.name);
        setBookAuthor(book.author); // Auto-complete the author
        setShowDropdown(false); // Hide suggestions
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!role || role !== 'citizen') {
            setModalMessage('You are not authorized to submit a loan request.');
            setIsModalVisible(true);
            return;
        }

        try {
            await axios.post(
                'http://localhost:8080/api/citizens/loan-request',
                {
                    citizenId,
                    bookTitle,
                    bookAuthor,
                },
                {
                    headers: {
                        Authorization: `${role}`, // Use Bearer if required
                    },
                }
            );
            setModalMessage('Request submitted successfully! Check Borrow History!');
            setIsModalVisible(true); // Show success modal
        } catch (err) {
            setModalMessage('Error: ' + (err.response?.data || err.message));
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
                    <div className="relative">
                        <label className="block text-sm font-medium text-gray-700">
                            Book Title
                        </label>
                        <input
                            type="text"
                            placeholder="Enter book title"
                            value={bookTitle}
                            onChange={handleBookTitleChange}
                            onFocus={handleFocus} // Show suggestions on focus
                            onBlur={handleBlur} // Hide suggestions on blur
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
                                        {book.name} - {book.author}
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
                        Submit Request
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

export default LoanRequest;
