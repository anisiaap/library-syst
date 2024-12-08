import React, { useState, useEffect } from 'react';
import { getFirestore, collection, query, getDocs } from 'firebase/firestore';

const AdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('users');
    const [data, setData] = useState({ users: [], borrows: [], fees: [], memberships: [], books: [] });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const db = getFirestore();

    useEffect(() => {
        const fetchData = async () => {
            setLoading(true);
            setError(null);

            try {
                const fetchCollection = async (collectionName) => {
                    const collectionRef = collection(db, collectionName);
                    const snapshot = await getDocs(query(collectionRef));
                    return snapshot.docs.map((doc) => {
                        const data = doc.data();
                        return { id: doc.id, ...data };
                    });
                };

                // Explicitly map each collection to its key to avoid data being mixed up
                const users = await fetchCollection('users');
                const borrows = await fetchCollection('borrows');
                const fees = await fetchCollection('fees');
                const memberships = await fetchCollection('memberships');
                const books = await fetchCollection('books');

                setData({ users, borrows, fees, memberships, books });
            } catch (err) {
                console.error('Error fetching data:', err);
                setError('Error fetching data. Please try again.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [db]);

    const renderTable = (data) => {
        if (!data.length) {
            return <p className="text-gray-500 text-center">No data available.</p>;
        }

        const columns = Object.keys(data[0]);

        return (
            <table className="table-auto w-full text-left">
                <thead>
                <tr>
                    {columns.map((column) => (
                        <th key={column} className="px-4 py-2 text-gray-700">
                            {column.charAt(0).toUpperCase() + column.slice(1)}
                        </th>
                    ))}
                </tr>
                </thead>
                <tbody>
                {data.map((item) => (
                    <tr key={item.id} className="hover:bg-gray-100">
                        {columns.map((column) => (
                            <td key={column} className="border px-4 py-2">
                                {item[column] !== null && item[column] !== undefined
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

                <div className="bg-gray-50 p-4 rounded-lg shadow-inner">
                    {renderTable(data[activeTab])}
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;
