import React, {useEffect, useState} from "react";
import { useParams } from "react-router-dom";
import { useCart } from "../context/CartContext";
import ApiService from "../../service/ApiService";
import '../../style/productDetailsPage.css';


const ProductDetailsPage = () => {

    const {productId} = useParams();
    const {cart, dispatch} = useCart();
    const [product, setProduct] = useState(null);

    useEffect(()=>{
        fetchProduct();
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [productId])

    const fetchProduct = async () => {
        try {
            const response = await ApiService.getProductById(productId);
            setProduct(response.product);
            
        } catch (error) {
            console.log(error.message || error)
        }
    }

    
    const addToCart = () => {
        if (product) {
            dispatch({type: 'ADD_ITEM', payload: product});   
        }
    }

    const incrementItem = () => {
        if(product){
            if (cartItem && cartItem.quantity >= product.quantity) {
                alert(`Only ${product.quantity} items are available in stock.`);
                return;
            }
    
            try {
                dispatch({ type: 'INCREMENT_ITEM', payload: product });
            } catch (error) {
                const errorMessage = error.response?.data?.message || "Unable to increase product quantity";
                alert(errorMessage); // Hiển thị thông báo lỗi
            }
        }
    }

    const decrementItem = () => {
        if (product) {
            const cartItem = cart.find(item => item.id === product.id);
            if (cartItem && cartItem.quantity > 1) {
                dispatch({type: 'DECREMENT_ITEM', payload: product}); 
            }else{
                dispatch({type: 'REMOVE_ITEM', payload: product}); 
            }
            
        }
    }

    if (!product) {
        return <p>Loading product details ...</p>
    }

    const cartItem = cart.find(item => item.id === product.id);

    return(
        <div className="product-detail">
            <img src={product?.imageUrl} alt={product?.name} />
            <h1>{product?.name}</h1>
            <p>{product?.description}</p>
            <span>{product.price.toFixed(2)}VND</span>
            {cartItem ? (
                <div className="quantity-controls">
                    <button onClick={decrementItem}>-</button>
                    <span>{cartItem.quantity}</span>
                    <button onClick={incrementItem}>+</button>
                </div>
            ):(
                product.quantity === 0 ? (
                    <button disabled className="out-of-stock">Out of Stock</button>
                ) : (
                    <button onClick={() => addToCart(product)}>Add To Cart</button>
                )
            )}

        </div>
    )

}

export default ProductDetailsPage;