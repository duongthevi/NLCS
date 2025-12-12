import React, {useEffect, useState} from "react";
import { useLocation } from "react-router-dom";
import ProductList from "../common/ProductList";
import Pagination from "../common/Pagination";
import ApiService from "../../service/ApiService";
import '../../style/home.css';


const Home = () => {
    const location = useLocation();
    const [products, setProducts] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [error, setError] = useState(null);
    const itemsPerPage = 4;

    const [currentIndex, setCurrentIndex] = useState(0);

    useEffect(()=> {

        const fetchProducts = async () => {
            try{
                let allProducts = [];
                const queryparams = new URLSearchParams(location.search);
                const searchItem = queryparams.get('search')

                if (searchItem) {
                    const response = await ApiService.searchProducts(searchItem);
                    allProducts = response.productList || [];
                }else{
                    const response = await ApiService.getAllProducts();
                    allProducts = response.productList || [];

                }

                setTotalPages(Math.ceil(allProducts.length/itemsPerPage));
                setProducts(allProducts.slice((currentPage -1) * itemsPerPage, currentPage * itemsPerPage));
               
            } catch(error){
                setError(error.response?.data?.message || error.message || 'unable to fetch products')
            }
            
        }

        fetchProducts();
    },[location.search, currentPage])

    // Slider logic
    const showSlide = (index) => {
        const totalSlides = 3; // Update this if you add more slides
        if (index >= totalSlides) {
            setCurrentIndex(0);
        }
        else if (index < 0) {
            setCurrentIndex(totalSlides - 1);
        }
        else {
            setCurrentIndex(index);
        }
    };

    const nextSlide = () => {
        showSlide(currentIndex + 1);
    };

    const prevSlide = () => {
        showSlide(currentIndex - 1);
    };




    return(

        <div className="home">

            {/* Slider Section */}
            <div className="slider">
                <div
                    className="slides"
                    style={{ transform: `translateX(-${currentIndex * 100}%)` }}>
                    <div className="slide">
                        <img
                            src="../logo.jpeg"
                            alt="Logo"
                        />
                        <div className="description">
                            <p>WEBSITE PHỤ KIỆN THÚ CƯNG UY TÍN SỐ 1 VIỆT NAM</p>
                        </div>
                    </div>
                    <div className="slide">
                        <img src="https://cdn.tgdd.vn/Files/2021/04/21/1345056/nhung-mau-quan-ao-cho-cho-meo-dep-va-de-thuong-202104211102278985.jpg" alt="Sản phẩm 2" />
                    </div>
                    <div className="slide">
                        <img src="https://cdn.tgdd.vn/Files/2021/04/19/1344714/co-nen-cho-cho-meo-mac-quan-ao-hay-khong-202201141348463688.jpg" alt="Sản phẩm 3" />
                    </div>
                </div>
                <div className="controls">
                    <button onClick={prevSlide}>&#10094;</button>
                    <button onClick={nextSlide}>&#10095;</button>
                </div>
            </div>
            {/* test slide */}

            {error ? (
                <p className="error-message">{error}</p>
            ) : products.length === 0 ? (
                <p className="error-message">No products found for your search.</p>
            ):(
                <div>
                    <ProductList products={products}/>
                    <Pagination  currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={(page)=> setCurrentPage(page)}/>
                </div>
            )}
        </div>
    )


}

export default Home;