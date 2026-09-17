package com.gelatinasclarissa.order_service.controller;

import com.gelatinasclarissa.order_service.config.SecurityConfig;
import com.gelatinasclarissa.order_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    void shouldReturnUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(
                        get("/api/admin/orders")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWithoutAdminRole() throws Exception {
        mockMvc.perform(
                        get("/api/admin/orders")
                                .with(user("client").roles("USER"))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToListOrders() throws Exception {
        when(orderService.findAll()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/admin/orders")
                                .with(user("admin").roles("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}