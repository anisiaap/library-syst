import React, { useState, useEffect } from 'react';
import { getFirestore, collection, query, getDocs } from 'firebase/firestore';

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('users');
    const [users, setUsers] = useState([]);
    const [borrows, setBorrows] = useState([]);
    const [fees, setFees] = useState([]);
    const [memberships, setMemberships] = useState([]);
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const db = getFirestore();

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);

                const fetchCollection = async (collectionName, setter) => {
                    const collectionRef = collection(db, collectionName);
                    const snapshot = await getDocs(query(collectionRef));
                    const data = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
                    setter(data);
                };

                await Promise.all([
                    fetchCollection('users', setUsers),
                    fetchCollection('borrows', setBorrows),
                    fetchCollection('fees', setFees),
                    fetchCollection('memberships', setMemberships),
                    fetchCollection('books', setBooks),
                ]);
            } catch (err) {
                console.error('Error fetching data:', err);
                setError('Error fetching data: ' + err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [db]);

    const renderTable = () => {
        switch (activeTab) {
            case 'users':
                return (
                    <table className="table-auto w-full text-right">
                        <thead>
                        <tr>
                            <th className="text-right">ID</th>
                            <th className="text-right">Name</th>
                            <th className="text-right">Email</th>
                        </tr>
                        </thead>
                        <tbody>
                        {users.map(user => (
                            <tr key={user.id}>
                                <td className="text-right">{user.id}</td>
                                <td className="text-right">{user.name}</td>
                                <td className="text-right">{user.email}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                );
            case 'borrows':
                return (
                    <table className="table-auto w-full text-right">
                        <thead>
                        <tr>
                            <th className="text-right">ID</th>
                            <th className="text-right">Book ID</th>
                            <th className="text-right">Membership ID</th>
                            <th className="text-right">Borrow Date</th>
                            <th className="text-right">Return Date</th>
                        </tr>
                        </thead>
                        <tbody>
                        {borrows.map(borrow => (
                            <tr key={borrow.id}>
                                <td className="text-right">{borrow.id}</td>
                                <td className="text-right">{borrow.bookId}</td>
                                <td className="text-right">{borrow.membershipId}</td>
                                <td className="text-right">{borrow.borrowDate}</td>
                                <td className="text-right">{borrow.returnDate || 'Not returned'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                );
            case 'fees':
                return (
                    <table className="table-auto w-full text-right">
                        <thead>
                        <tr>
                            <th className="text-right">ID</th>
                            <th className="text-right">Amount</th>
                            <th className="text-right">Borrow ID</th>
                            <th className="text-right">Paid</th>
                        </tr>
                        </thead>
                        <tbody>
                        {fees.map(fee => (
                            <tr key={fee.id}>
                                <td className="text-right">{fee.id}</td>
                                <td className="text-right">{fee.amount}</td>
                                <td className="text-right">{fee.borrowId}</td>
                                <td className="text-right">{fee.paid ? 'Yes' : 'No'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                );
            case 'memberships':
                return (
                    <table className="table-auto w-full text-right">
                        <thead>
                        <tr>
                            <th className="text-right">ID</th>
                            <th className="text-right">Citizen ID</th>
                            <th className="text-right">Issue Date</th>
                        </tr>
                        </thead>
                        <tbody>
                        {memberships.map(membership => (
                            <tr key={membership.id}>
                                <td className="text-right">{membership.id}</td>
                                <td className="text-right">{membership.citizenId}</td>
                                <td className="text-right">{membership.issueDate}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                );
            case 'books':
                return (
                    <table className="table-auto w-full text-right">
                        <thead>
                        <tr>
                            <th className="text-right">ID</th>
                            <th className="text-right">Title</th>
                            <th className="text-right">Author</th>
                            <th className="text-right">Available</th>
                        </tr>
                        </thead>
                        <tbody>
                        {books.map(book => (
                            <tr key={book.id}>
                                <td className="text-right">{book.id}</td>
                                <td className="text-right">{book.name}</td>
                                <td className="text-right">{book.author}</td>
                                <td className="text-right">{book.available ? 'Yes' : 'No'}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                );
            default:
                return null;
        }
    };

    if (loading) {
        return <p className="text-center text-gray-600">Loading...</p>;
    }

    if (error) {
        return <p className="text-center text-red-500">{error}</p>;
    }

    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold mb-6">Admin Dashboard</h1>

            <div className="mb-6 flex space-x-4">
                <button
                    onClick={() => setActiveTab('users')}
                    className={`px-4 py-2 rounded ${activeTab === 'users' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
                >
                    Users
                </button>
                <button
                    onClick={() => setActiveTab('borrows')}
                    className={`px-4 py-2 rounded ${activeTab === 'borrows' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
                >
                    Borrows
                </button>
                <button
                    onClick={() => setActiveTab('fees')}
                    className={`px-4 py-2 rounded ${activeTab === 'fees' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
                >
                    Fees
                </button>
                <button
                    onClick={() => setActiveTab('memberships')}
                    className={`px-4 py-2 rounded ${activeTab === 'memberships' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
                >
                    Memberships
                </button>
                <button
                    onClick={() => setActiveTab('books')}
                    className={`px-4 py-2 rounded ${activeTab === 'books' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
                >
                    Books
                </button>
            </div>

            <div className="bg-white rounded-lg shadow-lg p-4">
                {renderTable()}
            </div>
        </div>
    );
};

export default AdminDashboard;