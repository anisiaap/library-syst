import React, { useState, useEffect } from 'react';
import { getFirestore, collection, query, getDocs } from 'firebase/firestore';

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('users');
    const [data, setData] = useState({
        users: [],
        borrows: [],
        fees: [],
        memberships: [],
        books: [],
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const db = getFirestore();

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);

            try {
                // Fetch all collections
                const fetchCollection = async (collectionName) => {
                    const collectionRef = collection(db, collectionName);
                    const snapshot = await getDocs(query(collectionRef));
                    return snapshot.docs.map((doc) => ({
                        id: doc.id,
                        ...doc.data(),
                    }));
                };

                const [users, borrows, fees, memberships, books] = await Promise.all([
                    fetchCollection('users'),
                    fetchCollection('borrows'),
                    fetchCollection('fees'),
                    fetchCollection('memberships'),
                    fetchCollection('books'),
                ]);

                // Add missing membershipId handling for fees
                const feesWithFallback = fees.map((fee) => ({
                    ...fee,
                    membershipId: fee.membershipId || 'N/A',
                }));

                setData({
                    users,
                    borrows,
                    fees: feesWithFallback,
                    memberships,
                    books,
                });
            } catch (err) {
                console.error('Error fetching data:', err);
                setError('Error fetching data. Please try again later.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [db]);

    const renderTable = (items) => {
        if (!items.length) {
            return <p className="text-gray-500 text-center">No data available.</p>;
        }

        const columns = Object.keys(items[0]);

        return (
            <table className="table-auto w-full text-left border-collapse border border-gray-300">
                <thead>
                <tr>
                    {columns.map((column) => (
                        <th
                            key={column}
                            className="px-4 py-2 border border-gray-300 bg-gray-100 text-gray-800 font-semibold"
                        >
                            {column.charAt(0).toUpperCase() + column.slice(1)}
                        </th>
                    ))}
                </tr>
                </thead>
                <tbody>
                {items.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-100">
                        {columns.map((column) => (
                            <td key={column} className="px-4 py-2 border border-gray-300">
                                {item[column] !== null && item[column] !== undefined && item[column] !== ''
                                    ? item[column].toString()
                                    : 'N/A'}
                            </td>
                        ))}
                    </tr>
                ))}
                </tbody>
            </table>
        );
    };

    if (loading) {
        return <p className="text-center text-gray-600">Loading...</p>;
    }

    if (error) {
        return <p className="text-center text-red-500">{error}</p>;
    }

    const tabs = [
        { name: 'Users', key: 'users' },
        { name: 'Borrows', key: 'borrows' },
        { name: 'Fees', key: 'fees' },
        { name: 'Memberships', key: 'memberships' },
        { name: 'Books', key: 'books' },
    ];

    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
            <div className="w-full max-w-6xl bg-white rounded-lg shadow-md p-6">
                <h1 className="text-2xl font-bold mb-4 text-center">Admin Dashboard</h1>

                {/* Tabs for Navigation */}
                <div className="flex justify-center space-x-4 mb-6">
                    {tabs.map((tab) => (
                        <button
                            key={tab.key}
                            onClick={() => setActiveTab(tab.key)}
                            className={`px-4 py-2 rounded ${
                                activeTab === tab.key
                                    ? 'bg-blue-500 text-white'
                                    : 'bg-gray-200 hover:bg-gray-300'
                            }`}
                        >
                            {tab.name}
                        </button>
                    ))}
                </div>

                {/* Render the Active Tab Data */}
                <div className="bg-gray-50 p-4 rounded-lg shadow-inner">
                    {renderTable(data[activeTab])}
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
