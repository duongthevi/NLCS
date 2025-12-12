package com.dtvi.Pet.Ecommerce.service.impl;

import com.dtvi.Pet.Ecommerce.dto.BestSellingProductDto;
import com.dtvi.Pet.Ecommerce.dto.Response;
import com.dtvi.Pet.Ecommerce.mapper.EntityDtoMapper;
import com.dtvi.Pet.Ecommerce.repository.OrderItemRepo;
import com.dtvi.Pet.Ecommerce.service.interf.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final OrderItemRepo orderItemRepo;
    private final EntityDtoMapper entityDtoMapper;

    @Override
    public Response getMonthlyStatistics(int month, int year) {
        // Tổng doanh thu theo tháng
        Double totalRevenue = orderItemRepo.calculateMonthlyRevenue(month, year);

        // Tổng số lượng đơn hàng theo tháng
        Long totalOrders = orderItemRepo.countMonthlyOrders(month, year);

        // Sản phẩm bán chạy theo tháng
        List<Object[]> bestSellingProducts = orderItemRepo.findBestSellingProducts(month, year);
        // Chuyển đổi danh sách sản phẩm bán chạy thành danh sách DTO
        List<BestSellingProductDto> productList = bestSellingProducts.stream()
                .map(entityDtoMapper::mapBestSellingProduct)
                .collect(Collectors.toList());

        // Tạo đối tượng Response
        return Response.builder()
                .status(200)
                .message("Statistics in " + month + "/" + year + " Successfully")
                .totalRevenue(totalRevenue != null ? totalRevenue : 0)
                .totalOrders(totalOrders != null ? totalOrders : 0)
                .bestSellingProducts(productList)
                .build();
    }
}