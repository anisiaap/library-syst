import React, { useState, useEffect } from "react";
import { ResponsiveBar } from "@nivo/bar";
import { ResponsivePie } from "@nivo/pie";
import { db } from "../firebaseconfig.js";
import { collection, onSnapshot } from "firebase/firestore";

const BooksPage = () => {
    const [booksWithFees, setBooksWithFees] = useState([]);
    const [booksPerAuthor, setBooksPerAuthor] = useState([]);
    const [availabilityData, setAvailabilityData] = useState([]);
    const [averageBorrowTime, setAverageBorrowTime] = useState([]);
    const [totalRevenueByBook, setTotalRevenueByBook] = useState([]);
    const [mostBorrowedBooks, setMostBorrowedBooks] = useState([]); // Add state for top 10 borrowed books

    useEffect(() => {
        const fetchBooksData = async () => {
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

            // Books per Author
            const authorCounts = booksData.reduce((acc, book) => {
                acc[book.author] = (acc[book.author] || 0) + 1;
                return acc;
            }, {});

            setBooksPerAuthor(
                Object.entries(authorCounts).map(([author, count]) => ({
                    id: author,
                    label: author,
                    value: count,
                }))
            );

            // Top 10 Borrowed Books
            const borrowCounts = borrowsData.reduce((acc, borrow) => {
                const book = booksData.find((b) => b.id === borrow.bookId);
                if (book) {
                    acc[book.name] = (acc[book.name] || 0) + 1;
                }
                return acc;
            }, {});

            const sortedBorrowCounts = Object.entries(borrowCounts)
                .map(([bookTitle, count]) => ({ id: bookTitle, label: bookTitle, value: count }))
                .sort((a, b) => b.value - a.value)
                .slice(0, 10); // Get the top 10

            setMostBorrowedBooks(sortedBorrowCounts); // Save to state

            // Books Availability
            const availableBooks = booksData.filter((book) => book.available);
            const unavailableBooks = booksData.filter((book) => !book.available);

            setAvailabilityData([
                { id: "Available", label: "Available", value: availableBooks.length, color: "#6ABF69" },
                { id: "Borrowed", label: "Borrowed", value: unavailableBooks.length, color: "#FF6F61" },
            ]);

            // Average Borrow Time per Book
            const borrowTimes = borrowsData.reduce((acc, borrow) => {
                const book = booksData.find((b) => b.id === borrow.bookId);
                if (!borrow.returnDate || !book) return acc;

                const borrowDate = new Date(borrow.borrowDate);
                const returnDate = new Date(borrow.returnDate);
                const time = (returnDate - borrowDate) / (1000 * 60 * 60 * 24); // Days

                if (!acc[book.name]) acc[book.name] = [];
                acc[book.name].push(time);

                return acc;
            }, {});

            const avgBorrowTimes = Object.entries(borrowTimes).map(([bookTitle, times]) => ({
                id: bookTitle,
                label: bookTitle,
                value: times.reduce((sum, t) => sum + t, 0) / times.length,
            }));

            setAverageBorrowTime(avgBorrowTimes);

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

            // Total Revenue by Book
            const revenueByBook = feesData.reduce((acc, fee) => {
                const borrow = borrowsData.find((b) => b.id === fee.borrowId);
                const book = booksData.find((b) => b.id === borrow.bookId);
                if (book) {
                    acc[book.name] = (acc[book.name] || 0) + parseFloat(fee.amount);
                }
                return acc;
            }, {});

            setTotalRevenueByBook(
                Object.entries(revenueByBook).map(([bookTitle, revenue]) => ({
                    id: bookTitle,
                    label: bookTitle,
                    value: revenue,
                }))
            );
        };

        fetchBooksData();
    }, []);

    return (
        <div className="p-6 min-h-screen">
            <h1 className="text-4xl font-bold text-center text-white mb-10">
                Books Insights
            </h1>
            <div className="space-y-10">

                {/* Books Availability */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Books Availability
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsivePie
                            data={availabilityData.map((item, index) => ({
                                ...item,
                                color:
                                    index === 0
                                        ? "#87CEEB" // Lighter blue for Available
                                        : "#4682B4", // Darker blue for Borrowed
                            }))}
                            margin={{ top: 40, right: 80, bottom: 80, left: 80 }}
                            innerRadius={0.5}
                            padAngle={1}
                            cornerRadius={3}
                            colors={(d) => d.data.color} // Use the color defined in the data
                            tooltip={({ datum }) => (
                                <strong>
                                    {datum.data.label}: {datum.data.value} books
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

                {/* Top 10 Borrowed Books */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Top 10 Borrowed Books
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={mostBorrowedBooks}
                            keys={["value"]}
                            indexBy="label"
                            margin={{ top: 40, right: 40, bottom: 100, left: 60 }}
                            padding={0.3}
                            colors={(bar) => {
                                const value = bar.value;
                                if (value <= 5) return "#D0E7FF"; // Lightest blue
                                if (value > 5 && value <= 10) return "#91C7FF"; // Light blue
                                if (value > 10 && value <= 20) return "#5398FF"; // Medium blue
                                if (value > 20 && value <= 30) return "#1A6EFF"; // Dark blue
                                if (value > 30) return "#0044CC"; // Darkest blue
                                return "#D0E7FF"; // Fallback light blue
                            }}
                            tooltip={({ indexValue, value }) => (
                                <strong>
                                    {indexValue}: {value} borrows
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
                                legend: "Borrow Count",
                                legendPosition: "middle",
                                legendOffset: -50,
                            }}
                            labelSkipWidth={12}
                            labelSkipHeight={12}
                            labelTextColor={{
                                from: "color",
                                modifiers: [["darker", 1.6]],
                            }}
                            borderColor={{
                                from: "color",
                                modifiers: [["darker", 0.5]],
                            }}
                        />
                    </div>
                </div>

                {/* Average Borrow Time Per Book*/}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Average Borrow Time Per Book (Days)
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={averageBorrowTime.map((item) => ({
                                ...item,
                                value: Math.round(item.value), // Approximate borrow time
                            }))}
                            keys={["value"]}
                            indexBy="id"
                            margin={{ top: 40, right: 40, bottom: 100, left: 60 }}
                            padding={0.3}
                            colors={{ scheme: "category10" }}
                            tooltip={({ indexValue, value }) => (
                                <strong>
                                    {indexValue}: {value} days
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
                                legend: "Days",
                                legendPosition: "middle",
                                legendOffset: -50,
                            }}
                            labelSkipWidth={12}
                            labelSkipHeight={12}
                            labelTextColor={{
                                from: "color",
                                modifiers: [["darker", 1.6]],
                            }}
                        />
                    </div>
                </div>

                {/* Number of Books Per Author */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Number of Books Per Author
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={booksPerAuthor}
                            keys={["value"]}
                            indexBy="id"
                            margin={{ top: 40, right: 40, bottom: 100, left: 60 }}
                            padding={0.3}
                            colors={(bar) => {
                                const value = bar.value;
                                if (value === 1) return "#edf8fb"; // Lightest blue
                                if (value > 1 && value <= 5) return "#b3cde3"; // Medium blue
                                if (value > 5 && value <= 15) return "#8c96c6"; // Darker blue
                                if (value > 15 && value <= 30) return "#8856a7"; // Purple
                                if (value > 30) return "#4d004b"; // Darkest shade
                                return "#f7f7f7"; // Fallback color
                            }}
                            tooltip={({ indexValue, value }) => (
                                <strong>
                                    {indexValue}: {value} books
                                </strong>
                            )}
                            axisBottom={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: -45,
                                legend: "Authors",
                                legendPosition: "middle",
                                legendOffset: 50,
                            }}
                            axisLeft={{
                                tickSize: 5,
                                tickPadding: 5,
                                tickRotation: 0,
                                legend: "Books Count",
                                legendPosition: "middle",
                                legendOffset: -50,
                            }}
                            labelSkipWidth={12}
                            labelSkipHeight={12}
                            labelTextColor={{
                                from: "color",
                                modifiers: [["darker", 2]],
                            }}
                        />
                    </div>
                </div>

                {/* Books Most Often Returned Late */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Books Most Often Returned Late
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsivePie
                            data={booksWithFees}
                            margin={{ top: 40, right: 80, bottom: 80, left: 80 }}
                            innerRadius={0.5}
                            padAngle={1}
                            cornerRadius={3}
                            colors={{ scheme: "paired" }}
                            tooltip={({ datum }) => (
                                <strong>
                                    {datum.data.label}: {datum.data.value.toFixed(2)}
                                </strong>
                            )}
                        />
                    </div>
                </div>

                {/* Total Revenue by Book */}
                <div className="bg-white shadow-lg rounded-lg p-6">
                    <h2 className="text-2xl font-bold text-center mb-4 text-[#A87C5A]">
                        Total Revenue by Book
                    </h2>
                    <div style={{ height: "400px" }}>
                        <ResponsiveBar
                            data={totalRevenueByBook.map((item) => ({
                                ...item,
                                label: `${item.label}`, // Add $ in label
                            }))}
                            keys={["value"]}
                            indexBy="label"
                            margin={{ top: 40, right: 40, bottom: 100, left: 60 }}
                            padding={0.3}
                            colors={{ scheme: "set3" }}
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
                            labelSkipWidth={12}
                            labelSkipHeight={12}
                            labelTextColor={{
                                from: "color",
                                modifiers: [["darker", 1.6]],
                            }}
                            borderColor={{
                                from: "color",
                                modifiers: [["darker", 0.5]],
                            }}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BooksPage;