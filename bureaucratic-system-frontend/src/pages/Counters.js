import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from "../components/AuthProvider";
import { getFirestore, collection, getDocs } from 'firebase/firestore';

const Counters = () => {
    const { role } = useAuth(); // Fetch role from AuthProvider
    const [department, setDepartment] = useState('BookLoaningDepartment'); // Default department
    const [counterId, setCounterId] = useState('');
    const [action, setAction] = useState('pause'); // Default to pause
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [counters, setCounters] = useState([]); // List of counters from Firestore
    const [fetchingCounters, setFetchingCounters] = useState(true);

    const db = getFirestore();

    // Fetch counters from Firestore
    useEffect(() => {
        const fetchCounters = async () => {
            try {
                const countersCollection = collection(db, 'counters');
                const snapshot = await getDocs(countersCollection);
                const fetchedCounters = snapshot.docs.map(doc => ({
                    id: doc.id,
                    ...doc.data(),
                }));
                setCounters(fetchedCounters);
            } catch (err) {
                console.error('Error fetching counters:', err);
                setCounters([]);
            } finally {
                setFetchingCounters(false);
            }
        };

        fetchCounters();
    }, [db]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            // Validate inputs
            if (!department || !counterId) {
                throw new Error('Both department and counter ID are required.');
            }
            if (counters.length === 0) {
                throw new Error('No counters initialized.');
            }

            // API endpoint and request body
            const endpoint =
                action === 'pause'
                    ? 'http://localhost:8080/api/admin/pause-counter'
                    : 'http://localhost:8080/api/admin/resume-counter';

            const response = await axios.post(
                endpoint,
                { department, counterId: parseInt(counterId, 10) },
                {
                    headers: {
                        Authorization: `${role}`,
                    },
                }
            );

            setSuccess(response.data);
        } catch (err) {
            setError(err.response?.data || err.message || 'Failed to perform action. Please try again.');
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
            <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-lg">
                <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
                    Counter Control
                </h1>

                {/* Show Counters Status */}
                <div className="mb-6">
                    {fetchingCounters ? (
                        <p className="text-gray-700 text-center">Fetching counters...</p>
                    ) : counters.length === 0 ? (
                        <p className="text-red-500 text-center">No counters initialized.</p>
                    ) : (
                        <ul className="space-y-2">
                            {counters.map((counter) => (
                                <li key={counter.id} className="text-gray-700">
                                    Counter No. {counter.counterId} -{' '}
                                    <span className={`font-medium ${counter.isPaused ? 'text-red-500' : 'text-green-500'}`}>
                                        {counter.isPaused ? 'Paused' : 'Not Paused'}
                                    </span>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">
                            Department Name
                        </label>
                        <input
                            type="text"
                            value={department}
                            onChange={(e) => setDepartment(e.target.value)}
                            placeholder="e.g., BookLoaningDepartment"
                            className="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:ring focus:ring-[#A87C5A]"
                            required
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">
                            Counter ID
                        </label>
                        <select
                            value={counterId}
                            onChange={(e) => setCounterId(e.target.value)}
                            className="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:ring focus:ring-[#A87C5A]"
                            disabled={counters.length === 0}
                            required
                        >
                            <option value="">Select Counter ID</option>
                            {counters.map((counter) => (
                                <option key={counter.id} value={counter.counterId}>
                                    Counter {counter.counterId}
                                </option>
                            ))}
                        </select>
                        {counters.length === 0 && (
                            <p className="text-red-500 text-sm mt-2">No counters initialized.</p>
                        )}
                    </div>
                    <div>
                        <label className="block text-gray-700 font-medium mb-2">
                            Action
                        </label>
                        <select
                            value={action}
                            onChange={(e) => setAction(e.target.value)}
                            className="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:ring focus:ring-[#A87C5A]"
                        >
                            <option value="pause">Pause Counter</option>
                            <option value="resume">Resume Counter</option>
                        </select>
                    </div>
                    {error && <p className="text-red-500 text-center">{error}</p>}
                    {success && <p className="text-green-500 text-center">{success}</p>}
                    <button
                        type="submit"
                        className={`w-full py-3 px-4 text-white font-semibold rounded-lg shadow-md ${
                            loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-[#A87C5A] hover:bg-[#8B5E3C]'
                        }`}
                        disabled={loading || counters.length === 0}
                    >
                        {loading ? 'Submitting...' : `Submit ${action === 'pause' ? 'Pause' : 'Resume'}`}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Counters;
