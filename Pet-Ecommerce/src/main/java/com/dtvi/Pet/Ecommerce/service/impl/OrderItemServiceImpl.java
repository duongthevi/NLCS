package com.dtvi.Pet.Ecommerce.service.impl;

import com.dtvi.Pet.Ecommerce.dto.OrderItemDto;
import com.dtvi.Pet.Ecommerce.dto.OrderRequest;
import com.dtvi.Pet.Ecommerce.dto.Response;
import com.dtvi.Pet.Ecommerce.entity.Order;
import com.dtvi.Pet.Ecommerce.entity.OrderItem;
import com.dtvi.Pet.Ecommerce.entity.Product;
import com.dtvi.Pet.Ecommerce.entity.User;
import com.dtvi.Pet.Ecommerce.enums.OrderStatus;
import com.dtvi.Pet.Ecommerce.exception.CategoryOperationException;
import com.dtvi.Pet.Ecommerce.exception.NotFoundException;
import com.dtvi.Pet.Ecommerce.mapper.EntityDtoMapper;
import com.dtvi.Pet.Ecommerce.repository.OrderItemRepo;
import com.dtvi.Pet.Ecommerce.repository.OrderRepo;
import com.dtvi.Pet.Ecommerce.repository.ProductRepo;
import com.dtvi.Pet.Ecommerce.service.interf.OrderItemService;
import com.dtvi.Pet.Ecommerce.service.interf.UserService;
import com.dtvi.Pet.Ecommerce.specification.OrderItemSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemServiceImpl implements OrderItemService {


    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ProductRepo productRepo;
    private final UserService userService;
    private final EntityDtoMapper entityDtoMapper;


    @Override
    public Response placeOrder(OrderRequest orderRequest) {

        User user = userService.getLoginUser();
        //map order request items to order entities

        List<OrderItem> orderItems = orderRequest.getItems().stream().map(orderItemRequest -> {
            Product product = productRepo.findById(orderItemRequest.getProductId())
                    .orElseThrow(()-> new NotFoundException("Product Not Found"));

                // Ngăn chặn nếu số lượng tồn kho là 0
                if (product.getQuantity() == 0) {
                        throw new CategoryOperationException("Product " + product.getName() + " Out Of Stock.");
                }

                // Kiểm tra số lượng tồn kho
                if (orderItemRequest.getQuantity() > product.getQuantity()) {
                        throw new CategoryOperationException("The quantity requested exceeds the quantity in stock. Quantity available: " + product.getQuantity());
                }

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setQuantity(orderItemRequest.getQuantity());
                orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()))); //set price according to the quantity
                orderItem.setStatus(OrderStatus.PENDING);
                orderItem.setUser(user);

                 // Giảm số lượng tồn kho
                product.setQuantity(product.getQuantity() - orderItemRequest.getQuantity());
                productRepo.save(product);

                return orderItem;

        }).collect(Collectors.toList());

        //calculate the total price
        BigDecimal totalPrice = orderRequest.getTotalPrice() != null && orderRequest.getTotalPrice().compareTo(BigDecimal.ZERO) > 0
                ? orderRequest.getTotalPrice()
                : orderItems.stream().map(OrderItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        //create order entity
        Order order = new Order();
        order.setOrderItemList(orderItems);
        order.setTotalPrice(totalPrice);

        //set the order reference in each orderitem
        orderItems.forEach(orderItem -> orderItem.setOrder(order));

        orderRepo.save(order);

        return Response.builder()
                .status(200)
                .message("Order was successfully placed")
                .build();

    }

        @Override
        @Transactional
        public Response updateOrderItemStatus(Long orderItemId, String status) {
                // Lấy thông tin OrderItem từ cơ sở dữ liệu
                OrderItem orderItem = orderItemRepo.findById(orderItemId)
                        .orElseThrow(() -> new NotFoundException("Order Item not found"));
        
                // Lấy thông tin sản phẩm liên quan
                Product product = orderItem.getProduct();
                if (product == null) {
                throw new IllegalStateException("Product not found for this order item");
                }
        
                // Lấy trạng thái mới
                OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        
                // Nếu trạng thái mới là CANCELLED, xử lý xóa OrderItem khỏi Order
                if (newStatus == OrderStatus.CANCELLED) {
                // Trả lại số lượng hàng về kho
                product.setQuantity(product.getQuantity() + orderItem.getQuantity());
                productRepo.save(product);
        
                // Cập nhật trạng thái của OrderItem thành CANCELLED
                orderItem.setStatus(newStatus);
                orderItem.setAdminMessage("Order Item has been cancelled by admin");
                orderItemRepo.save(orderItem);
        
                // Lấy thông tin Order liên quan
                Order order = orderItem.getOrder();
                if (order != null) {
                        // Xóa OrderItem khỏi danh sách OrderItemList của Order
                        order.getOrderItemList().remove(orderItem);
        
                        // Nếu Order không còn OrderItem nào, xóa Order
                        if (order.getOrderItemList().isEmpty()) {
                        orderRepo.delete(order);
                        } else {
                        // Cập nhật lại Order nếu vẫn còn OrderItem
                        orderRepo.save(order);
                        }
                }
        
                // Xóa OrderItem khỏi cơ sở dữ liệu
                orderItemRepo.delete(orderItem);
        
                return Response.builder()
                        .status(200)
                        .message("Order Item cancelled and removed successfully")
                        .build();
                }
        
                // Nếu trạng thái mới không phải là CANCELLED, chỉ cập nhật trạng thái
                orderItem.setStatus(newStatus);
                orderItemRepo.save(orderItem);
        
                return Response.builder()
                        .status(200)
                        .message("Order Item status updated successfully")
                        .build();
        }

    @Override
    public Response filterOrderItems(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Long itemId, Pageable pageable) {
        Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasStatus(status))
                .and(OrderItemSpecification.createdBetween(startDate, endDate)
                .and(OrderItemSpecification.hasItemId(itemId)));

        Page<OrderItem> orderItemPage = orderItemRepo.findAll(spec, pageable);

        if (orderItemPage.isEmpty()){
            throw new NotFoundException("No Order Found");
        }
        List<OrderItemDto> orderItemDtos = orderItemPage.getContent().stream()
                .map(entityDtoMapper::mapOrderItemToDtoPlusProductAndUser)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .orderItemList(orderItemDtos)
                .totalPage(orderItemPage.getTotalPages())
                .totalElement(orderItemPage.getTotalElements())
                .build();
    }

        @Override
        @Transactional
        public Response cancelOrderItem(Long orderItemId) {
        // Lấy thông tin OrderItem từ cơ sở dữ liệu
        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order Item not found"));

        // Lấy thông tin sản phẩm liên quan
        Product product = orderItem.getProduct();
        if (product == null) {
                throw new IllegalStateException("Product not found for this order item");
        }

        // Trả lại số lượng hàng về kho
        product.setQuantity(product.getQuantity() + orderItem.getQuantity());
        productRepo.save(product);

        // Cập nhật trạng thái của OrderItem thành CANCELLED
        orderItem.setStatus(OrderStatus.CANCELLED);
        orderItemRepo.save(orderItem);

        // Lấy thông tin Order liên quan
        Order order = orderItem.getOrder();
        if (order != null) {
                // Xóa OrderItem khỏi danh sách OrderItemList của Order
                order.getOrderItemList().remove(orderItem);

                // Nếu Order không còn OrderItem nào, xóa Order
                if (order.getOrderItemList().isEmpty()) {
                orderRepo.delete(order);
                } else {
                // Cập nhật lại Order nếu vẫn còn OrderItem
                orderRepo.save(order);
                }
        }

        return Response.builder()
                .status(200)
                .message("Order Item cancelled and removed successfully")
                .build();
        }

}
