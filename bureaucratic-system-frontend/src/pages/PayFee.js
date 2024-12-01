import React, { useState } from 'react';
import axios from 'axios';
import { useAuth } from '../components/AuthProvider.js'; // Import Auth Context

const PayFee = () => {
    const { role } = useAuth(); // Get the user role from AuthContext
    const [borrowId, setBorrowId] = useState('');
    const [feeDetails, setFeeDetails] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);

    const handleFetchFee = async () => {
        setError(null);
        setSuccess(null);
        setFeeDetails(null);

        if (!borrowId) {
            setError('Please enter a valid Borrow ID.');
            return;
        }

        setLoading(true);

        try {
            const response = await axios.get(
                `http://localhost:8080/api/fees/${borrowId}`,
                {
                    headers: {
                        Authorization: `Bearer ${role}`, // Use role as the token
                    },
                }
            );
            setFeeDetails(response.data);
        } catch (err) {
            setError('Failed to fetch fee details: ' + (err.response?.data || err.message));
        } finally {
            setLoading(false);
        }
    };

    const handlePayFee = async () => {
        if (!feeDetails) {
            setError('No fee details available to pay.');
            return;
        }

        setLoading(true);
        setError(null);
        setSuccess(null);

        try {
            const response = await axios.post(
                `http://localhost:8080/api/fees/pay`,
                { feeId: feeDetails.id },
                {
                    headers: {
                        Authorization: `Bearer ${role}`,
                    },
                }
            );
            setSuccess('Fee paid successfully.');
        } catch (err) {
            setError('Failed to pay fee: ' + (err.response?.data || err.message));
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
            <div className="bg-white rounded-lg shadow-lg p-8 w-full max-w-2xl">
                <h1 className="text-2xl font-bold text-gray-800 mb-6 text-center">
                    Pay Fee
                </h1>
                <div className="space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-gray-700">
                            Borrow ID
                        </label>
                        <input
                            type="text"
                            value={borrowId}
                            onChange={(e) => setBorrowId(e.target.value)}
                            placeholder="Enter Borrow ID"
                            className="block w-full p-2 border border-gray-300 rounded shadow-sm"
                        />
                    </div>
                    <button
                        onClick={handleFetchFee}
                        className="w-full bg-[#A87C5A] text-white font-semibold py-2 rounded shadow-md hover:bg-[#8B5E3C]"
                        disabled={loading}
                    >
                        {loading ? 'Fetching Fee...' : 'Get Fee Details'}
                    </button>
                </div>

                {feeDetails && (
                    <div className="mt-6 p-4 bg-gray-100 rounded shadow-sm">
                        <h2 className="text-lg font-bold text-gray-800 mb-4">Fee Details</h2>
                        <p>
                            <strong>Fee ID:</strong> {feeDetails.id}
                        </p>
                        <p>
                            <strong>Amount:</strong> ${feeDetails.amount}
                        </p>
                        <button
                            onClick={handlePayFee}
                            className="mt-4 w-full bg-green-600 text-white font-semibold py-2 rounded shadow-md hover:bg-green-700"
                            disabled={loading}
                        >
                            {loading ? 'Processing...' : 'Pay Fee'}
                        </button>
                    </div>
                )}

                {error && <p className="mt-4 text-red-500">{error}</p>}
                {success && <p className="mt-4 text-green-500">{success}</p>}
            </div>
        </div>
    );
};

export default PayFee;