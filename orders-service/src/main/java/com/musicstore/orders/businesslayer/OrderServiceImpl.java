package com.musicstore.orders.businesslayer;


import com.musicstore.orders.dataaccesslayer.Order;
import com.musicstore.orders.dataaccesslayer.OrderIdentifier;
import com.musicstore.orders.dataaccesslayer.OrderRepository;
import com.musicstore.orders.domainclientlayer.customer.CustomerModel;
import com.musicstore.orders.domainclientlayer.customer.CustomersServiceClient;
import com.musicstore.orders.domainclientlayer.musiccatalog.AlbumModel;
import com.musicstore.orders.domainclientlayer.musiccatalog.Status;
import com.musicstore.orders.domainclientlayer.musiccatalog.MusicCatalogServiceClient;
import com.musicstore.orders.domainclientlayer.storelocation.StoreLocationModel;
import com.musicstore.orders.domainclientlayer.storelocation.StoresServiceClient;
import com.musicstore.orders.mappinglayer.OrderRequestMapper;
import com.musicstore.orders.mappinglayer.OrderResponseMapper;

import com.musicstore.orders.presentationlayer.OrderRequestModel;
import com.musicstore.orders.presentationlayer.OrderResponseModel;
import com.musicstore.orders.utils.exceptions.InvalidInputException;
import com.musicstore.orders.utils.exceptions.InvalidOrderPriceException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{
    private final CustomersServiceClient customersServiceClient;
    private final MusicCatalogServiceClient musicCatalogServiceClient;
    private final StoresServiceClient storesServiceClient;
    private final OrderRequestMapper orderRequestMapper;
    private final OrderResponseMapper orderResponseMapper;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(CustomersServiceClient customersServiceClient, MusicCatalogServiceClient musicCatalogServiceClient, StoresServiceClient storesServiceClient, OrderRequestMapper orderRequestMapper, OrderResponseMapper orderResponseMapper, OrderRepository orderRepository) {
        this.customersServiceClient = customersServiceClient;
        this.musicCatalogServiceClient = musicCatalogServiceClient;
        this.storesServiceClient = storesServiceClient;
        this.orderRequestMapper = orderRequestMapper;
        this.orderResponseMapper = orderResponseMapper;
        this.orderRepository = orderRepository;
    }

    @Override
    public List<OrderResponseModel> getAllOrdersByCustomerId(String customerId) {
        List<Order> orders = orderRepository.findAllByCustomerModel_CustomerId(customerId);

        CustomerModel customer = customersServiceClient.getCustomerByCustomerId(customerId);

        orders.forEach(order -> {
            order.setCustomerModel(customer);
            AlbumModel album = musicCatalogServiceClient.getAlbumByAlbumId(
                    order.getAlbumModel().getArtistId(),
                    order.getAlbumModel().getAlbumId());

            if (album.getArtistName() == null || album.getArtistName().isEmpty()) {
                AlbumModel artistOnly =
                        musicCatalogServiceClient.getArtistByArtistId(album.getArtistId());
                album.setArtistName(artistOnly.getArtistName());
            }
            order.setAlbumModel(album);

            StoreLocationModel store = storesServiceClient.getStoreByStoreId(
                    order.getStoreLocationModel().getStoreId());
            order.setStoreLocationModel(store);
        });

        return orderResponseMapper.entityListToResponseModelList(orders);
    }

    @Override
    public OrderResponseModel findOrderBydOrderId(String customerId, String orderId) {
        Order order = orderRepository.findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(
                        customerId, orderId);
        if (order == null) {
            throw new InvalidInputException("Unknown orderId: " + orderId + " for customerId: " + customerId);
        }

        order.setCustomerModel(customersServiceClient.getCustomerByCustomerId(customerId));

        AlbumModel album = musicCatalogServiceClient.getAlbumByAlbumId(
                order.getAlbumModel().getArtistId(),
                order.getAlbumModel().getAlbumId());

        if (album.getArtistName() == null || album.getArtistName().isEmpty()) {
            AlbumModel artistOnly =
                    musicCatalogServiceClient.getArtistByArtistId(album.getArtistId());
            album.setArtistName(artistOnly.getArtistName());
        }
        order.setAlbumModel(album);

        order.setStoreLocationModel(storesServiceClient.getStoreByStoreId(order.getStoreLocationModel().getStoreId()));

        return orderResponseMapper.entityToResponseModel(order);
    }

    @Override
    public OrderResponseModel createOrder(OrderRequestModel orderRequestModel, String customerId) {
        CustomerModel customer = customersServiceClient.getCustomerByCustomerId(customerId);
        if (customer == null) {
            throw new InvalidInputException("Unknown customerId provided: " + customerId);
        }

        AlbumModel album = musicCatalogServiceClient.getAlbumByAlbumId(orderRequestModel.getArtistId(), orderRequestModel.getAlbumId());
        if (album == null) {
            throw new InvalidInputException("No album with id: " + orderRequestModel.getAlbumId() +
                    " for artistId: " + orderRequestModel.getArtistId());
        }
        if (album.getStatus() == Status.UNAVAILABLE) {
            throw new InvalidInputException(
                    "Album “" + album.getAlbumTitle() + "” is unavailable and cannot be ordered");
        }


        StoreLocationModel store = storesServiceClient.getStoreByStoreId(orderRequestModel.getStoreId());
        if (store == null) {
            throw new InvalidInputException("Unknown storeId provided: " + orderRequestModel.getStoreId());
        }

        if (orderRequestModel.getOrderPrice() == null || orderRequestModel.getOrderPrice() <= 0) {
            throw new InvalidOrderPriceException("Order price must be greater than 0: " + orderRequestModel.getOrderPrice());
        }

        if (orderRequestModel.getOrderPrice() == null || orderRequestModel.getOrderPrice() <= 0) {
            throw new InvalidOrderPriceException("Order price must be > 0: " + orderRequestModel.getOrderPrice());
        }

        // enforce invariant
        if (orderRequestModel.getOrderPrice() < 10.0) {
            // Update the album condition in the MusicCatalog service
            musicCatalogServiceClient.patchAlbumConditionTypeByArtistAndAlbumId(
                    orderRequestModel.getArtistId(),
                    orderRequestModel.getAlbumId(),
                    Status.BARGAIN
            );

            // Fetch the updated album details to ensure artistName is populated
            album = musicCatalogServiceClient.getAlbumByAlbumId(
                    orderRequestModel.getArtistId(),
                    orderRequestModel.getAlbumId()
            );
        }
        if (album.getArtistName() == null || album.getArtistName().isEmpty()) {
            AlbumModel artistOnly =
                    musicCatalogServiceClient.getArtistByArtistId(album.getArtistId());
            album.setArtistName(artistOnly.getArtistName());
        }

        Order newOrder = orderRequestMapper
                .requestModelToEntity(orderRequestModel, new OrderIdentifier(), album, customer, store);

        Order saved = orderRepository.save(newOrder);
        return orderResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public OrderResponseModel updateOrder(OrderRequestModel orderRequestModel, String customerId, String orderId) {

        Order existing = orderRepository.findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(
                        customerId, orderId);
        if (existing == null) {
            throw new InvalidInputException(
                    "Unknown orderId: " + orderId + " for customerId: " + customerId);
        }

        if (orderRequestModel.getOrderPrice() == null || orderRequestModel.getOrderPrice() <= 0) {
            throw new InvalidOrderPriceException("Order price must be greater than 0: " + orderRequestModel.getOrderPrice());
        }

        CustomerModel customer = customersServiceClient.getCustomerByCustomerId(customerId);

        AlbumModel album = musicCatalogServiceClient.getAlbumByAlbumId(orderRequestModel.getArtistId(), orderRequestModel.getAlbumId());

        StoreLocationModel store = storesServiceClient.getStoreByStoreId(orderRequestModel.getStoreId());

        if (orderRequestModel.getOrderPrice() == null || orderRequestModel.getOrderPrice() <= 0) {
            throw new InvalidOrderPriceException("Order price must be > 0: " + orderRequestModel.getOrderPrice());
        }

        // enforce invariant
        if (orderRequestModel.getOrderPrice() < 10.0) {
            // Update the album condition in the MusicCatalog service
            musicCatalogServiceClient.patchAlbumConditionTypeByArtistAndAlbumId(
                    orderRequestModel.getArtistId(),
                    orderRequestModel.getAlbumId(),
                    Status.BARGAIN
            );

            // Fetch the updated album details to ensure artistName is populated
            album = musicCatalogServiceClient.getAlbumByAlbumId(
                    orderRequestModel.getArtistId(),
                    orderRequestModel.getAlbumId()
            );
        }
        if (album.getArtistName() == null || album.getArtistName().isEmpty()) {
            AlbumModel artistOnly =
                    musicCatalogServiceClient.getArtistByArtistId(album.getArtistId());
            album.setArtistName(artistOnly.getArtistName());
        }

        Order newOrder = orderRequestMapper
                .requestModelToEntity(orderRequestModel, existing.getOrderIdentifier(), album, customer, store);
        newOrder.setId(existing.getId());
        Order saved = orderRepository.save(newOrder);
        return orderResponseMapper.entityToResponseModel(saved);
    }

    @Override
    public void deleteOrder(String customerId, String orderId) {
        Order existing = orderRepository.findOrderByCustomerModel_CustomerIdAndOrderIdentifier_OrderId(
                        customerId, orderId);
        if (existing == null) {
            throw new InvalidInputException(
                    "Unknown orderId: " + orderId + " for customerId: " + customerId);
        }

        orderRepository.delete(existing);
    }
}
