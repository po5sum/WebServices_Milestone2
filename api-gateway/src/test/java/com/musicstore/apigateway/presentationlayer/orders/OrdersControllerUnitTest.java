package com.musicstore.apigateway.presentationlayer.orders;

import com.musicstore.apigateway.orders.businesslayer.OrdersService;
import com.musicstore.apigateway.orders.presentationlayer.OrdersController;
import com.musicstore.apigateway.orders.presentationlayer.OrdersRequestModel;
import com.musicstore.apigateway.orders.presentationlayer.OrdersResponseModel;
import com.musicstore.apigateway.utils.exceptions.InvalidInputException;
import com.musicstore.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrdersControllerUnitTest {
    @Autowired
    private OrdersController ordersController;

    @MockitoBean
    private OrdersService ordersService;

    private final String FOUND_ORDER = "05c8ab76-4f75-45c1-b6e2-aa8e914ea08f";
    private final String INVALID_ORDER = "05c8ab76-4f75-45c1-b6e2-aa8e";
    private final String NOT_FOUND_ORDER = "05c8ab76-4f75-45c1-b6e2-aa8e914ea08p";
    private final String FOUND_CUSTOMER = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_CUSTOMER = "c3540a89-cb47-4c96-888e-ff96708db4d7";
    private final String INVALID_CUSTOMER = "c3540a89-cb47-4c96-888e-ff96708d";

    @Test
    void whenNoOrders_thenReturnEmptyList() {
        when(ordersService.getAllOrdersByCustomerId(FOUND_CUSTOMER))
                .thenReturn(List.of());

        ResponseEntity<List<OrdersResponseModel>> response =
                ordersController.getAllOrdersByCustomerId(FOUND_CUSTOMER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(ordersService, times(1)).getAllOrdersByCustomerId(FOUND_CUSTOMER);
    }

    @Test
    void whenInvalidCustomerIdOnGetAll_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Bad id"))
                .when(ordersService).getAllOrdersByCustomerId(INVALID_CUSTOMER);

        assertThrows(InvalidInputException.class,
                () -> ordersController.getAllOrdersByCustomerId(INVALID_CUSTOMER));
        verify(ordersService, never()).getAllOrdersByCustomerId(NOT_FOUND_CUSTOMER);
    }

    @Test
    void whenValidGetOne_thenReturnOrder() {
        OrdersResponseModel dummy = new OrdersResponseModel();
        when(ordersService.findOrderBydOrderId(FOUND_CUSTOMER, FOUND_ORDER))
                .thenReturn(dummy);

        ResponseEntity<OrdersResponseModel> resp =
                ordersController.findOrderBydOrderId(FOUND_CUSTOMER, FOUND_ORDER);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(ordersService, times(1))
                .findOrderBydOrderId(FOUND_CUSTOMER, FOUND_ORDER);
    }

    @Test
    void whenOrderNotFoundOnGetOne_thenThrowInvalidInput() {
        when(ordersService.findOrderBydOrderId(FOUND_CUSTOMER, NOT_FOUND_ORDER))
                .thenThrow(new InvalidInputException("Not found"));

        assertThrows(InvalidInputException.class,
                () -> ordersController.findOrderBydOrderId(FOUND_CUSTOMER, NOT_FOUND_ORDER));
    }

    @Test
    void whenValidCreate_thenReturnCreated() {
        OrdersRequestModel req = OrdersRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderDate("2025-05-12").orderPrice(9.99)
                .paymentMethod(null).build();
        OrdersResponseModel dummy = new OrdersResponseModel();

        when(ordersService.createOrder(req, FOUND_CUSTOMER)).thenReturn(dummy);

        ResponseEntity<OrdersResponseModel> resp =
                ordersController.createOrder(req, FOUND_CUSTOMER);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(ordersService, times(1)).createOrder(req, FOUND_CUSTOMER);
    }

    @Test
    void whenCustomerNotFoundOnCreate_thenThrowInvalidInput() {
        OrdersRequestModel req = OrdersRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderDate("2025-05-12").orderPrice(9.99)
                .paymentMethod(null).build();

        when(ordersService.createOrder(req, NOT_FOUND_CUSTOMER))
                .thenThrow(new InvalidInputException("Unknown customer"));

        assertThrows(InvalidInputException.class,
                () -> ordersController.createOrder(req, NOT_FOUND_CUSTOMER));
    }

    @Test
    void whenValidUpdate_thenReturnOk() {
        OrdersRequestModel req = OrdersRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderDate("2025-05-12").orderPrice(19.99)
                .paymentMethod(null).build();
        OrdersResponseModel dummy = new OrdersResponseModel();

        when(ordersService.updateOrder(req, FOUND_CUSTOMER, FOUND_ORDER))
                .thenReturn(dummy);

        ResponseEntity<OrdersResponseModel> resp =
                ordersController.updateOrder(req, FOUND_CUSTOMER, FOUND_ORDER);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(ordersService, times(1)).updateOrder(req, FOUND_CUSTOMER, FOUND_ORDER);
    }

    @Test
    void whenUpdateUnknown_thenThrowInvalidInput() {
        OrdersRequestModel req = OrdersRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderDate("2025-05-12").orderPrice(19.99)
                .paymentMethod(null).build();

        doThrow(new InvalidInputException("Not found"))
                .when(ordersService).updateOrder(req, FOUND_CUSTOMER, NOT_FOUND_ORDER);

        assertThrows(InvalidInputException.class,
                () -> ordersController.updateOrder(req, FOUND_CUSTOMER, NOT_FOUND_ORDER));
    }

    @Test
    void whenValidDelete_thenReturnNoContent() {
        doNothing().when(ordersService).deleteOrder(FOUND_CUSTOMER, FOUND_ORDER);

        ResponseEntity<Void> resp =
                ordersController.deleteOrder(FOUND_CUSTOMER, FOUND_ORDER);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(ordersService, times(1)).deleteOrder(FOUND_CUSTOMER, FOUND_ORDER);
    }

    @Test
    void whenDeleteUnknown_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Not found"))
                .when(ordersService).deleteOrder(FOUND_CUSTOMER, NOT_FOUND_ORDER);

        assertThrows(InvalidInputException.class,
                () -> ordersController.deleteOrder(FOUND_CUSTOMER, NOT_FOUND_ORDER));
    }
    // ==== updateOrder positive ====

    @Test
    void whenValidUpdateOrder_thenReturnOk() {
        OrdersRequestModel req = OrdersRequestModel.builder()
                .artistId("a1")
                .albumId("al1")
                .storeId("s1")
                .orderDate("2025-05-12")
                .orderPrice(39.99)
                .paymentMethod(null)
                .build();
        OrdersResponseModel dummy = OrdersResponseModel.builder()
                .orderId(FOUND_ORDER)
                .customerId(FOUND_CUSTOMER)
                .orderPrice(39.99)
                .paymentMethod(null)
                .build();

        when(ordersService.updateOrder(req, FOUND_CUSTOMER, FOUND_ORDER))
                .thenReturn(dummy);

        ResponseEntity<OrdersResponseModel> resp =
                ordersController.updateOrder(req, FOUND_CUSTOMER, FOUND_ORDER);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(ordersService, times(1))
                .updateOrder(req, FOUND_CUSTOMER, FOUND_ORDER);
    }

// ==== updateOrder not found ====

    @Test
    void whenUpdateOrderNotFound_thenThrowNotFound() {
        OrdersRequestModel req = OrdersRequestModel.builder()
                .artistId("a1")
                .albumId("al1")
                .storeId("s1")
                .orderDate("2025-05-12")
                .orderPrice(39.99)
                .paymentMethod(null)
                .build();

        doThrow(new NotFoundException("Order not found"))
                .when(ordersService)
                .updateOrder(req, FOUND_CUSTOMER, NOT_FOUND_ORDER);

        assertThrows(NotFoundException.class,
                () -> ordersController.updateOrder(req, FOUND_CUSTOMER, NOT_FOUND_ORDER));
    }

// ==== updateOrder invalid input ====

    @Test
    void whenUpdateOrderInvalid_thenThrowInvalidInput() {
        OrdersRequestModel req = OrdersRequestModel.builder()
                .artistId("a1")
                .albumId("al1")
                .storeId("s1")
                .orderDate("2025-05-12")
                .orderPrice(39.99)
                .paymentMethod(null)
                .build();

        doThrow(new InvalidInputException("Bad IDs"))
                .when(ordersService)
                .updateOrder(req, INVALID_CUSTOMER, INVALID_ORDER);

        assertThrows(InvalidInputException.class,
                () -> ordersController.updateOrder(req, INVALID_CUSTOMER, INVALID_ORDER));
    }

}

