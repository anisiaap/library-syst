import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from "../components/AuthProvider";

const Counters = () => {
    const { role } = useAuth(); // Fetch role from AuthProvider
    const [department, setDepartment] = useState('');
    const [counterId, setCounterId] = useState('');
    const [action, setAction] = useState('pause'); // Default to pause
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

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
            setError(err.response?.data || 'Failed to perform action. Please try again.');
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
                        <input
                            type="number"
                            value={counterId}
                            onChange={(e) => setCounterId(e.target.value)}
                            placeholder="Enter Counter ID (e.g., 1)"
                            className="w-full p-3 border border-gray-300 rounded-lg shadow-sm focus:ring focus:ring-[#A87C5A]"
                            required
                        />
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
                        disabled={loading}
                    >
                        {loading ? 'Submitting...' : `Submit ${action === 'pause' ? 'Pause' : 'Resume'}`}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Counters;
