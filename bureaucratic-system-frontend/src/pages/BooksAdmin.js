import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { useAuth } from "../components/AuthProvider";

const Books = () => {
    const { role } = useAuth();
    const [groupedBooks, setGroupedBooks] = useState([]);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [loading, setLoading] = useState(false);
    const [newBook, setNewBook] = useState({ name: '', author: '' });
    const [updateRequest, setUpdateRequest] = useState({ bookId: '', fieldName: '', value: '' });
    const [deleteBookId, setDeleteBookId] = useState('');

    const fetchBooks = async () => {
        setLoading(true);
        setError(null);
        try {
            const response = await axios.get('http://localhost:8080/api/firebase/books', {
                headers: {
                    Authorization: `${role}`,
                },
            });
            setGroupedBooks(response.data);
        } catch (err) {
            console.error('Error fetching books:', err);
            setError('Failed to load books. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleAddBook = async () => {
        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            await axios.post(
                'http://localhost:8080/api/admin/add-book',
                newBook,
                {
                    headers: {
                        Authorization: `${role}`,
                    },
                }
            );
            setSuccess('Book added successfully.');
            setNewBook({ name: '', author: '' });
            fetchBooks(); // Refresh the book list
        } catch (err) {
            console.error('Error adding book:', err);
            setError(err.response?.data || 'Failed to add book. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleUpdateBook = async () => {
        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            await axios.put(
                'http://localhost:8080/api/admin/update-book',
                updateRequest,
                {
                    headers: {
                        Authorization: `${role}`,
                    },
                }
            );
            setSuccess('Book updated successfully.');
            setUpdateRequest({ bookId: '', fieldName: '', value: '' });
            fetchBooks(); // Refresh the book list
        } catch (err) {
            console.error('Error updating book:', err);
            setError(err.response?.data || 'Failed to update book. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteBook = async () => {
        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            await axios.delete(`http://localhost:8080/api/admin/delete-book/${deleteBookId}`, {
                headers: {
                    Authorization: `${role}`,
                },
            });
            setSuccess('Book deleted successfully.');
            setDeleteBookId('');
            fetchBooks(); // Refresh the book list
        } catch (err) {
            console.error('Error deleting book:', err);
            setError(err.response?.data || 'Failed to delete book. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
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
                    Manage Books
                </h1>
                {error && <p className="text-red-500 text-center">{error}</p>}
                {success && <p className="text-green-500 text-center">{success}</p>}
                <div className="space-y-6">
                    {/* Add Book */}
                    <div>
                        <h2 className="text-lg font-bold text-gray-800 mb-4">Add Book</h2>
                        <input
                            type="text"
                            placeholder="Book Name"
                            value={newBook.name}
                            onChange={(e) => setNewBook({ ...newBook, name: e.target.value })}
                            className="block w-full p-2 border border-gray-300 rounded mb-2"
                        />
                        <input
                            type="text"
                            placeholder="Author"
                            value={newBook.author}
                            onChange={(e) => setNewBook({ ...newBook, author: e.target.value })}
                            className="block w-full p-2 border border-gray-300 rounded mb-2"
                        />
                        <button
                            onClick={handleAddBook}
                            className="w-full bg-[#A87C5A] text-white py-2 rounded hover:bg-[#8B5E3C]"
                        >
                            Add Book
                        </button>
                    </div>

                    {/* Update Book */}
                    <div>
                        <h2 className="text-lg font-bold text-gray-800 mb-4">Update Book</h2>
                        <input
                            type="text"
                            placeholder="Book ID"
                            value={updateRequest.bookId}
                            onChange={(e) => setUpdateRequest({ ...updateRequest, bookId: e.target.value })}
                            className="block w-full p-2 border border-gray-300 rounded mb-2"
                        />
                        <input
                            type="text"
                            placeholder="Field Name (e.g., name, author)"
                            value={updateRequest.fieldName}
                            onChange={(e) => setUpdateRequest({ ...updateRequest, fieldName: e.target.value })}
                            className="block w-full p-2 border border-gray-300 rounded mb-2"
                        />
                        <input
                            type="text"
                            placeholder="New Value"
                            value={updateRequest.value}
                            onChange={(e) => setUpdateRequest({ ...updateRequest, value: e.target.value })}
                            className="block w-full p-2 border border-gray-300 rounded mb-2"
                        />
                        <button
                            onClick={handleUpdateBook}
                            className="w-full bg-[#A87C5A] text-white py-2 rounded hover:bg-[#8B5E3C]"
                        >
                            Update Book
                        </button>
                    </div>

                    {/* Delete Book */}
                    <div>
                        <h2 className="text-lg font-bold text-gray-800 mb-4">Delete Book</h2>
                        <input
                            type="text"
                            placeholder="Book ID"
                            value={deleteBookId}
                            onChange={(e) => setDeleteBookId(e.target.value)}
                            className="block w-full p-2 border border-gray-300 rounded mb-2"
                        />
                        <button
                            onClick={handleDeleteBook}
                            className="w-full bg-[#A87C5A] text-white py-2 rounded hover:bg-[#8B5E3C]"
                        >
                            Delete Book
                        </button>
                    </div>
                </div>

                {/* Book List */}
                <div className="mt-8">
                    <h2 className="text-lg font-bold text-gray-800 mb-4">Available Books</h2>
                    {groupedBooks.length > 0 ? (
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
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="text-gray-600 text-center">No books available.</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Books;
