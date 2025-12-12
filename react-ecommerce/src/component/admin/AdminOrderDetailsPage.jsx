import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import '../../style/adminOrderDetails.css'
import ApiService from "../../service/ApiService";


const OrderStatus = ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED"];

const AdminOrderDetailsPage = () => {
    const { itemId } = useParams();
    const [orderItems, setOrderItems] = useState([]);
    const [message, setMessage] = useState('');
    const [selectedStatus, setSelectedStatus] = useState({});


    useEffect(() => {
        fetchOrderDetails(itemId);
    }, [itemId]);

    const fetchOrderDetails = async (itemId) => {
        try {
            const response = await ApiService.getOrderItemById(itemId);
            setOrderItems(response.orderItemList)
        } catch (error) {
            // Hiển thị thông báo lỗi từ backend
            const errorMessage = error.response?.data?.message || error.message || 'Unable to update order item status';
            setMessage(errorMessage);
            setTimeout(() => {
                setMessage('');
            }, 3000);
        }
    }

    const handleStatusChange = (orderItemId, newStatus) => {
        setSelectedStatus({ ...selectedStatus, [orderItemId]: newStatus })
    }

    const handleSubmitStatusChange = async (orderItemId) => {
        try {
            // Gửi yêu cầu cập nhật trạng thái đến backend
            const response = await ApiService.updateOrderitemStatus(orderItemId, selectedStatus[orderItemId]);
    
            // Kiểm tra phản hồi từ backend
            if (response.status === 200) {
                alert('Order item status was successfully updated');
            } else {
                alert(response.message || 'Failed to update order item status');
            }
    
            setTimeout(() => {
                setMessage('');
            }, 1000);
        } catch (error) {
            // Hiển thị thông báo lỗi từ backend
            const errorMessage = error.response?.data?.message || error.message || 'Unable to update order item status';
            alert(errorMessage);
            setTimeout(() => {
                setMessage('');
            }, 1000);
        }
    };


    return (
        <div className="order-details-page">
            
            <h2>Order Details</h2>
            {orderItems.length ? (
                orderItems.map((orderItem) => (
                    <div key={orderItem.id} className="order-item-details">
                        <div className="info">
                            <h3>Order Information</h3>
                            <p><strong>Order Item ID:</strong>{orderItem.id}</p>
                            <p><strong>Quantity:</strong>{orderItem.quantity}</p>
                            <p><strong>Total Price:</strong>{orderItem.price}</p>
                            <p><strong>Order Status:</strong>{orderItem.status}</p>
                            <p><strong>date Ordered:</strong>{new Date(orderItem.createdAt).toLocaleDateString()}</p>
                        </div>
                        <div className="info">
                            <h3>User Information</h3>
                            <p><strong>Name:</strong>{orderItem.user.name}</p>
                            <p><strong>Email:</strong>{orderItem.user.email}</p>
                            <p><strong>Phone:</strong>{orderItem.user.phoneNumber}</p>
                            <p><strong>Role:</strong>{orderItem.user.role}</p>

                            <div className="info">
                                <h3>Delivery Address</h3>
                                {orderItem.user.address ? (
                                    <>
                                        <p><strong>Country:</strong> {orderItem.user.address.country}</p>
                                        <p><strong>City:</strong> {orderItem.user.address.city}</p>
                                        <p><strong>District:</strong> {orderItem.user.address.district}</p>
                                        <p><strong>Street:</strong> {orderItem.user.address.street}</p>
                                    </>
                                ) : (
                                    <p>No delivery address available</p>
                                )}
                            </div>
                        </div>
                        <div className="info">
                            <h2>Product Information</h2>
                            <img src={orderItem.product.imageUrl} alt={orderItem.product.name} />
                            <p><strong>Name:</strong>{orderItem.product.name}</p>
                            <p><strong>Description:</strong>{orderItem.product.description}</p>
                            <p><strong>Price:</strong>{orderItem.product.price}</p>
                        </div>
                        <div className="status-change">
                            <h4>Change Status</h4>
                            <select
                                className="status-option"
                                value={selectedStatus[orderItem.id] || orderItem.status}
                                onChange={(e) => handleStatusChange(orderItem.id, e.target.value)}>

                                {OrderStatus.map(status => (
                                    <option key={status} value={status}>{status}</option>
                                ))}
                            </select>
                            <button className="update-status-button" onClick={() => handleSubmitStatusChange(orderItem.id)}>Update Status</button>
                        </div>
                    </div>

                ))
            ) : (
                <p>Loading order details ....</p>
            )}
            {message && <p className="response-message">{message}</p>}
        </div>
    );

}

export default AdminOrderDetailsPage;