package com.musicstore.orders;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ActiveProfiles("test")
class OrdersServiceApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void main_shouldStartApplication() {
		try (var mockedSpringApplication = mockStatic(SpringApplication.class)) {
			mockedSpringApplication.when(() -> SpringApplication.run(OrdersServiceApplication.class, new String[]{}))
					.thenReturn(null);

			OrdersServiceApplication.main(new String[]{});

			mockedSpringApplication.verify(() -> SpringApplication.run(OrdersServiceApplication.class, new String[]{}));
		}
	}

}
