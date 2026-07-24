package com.unrn;

import com.unrn.event.CompraEventPublisher;
import com.unrn.services.Externo.ClientePeliculas;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.security.oauth2.jwt.JwtDecoder;

@SpringBootTest(classes = CarritoApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CarritoApplicationTest {

    @MockBean
    private ClientePeliculas clientePeliculas;

    @MockBean
    private CompraEventPublisher compraEventPublisher;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {
    }

}