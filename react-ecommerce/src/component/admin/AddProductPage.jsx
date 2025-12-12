import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import '../../style/addProduct.css'
import ApiService from "../../service/ApiService";

const AddProductPage = () => {

    const [image, setImage] = useState(null);
    const [categories, setCategories] = useState([]);
    const [categoryId, setCategoryId] = useState('');
    const [name, setName] = useState('');
    const [description, setDescription] = useState('');
    const [message, setMessage] = useState('');
    const [price, setPrice] = useState('');
    const [quantity, setQuantity] = useState('');


    const navigate = useNavigate();

    useEffect(() => {
        ApiService.getAllCategory().then((res) => setCategories(res.categoryList));
    }, [])

    const handleImage = (e) => {
        setImage(e.target.files[0])
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        // Kiểm tra nếu bất kỳ trường nào bị bỏ trống
        if (!image || !categoryId || !name || !description || !price || !quantity) {
            const errorMessage = "Please fill in all fields and upload an image before adding the product.";
            alert(errorMessage);
            setTimeout(() => {
            }, 1000);
            return;
        }
        try {
            const formData = new FormData();
            formData.append('image', image);
            formData.append('categoryId', categoryId);
            formData.append('name', name);
            formData.append('description', description);
            formData.append('price', price);
            formData.append('quantity', quantity);

            const response = await ApiService.addProduct(formData);
            if (response.status === 200) {
                setMessage(response.message)
                setTimeout(() => {
                    setMessage('')
                    navigate('/admin/products')
                }, 1000);
            }

        } catch (error) {
            const errorMessage = (error.response?.data?.message || error.message || 'unable to upload product');
            alert(errorMessage);
            setTimeout(() => {
                setMessage('');
            }, 1000);
        }
    }

    return(
        <div>
            <form onSubmit={handleSubmit} className="product-form">
                <h2>Add Product</h2>
                {message && <div className="message">{message}</div>}
                <input type="file" onChange={handleImage} />
                <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)} >
                    <option value="">Select Category</option>
                    {categories.map((cat)=>(
                        <option value={cat.id} key={cat.id}>{cat.name}</option>
                    ))}
                </select>
                <input type="text" 
                placeholder="Product name"
                value={name}
                onChange={(e)=> setName(e.target.value)} />

                <textarea 
                placeholder="Description"
                value={description}
                onChange={(e)=> setDescription(e.target.value)}/>

                <input type="number" 
                placeholder="price"
                value={price}
                onChange={(e)=> setPrice(e.target.value)} />
                <input type="number"
                placeholder="quantity"
                value={quantity}
                onChange={(e)=> setQuantity(e.target.value)} />

                <button type="submit">Add Product</button>
            </form>
        </div>
    )

}
export default AddProductPage;