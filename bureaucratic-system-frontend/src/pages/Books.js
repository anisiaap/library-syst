import React, { useEffect, useState } from 'react';
import axios from 'axios';

const Books = () => {
    const [groupedBooks, setGroupedBooks] = useState([]);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchBooks = async () => {
            try {
                const token = localStorage.getItem('authToken'); // Get the token from localStorage
                const response = await axios.get('http://localhost:8080/api/firebase/books', {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                });

                setGroupedBooks(response.data);
            } catch (error) {
                console.error('Error fetching books:', error);
                setError('Failed to load books. Please try again.');
            }
        };

        fetchBooks();
    }, []);

    return (
        <div
            className="flex flex-col items-center justify-center min-h-screen"
            style={{
                background: 'linear-gradient(to bottom, white, #A87C5A)',
            }}
        >
            <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-4xl">
                <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
                    Available Books
                </h1>
                {error ? (
                    <p className="text-red-500 text-center">{error}</p>
                ) : groupedBooks.length > 0 ? (
                    <ul className="space-y-4">
                        {groupedBooks.map((book, index) => (
                            <li
                                key={`${book.name}-${book.author}-${index}`}
                                className="flex justify-between items-center bg-gray-100 p-4 rounded-lg shadow-md"
                            >
                                <div>
                                    <p className="text-lg font-semibold text-gray-800">
                                        {book.name}
                                    </p>
                                    <p className="text-sm text-gray-600">by {book.author}</p>
                                </div>
                                <div>
                                    <p className="text-lg font-semibold text-gray-700">
                                        Pieces Available: {book.totalPieces}
                                    </p>
                                </div>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p className="text-gray-600 text-center">No books available.</p>
                )}
            </div>
        </div>
    );
};

export default Books;