import React, { useState, useEffect } from "react";
import { ResponsiveBar } from "@nivo/bar";
import { ResponsivePie } from "@nivo/pie";
import { ResponsiveLine } from "@nivo/line";
import { db } from "../firebaseconfig.js";
import { collection, onSnapshot } from "firebase/firestore";

const RevenuePage = () => {
    const [revenueByMember, setRevenueByMember] = useState([]);
    const [revenueByBook, setRevenueByBook] = useState([]);
    const [feesOverTime, setFeesOverTime] = useState([]);
    const [booksWithFees, setBooksWithFees] = useState([]);
    const [booksWithZeroFees, setBooksWithZeroFees] = useState([]);

    useEffect(() => {
        const fetchRevenueData = async () => {
            const booksCollection = collection(db, "books");
            const borrowsCollection = collection(db, "borrows");
            const feesCollection = collection(db, "fees");

            const booksSnapshot = await new Promise((resolve) =>
                onSnapshot(booksCollection, (snapshot) => resolve(snapshot.docs))
            );

            const borrowsSnapshot = await new Promise((resolve) =>
                onSnapshot(borrowsCollection, (snapshot) => resolve(snapshot.docs))
            );

            const feesSnapshot = await new Promise((resolve) =>
                onSnapshot(feesCollection, (snapshot) => resolve(snapshot.docs))
            );

            const booksData = booksSnapshot.map((doc) => ({ id: doc.id, ...doc.data() }));
            const borrowsData = borrowsSnapshot.map((doc) => ({ id: doc.id, ...doc.data() }));
            const feesData = feesSnapshot.map((doc) => ({ id: doc.id, ...doc.data() }));

            // Revenue by Member
            const revenueByMemberData = feesData.reduce((acc, fee) => {
                const memberId = fee.membershipId;
                const amount = parseFloat(fee.amount);
                acc[memberId] = (acc[memberId] || 0) + amount;
                return acc;
            }, {});

            setRevenueByMember(
                Object.entries(revenueByMemberData).map(([memberId, total]) => ({
                    id: memberId,
                    label: `Member ${memberId}`,
                    value: total,
                }))
            );

            // Revenue by Book
            const revenueByBookData = feesData.reduce((acc, fee) => {
                const borrow = borrowsData.find((b) => b.id === fee.borrowId);
                const book = booksData.find((b) => b.id === borrow.bookId);
                if (book) {
                    acc[book.name] = (acc[book.name] || 0) + parseFloat(fee.amount);
                }
                return acc;
            }, {});

            setRevenueByBook(
                Object.entries(revenueByBookData).map(([bookTitle, revenue]) => ({
                    id: bookTitle,
                    label: bookTitle,
                    value: revenue,
                }))
            );

            // Fees Over Time
            const feesByDate = feesData.reduce((acc, fee) => {
                const borrow = borrowsData.find((b) => b.id === fee.borrowId);
                const date = new Date(borrow.borrowDate).toLocaleDateString("en-US", {
                    month: "short",
                    year: "numeric",
                });
                acc[date] = (acc[date] || 0) + parseFloat(fee.amount);
                return acc;
            }, {});

            setFeesOverTime(
                Object.entries(feesByDate).map(([date, total]) => ({
                    id: date,
                    label: date,
                    value: total,
                }))
            );

            // Books Generating Fees
            const booksWithFeesData = feesData.map((fee) => {
                const borrow = borrowsData.find((b) => b.id === fee.borrowId);
                const book = booksData.find((b) => b.id === borrow.bookId);
                return {
                    id: fee.id,
                    label: book?.name || "Unknown",
                    value: parseFloat(fee.amount),
                };
            });

            setBooksWithFees(booksWithFeesData);

            // Books with Zero Fees
            const feeBookIds = feesData.map((fee) => {
                const borrow = borrowsData.find((b) => b.id === fee.borrowId);
                return borrow?.bookId;
            });

            const zeroFeeBooks = booksData.filter((book) => !feeBookIds.includes(book.id));

            setBooksWithZeroFees([
                { id: "Books with Fees", label: "Books with Fees", value: feeBookIds.length },
                { id: "Books with Zero Fees", label: "Books with Zero Fees", value: zeroFeeBooks.length },
            ]);
        };

        fetchRevenueData();
    }, []);

    return (
        <div className="p-6 min-h-screen">
            <h1 className="text-4xl font-bold text-center text-white mb-10">
                Revenue Insights
            </h1>
            <div className="space-y-10">
                {/* Revenue by Member */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Revenue by Member
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={revenueByMember}
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
                                legend: "Member ID",
                                legendPosition: "middle",
                                legendOffset: 50,
                            }}
                            axisLeft={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: 0,
                                legend: "Revenue ($)",
                                legendPosition: "middle",
                                legendOffset: -50,
                            }}
                        />
                    </div>
                </div>

                {/* Revenue by Book */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Revenue by Book
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={revenueByBook}
                            keys={["value"]}
                            indexBy="label"
                            margin={{ top: 40, right: 40, bottom: 100, left: 60 }}
                            padding={0.3}
                            colors={{ scheme: "category10" }}
                            tooltip={({ indexValue, value }) => (
                                <strong>
                                    {indexValue}: ${value.toFixed(2)}
                                </strong>
                            )}
                            axisBottom={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: -45,
                                legend: "Book Titles",
                                legendPosition: "middle",
                                legendOffset: 50,
                            }}
                            axisLeft={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: 0,
                                legend: "Revenue ($)",
                                legendPosition: "middle",
                                legendOffset: -50,
                            }}
                        />
                    </div>
                </div>

                {/* Fees Over Time */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Fees Over Time
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveLine
                            data={[
                                {
                                    id: "Fees",
                                    data: feesOverTime.map(({ label, value }) => ({
                                        x: label,
                                        y: value.toFixed(2), // Ensure values are formatted
                                    })),
                                },
                            ]}
                            margin={{ top: 50, right: 50, bottom: 100, left: 60 }}
                            xScale={{ type: "point" }}
                            yScale={{ type: "linear", min: "auto", max: "auto", stacked: false, reverse: false }}
                            axisTop={null}
                            axisRight={null}
                            axisBottom={{
                                orient: "bottom",
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: -45,
                                legend: "Date",
                                legendOffset: 50,
                                legendPosition: "middle",
                            }}
                            axisLeft={{
                                orient: "left",
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: 0,
                                legend: "Fees ($)",
                                legendOffset: -50,
                                legendPosition: "middle",
                            }}
                            colors="#1f77b4" // Single blue color
                            pointSize={8}
                            pointColor={{ theme: "background" }}
                            pointBorderWidth={2}
                            pointBorderColor={{ from: "serieColor" }}
                            pointLabelYOffset={-12}
                            useMesh={true}
                            tooltip={({ point }) => (
                                <div>
                                    <strong>{point.data.xFormatted}</strong>: ${point.data.yFormatted}
                                </div>
                            )}
                        />
                    </div>
                </div>

                {/* Books with Zero Fees */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Books With or Without Fees
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsivePie
                            data={booksWithZeroFees}
                            margin={{ top: 40, right: 80, bottom: 80, left: 80 }}
                            innerRadius={0.5}
                            padAngle={1}
                            cornerRadius={3}
                            colors={{ scheme: "paired" }}
                            tooltip={({ datum }) => (
                                <strong>
                                    {datum.data.label}: {datum.data.value} books
                                </strong>
                            )}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default RevenuePage;