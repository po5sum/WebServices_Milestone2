package com.musicstore.orders.presentationlayer;

import com.musicstore.orders.businesslayer.OrderService;
import com.musicstore.orders.dataaccesslayer.PaymentMethod;
import com.musicstore.orders.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@SpringBootTest
@ActiveProfiles("test")
class OrderControllerUnitTest {

    @Autowired
    OrderController orderController;

    @MockitoBean
    OrderService orderService;

    private final String FOUND_ORDER_ID = "05c8ab76-4f75-45c1-b6e2-aa8e914ea08f";
    private final String NOT_FOUND_ORDER_ID = "05c8ab76-4f75-45c1-b6e2-aa8e914ea08p";
    private final String FOUND_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d8";
    private final String NOT_FOUND_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708db4d7";
    private final String INVALID_CUSTOMER_ID = "c3540a89-cb47-4c96-888e-ff96708d";

    @Test
    void whenNoOrdersExist_thenReturnEmptyList() {
        when(orderService.getAllOrdersByCustomerId(FOUND_CUSTOMER_ID))
                .thenReturn(List.of());

        ResponseEntity<List<OrderResponseModel>> resp =
                orderController.getAllOrdersByCustomerId(FOUND_CUSTOMER_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(orderService, times(1)).getAllOrdersByCustomerId(FOUND_CUSTOMER_ID);
    }

    @Test
    void whenCustomerIdInvalid_thenThrowInvalidInputExceptionOnGetAll() {
        assertThrows(
                InvalidInputException.class,
                () -> orderController.getAllOrdersByCustomerId(INVALID_CUSTOMER_ID)
        );
        verify(orderService, never()).getAllOrdersByCustomerId(any());
    }


    // ==== GET ONE ====

    @Test
    void whenValidCustomerAndOrderId_thenReturnOrder() {
        OrderResponseModel dummy = new OrderResponseModel();
        when(orderService.findOrderBydOrderId(FOUND_CUSTOMER_ID, FOUND_ORDER_ID))
                .thenReturn(dummy);

        ResponseEntity<OrderResponseModel> resp =
                orderController.findOrderBydOrderId(FOUND_CUSTOMER_ID, FOUND_ORDER_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(orderService, times(1))
                .findOrderBydOrderId(FOUND_CUSTOMER_ID, FOUND_ORDER_ID);
    }

    @Test
    void whenOrderIdNotFound_thenThrowInvalidInputExceptionOnGetOne() {
        when(orderService.findOrderBydOrderId(FOUND_CUSTOMER_ID, NOT_FOUND_ORDER_ID))
                .thenThrow(new InvalidInputException("Unknown order"));

        assertThrows(
                InvalidInputException.class,
                () -> orderController.findOrderBydOrderId(FOUND_CUSTOMER_ID, NOT_FOUND_ORDER_ID)
        );
    }


    // ==== POST ====

    @Test
    void whenValidCreateOrder_thenReturnCreated() {
        var req = OrderRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderPrice(10.0).paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
        OrderResponseModel dummy = new OrderResponseModel();

        when(orderService.createOrder(req, FOUND_CUSTOMER_ID))
                .thenReturn(dummy);

        // note: controller method is createOrder(OrderRequestModel, String)
        ResponseEntity<OrderResponseModel> resp =
                orderController.createOrder(req, FOUND_CUSTOMER_ID);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(orderService, times(1))
                .createOrder(req, FOUND_CUSTOMER_ID);
    }

    @Test
    void whenCustomerNotFoundOnCreate_thenThrowInvalidInputException() {
        var req = OrderRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderPrice(10.0).paymentMethod(PaymentMethod.CREDIT_CARD)
                .build();
        when(orderService.createOrder(req, NOT_FOUND_CUSTOMER_ID))
                .thenThrow(new InvalidInputException("Unknown customer"));

        assertThrows(
                InvalidInputException.class,
                () -> orderController.createOrder(req, NOT_FOUND_CUSTOMER_ID)
        );
    }


    // ==== PUT ====

    @Test
    void whenValidUpdateOrder_thenReturnOk() {
        var req = OrderRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderPrice(20.0).paymentMethod(PaymentMethod.CASH)
                .build();
        OrderResponseModel dummy = new OrderResponseModel();

        when(orderService.updateOrder(req, FOUND_CUSTOMER_ID, FOUND_ORDER_ID))
                .thenReturn(dummy);

        ResponseEntity<OrderResponseModel> resp =
                orderController.updateOrder(FOUND_CUSTOMER_ID, FOUND_ORDER_ID, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertSame(dummy, resp.getBody());
        verify(orderService, times(1))
                .updateOrder(req, FOUND_CUSTOMER_ID, FOUND_ORDER_ID);
    }

    @Test
    void whenUnknownOrderOnUpdate_thenThrowInvalidInputException() {
        var req = OrderRequestModel.builder()
                .artistId("a").albumId("b").storeId("c")
                .orderPrice(20.0).paymentMethod(PaymentMethod.CASH)
                .build();
        when(orderService.updateOrder(req, FOUND_CUSTOMER_ID, NOT_FOUND_ORDER_ID))
                .thenThrow(new InvalidInputException("Unknown order"));

        assertThrows(
                InvalidInputException.class,
                () -> orderController.updateOrder(FOUND_CUSTOMER_ID, NOT_FOUND_ORDER_ID, req)
        );
    }


    // ==== DELETE ====

    @Test
    void whenCustomerIdFoundOnDelete_thenReturnOkWithMessage() {
        doNothing().when(orderService)
                .deleteOrder(FOUND_CUSTOMER_ID, FOUND_ORDER_ID);

        // controller returns ResponseEntity<String> with a message
        ResponseEntity<String> resp =
                orderController.deleteOrder(FOUND_CUSTOMER_ID, FOUND_ORDER_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Order deleted successfully", resp.getBody());
        verify(orderService, times(1))
                .deleteOrder(FOUND_CUSTOMER_ID, FOUND_ORDER_ID);
    }

    @Test
    void whenUnknownOrderOnDelete_thenThrowInvalidInputException() {
        doThrow(new InvalidInputException("Unknown order"))
                .when(orderService)
                .deleteOrder(FOUND_CUSTOMER_ID, NOT_FOUND_ORDER_ID);

        assertThrows(
                InvalidInputException.class,
                () -> orderController.deleteOrder(FOUND_CUSTOMER_ID, NOT_FOUND_ORDER_ID)
        );
    }
}
