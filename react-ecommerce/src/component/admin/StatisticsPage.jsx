import React, { useState } from "react";
import ApiService from "../../service/ApiService";
import "../../style/statisticsPage.css";

const MonthlyStatistics = () => {
    const [month, setMonth] = useState(new Date().getMonth() + 1); // Tháng hiện tại
    const [year, setYear] = useState(new Date().getFullYear()); // Năm hiện tại
    const [statistics, setStatistics] = useState(null);
    const [error, setError] = useState(null);

    const fetchStatistics = async () => {
        try {
            const response = await ApiService.getMonthlyStatistics(month, year);
            setStatistics(response);
            // setError(null);
        } catch (err) {
            setError(err.response?.data?.message || "Unable to fetch statistics");
            setStatistics(null);
        }
    };

    return (
        <div className="admin-page">
            <h1>Thống Kê Doanh Thu</h1>
            <div className="filter">
                <label>
                    Month:
                    <input
                        type="number"
                        value={month}
                        onChange={(e) => setMonth(e.target.value)}
                        min="1"
                        max="12"
                    />
                </label>
                <label>
                    Year:
                    <input
                        type="number"
                        value={year}
                        onChange={(e) => setYear(e.target.value)}
                        min="2000"
                        max={new Date().getFullYear()}
                    />
                </label>
                <button onClick={fetchStatistics}>Fetch Statistics</button>
            </div>
            {error && <p className="error-message">{error}</p>}
            {statistics && (
                <div className="statistics-container">
                    <div className="stat-item">
                        <h2>Total Revenue</h2>
                        <p>{statistics.totalRevenue.toFixed(2)}VND</p>
                    </div>
                    <div className="stat-item">
                        <h2>Total Orders</h2>
                        <p>{statistics.totalOrders}</p>
                    </div>
                    <div className="stat-item">
                        <h2>Best Selling Products</h2>
                        <ul>
                            {statistics.bestSellingProducts.map((product) => (
                                <li key={product.id}>
                                    <p>
                                        <strong>{product.productName}</strong> - {product.totalQuantity} sold
                                    </p>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MonthlyStatistics;