import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from "../components/AuthProvider";
import { getFirestore, collection, getDocs } from 'firebase/firestore';

const Config = () => {
    const { role } = useAuth();
    const [config, setConfig] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const [currentCounters, setCurrentCounters] = useState(null); // For current counter count

    const db = getFirestore();

    // Fetch the current number of counters from Firestore
    useEffect(() => {
        const fetchCurrentCounters = async () => {
            try {
                const countersCollection = collection(db, 'counters');
                const snapshot = await getDocs(countersCollection);
                setCurrentCounters(snapshot.size); // Count documents
            } catch (err) {
                console.error('Error fetching current counters:', err);
                setCurrentCounters('Error'); // Show error if fetching fails
            }
        };

        fetchCurrentCounters();
    }, [db]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setLoading(true);

        try {
            if (!/^counters=\d+$/.test(config.trim())) {
                throw new Error('Invalid format. Use counters=number (e.g., counters=3).');
            }

            await axios.post(
                'http://localhost:8080/api/admin/config',
                { config: config.trim() },
                {
                    headers: {
                        Authorization: `${role}`,
                    },
                }
            );

            setSuccess('Configuration updated successfully!');
            setConfig('');
        } catch (err) {
            setError(err.message || 'Failed to submit configuration.');
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
                    Configure Counters
                </h1>

                {/* Show Current Counter Count */}
                <div className="mb-4 text-center">
                    <p className="text-gray-700 font-medium">
                        Current Number of Counters: {currentCounters !== null ? currentCounters : 'Loading...'}
                    </p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <textarea
                        className="w-full h-20 p-4 border border-gray-300 rounded-lg shadow-sm focus:ring focus:ring-[#A87C5A]"
                        placeholder="Enter configuration (e.g., counters=3)"
                        value={config}
                        onChange={(e) => setConfig(e.target.value)}
                        required
                    />
                    {error && (
                        <p className="text-red-500 text-center font-medium">{error}</p>
                    )}
                    {success && (
                        <p className="text-green-500 text-center font-medium">{success}</p>
                    )}
                    <button
                        type="submit"
                        className={`w-full py-3 px-4 text-white font-semibold rounded-lg shadow-md ${
                            loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-[#A87C5A] hover:bg-[#8B5E3C]'
                        }`}
                        disabled={loading}
                    >
                        {loading ? 'Submitting...' : 'Submit Configuration'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Config;
