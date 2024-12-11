import React, { useState, useEffect } from "react";
import { ResponsiveBar } from "@nivo/bar";
import { ResponsivePie } from "@nivo/pie";
import { db } from "../firebaseconfig.js";
import { collection, onSnapshot } from "firebase/firestore";

const UsersPage = () => {
    const [userActivity] = useState([]);
    const [membershipStatus, setMembershipStatus] = useState([]);
    const [borrowCountPerUser, setBorrowCountPerUser] = useState([]);
    const [feePaymentsPerUser, setFeePaymentsPerUser] = useState([]);
    const [citizensVsMemberships, setCitizensVsMemberships] = useState([]);

    useEffect(() => {
        const fetchUsersData = async () => {
            const citizenCollection = collection(db, "citizen");
            const membershipsCollection = collection(db, "memberships");
            const borrowsCollection = collection(db, "borrows");
            const feesCollection = collection(db, "fees");

            const citizenSnapshot = await new Promise((resolve) =>
                onSnapshot(citizenCollection, (snapshot) => resolve(snapshot.docs))
            );

            const membershipsSnapshot = await new Promise((resolve) =>
                onSnapshot(membershipsCollection, (snapshot) => resolve(snapshot.docs))
            );

            const borrowsSnapshot = await new Promise((resolve) =>
                onSnapshot(borrowsCollection, (snapshot) => resolve(snapshot.docs))
            );

            const feesSnapshot = await new Promise((resolve) =>
                onSnapshot(feesCollection, (snapshot) => resolve(snapshot.docs))
            );

            const citizensData = citizenSnapshot.map((doc) => ({ id: doc.id, ...doc.data() }));
            const membershipsData = membershipsSnapshot.map((doc) => ({ id: doc.id, ...doc.data() }));
            const borrowsData = borrowsSnapshot.map((doc) => ({ id: doc.id, ...doc.data() }));
            const feesData = feesSnapshot.map((doc) => ({ id: doc.id, ...doc.data() }));

            // Citizens vs Memberships
            const citizenIds = citizensData.map((citizen) => citizen.id);
            const membershipCitizenIds = membershipsData.map((membership) => membership.citizenId);

            const unenrolledCitizensCount = citizenIds.filter(
                (citizenId) => !membershipCitizenIds.includes(citizenId)
            ).length;

            setCitizensVsMemberships([
                { id: "Citizens with Memberships", label: "Enrolled Citizens", value: membershipCitizenIds.length, color: "#6ABF69" },
                { id: "Citizens without Memberships", label: "Unenrolled Citizens", value: unenrolledCitizensCount, color: "#FF6F61" },
            ]);

            // Map membershipId to citizen names
            const membershipIdToCitizenName = membershipsData.reduce((acc, membership) => {
                const citizen = citizensData.find((citizen) => citizen.id === membership.citizenId);
                acc[membership.id] = citizen?.name || `Citizen ${membership.citizenId}`;
                return acc;
            }, {});

            // Membership Status Distribution
            const activeMemberships = membershipsData.filter((membership) => membership.active).length;
            const inactiveMemberships = membershipsData.length - activeMemberships;

            setMembershipStatus([
                { id: "Active", label: "Active Memberships", value: activeMemberships, color: "#6ABF69" },
                { id: "Inactive", label: "Inactive Memberships", value: inactiveMemberships, color: "#FF6F61" },
            ]);

            // Borrow Count Per User
            const borrowCounts = borrowsData.reduce((acc, borrow) => {
                const userName = membershipIdToCitizenName[borrow.membershipId] || "Unknown Membership";
                acc[userName] = (acc[userName] || 0) + 1;
                return acc;
            }, {});

            const sortedBorrowCounts = Object.entries(borrowCounts)
                .map(([userName, count]) => ({ id: userName, label: userName, value: count }))
                .sort((a, b) => b.value - a.value)
                .slice(0, 10);

            setBorrowCountPerUser(sortedBorrowCounts);

            // Fee Payments Per User
            const feesByUser = feesData.reduce((acc, fee) => {
                const userName = membershipIdToCitizenName[fee.membershipId] || "Unknown Membership";
                acc[userName] = (acc[userName] || 0) + parseFloat(fee.amount);
                return acc;
            }, {});

            setFeePaymentsPerUser(
                Object.entries(feesByUser).map(([userName, total]) => ({
                    id: userName,
                    label: userName,
                    value: total,
                }))
            );
        };

        fetchUsersData();
    }, []);

    return (
        <div className="p-6 min-h-screen">
            <h1 className="text-4xl font-bold text-center text-white mb-10">
                Users Insights
            </h1>
            <div className="space-y-10">
                {/* Citizens vs Memberships */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Citizens vs Memberships
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsivePie
                            data={citizensVsMemberships}
                            margin={{ top: 40, right: 80, bottom: 80, left: 80 }}
                            innerRadius={0.5}
                            padAngle={1}
                            cornerRadius={3}
                            colors={(d) => d.data.color} // Use the color defined in the data
                            tooltip={({ datum }) => (
                                <strong>
                                    {datum.data.label}: {datum.data.value}
                                </strong>
                            )}
                            borderColor={{
                                from: "color",
                                modifiers: [["darker", 0.2]],
                            }}
                            arcLinkLabelsSkipAngle={10}
                            arcLinkLabelsTextColor="#333"
                            arcLinkLabelsThickness={2}
                            arcLinkLabelsColor={{ from: "color" }}
                            arcLabelsRadiusOffset={0.5}
                            arcLabelsSkipAngle={10}
                            arcLabelsTextColor="#ffffff"
                        />
                    </div>
                </div>

                {/* Borrow Count Per User */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Top 10 Users by Borrow Count
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={borrowCountPerUser}
                            keys={["value"]}
                            indexBy="label"
                            margin={{ top: 40, right: 40, bottom: 100, left: 60 }}
                            padding={0.3}
                            colors={{ scheme: "category10" }}
                            tooltip={({ indexValue, value }) => (
                                <strong>
                                    {indexValue}: {value} borrows
                                </strong>
                            )}
                            axisBottom={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: -45,
                                legend: "Users",
                                legendPosition: "middle",
                                legendOffset: 50,
                            }}
                            axisLeft={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: 0,
                                legend: "Borrow Count",
                                legendPosition: "middle",
                                legendOffset: -50,
                            }}
                        />
                    </div>
                </div>

                {/* Fee Payments Per User */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Fee Payments Per User
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={feePaymentsPerUser}
                            keys={["value"]}
                            indexBy="label"
                            margin={{ top: 40, right: 40, bottom: 100, left: 60 }}
                            padding={0.3}
                            colors={{ scheme: "set2" }}
                            tooltip={({ indexValue, value }) => (
                                <strong>
                                    {indexValue}: ${value.toFixed(2)}
                                </strong>
                            )}
                            axisBottom={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: -45,
                                legend: "Users",
                                legendPosition: "middle",
                                legendOffset: 50,
                            }}
                            axisLeft={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: 0,
                                legend: "Fees ($)",
                                legendPosition: "middle",
                                legendOffset: -50,
                            }}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UsersPage;