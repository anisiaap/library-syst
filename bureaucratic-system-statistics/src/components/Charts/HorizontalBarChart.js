import React from "react";
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from "recharts";

const HorizontalBarChart = ({ data, xKey, yKey }) => (
    <ResponsiveContainer width="100%" height={400}>
        <BarChart layout="vertical" data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <XAxis type="number" />
            <YAxis type="category" dataKey={yKey} />
            <Tooltip />
            <Bar dataKey={xKey} fill="#A87C5A" />
        </BarChart>
    </ResponsiveContainer>
);

export default HorizontalBarChart;