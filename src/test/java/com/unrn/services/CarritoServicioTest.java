package com.unrn.services;

import com.unrn.event.CompraEventPublisher;
import com.unrn.event.dto.CompraEventDTO;
import com.unrn.model.Carrito;
import com.unrn.model.CarritoEstado;
import com.unrn.model.CarritoItem;
import com.unrn.repository.CarritoRepositorio;
import com.unrn.services.Externo.ClientePeliculas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServicioTest {

        @Mock
        private CarritoRepositorio repo;

        @Mock
        private ClientePeliculas clientePeliculas;

        @Mock
        private CompraEventPublisher compraEventPublisher;

        @InjectMocks
        private CarritoServicio carritoServicio;

        void setUp() {
                // Si en el futuro necesitamos crear objetos comunes
                // para varios tests, los inicializaremos aquí.
        }

        @Test
        void CrearUnCarritoConUsuario() {

                // Arrange
                String usuarioId = "evangelina";

                Carrito carritoGuardado = new Carrito();
                carritoGuardado.setUsuarioId(usuarioId);

                when(repo.save(any(Carrito.class)))
                                .thenReturn(carritoGuardado);

                // Act
                Carrito resultado = carritoServicio.crear(usuarioId);

                // Assert
                assertNotNull(resultado);
                assertEquals(usuarioId, resultado.getUsuarioId());

                verify(repo, times(1)).save(any(Carrito.class));
        }

        @Test
        void RetornarElCarritoAbiertoExistente() {

                // Arrange
                String usuarioId = "evangelina";

                Carrito carrito = new Carrito();
                carrito.setUsuarioId(usuarioId);

                when(repo.findByUsuarioIdAndEstado(
                                usuarioId,
                                CarritoEstado.ABIERTO))
                                .thenReturn(Optional.of(carrito));

                // Act
                Carrito resultado = carritoServicio.obtenerCarritoAbierto(usuarioId);

                // Assert
                assertNotNull(resultado);
                assertEquals(usuarioId, resultado.getUsuarioId());

                verify(repo, never()).save(any(Carrito.class));
        }

        @Test
        void CrearUnCarritoCuandoNoExisteUnoAbierto() {

                // Arrange
                String usuarioId = "evangelina";

                when(repo.findByUsuarioIdAndEstado(
                                usuarioId,
                                CarritoEstado.ABIERTO))
                                .thenReturn(Optional.empty());

                Carrito carritoNuevo = new Carrito();
                carritoNuevo.setUsuarioId(usuarioId);

                when(repo.save(any(Carrito.class)))
                                .thenReturn(carritoNuevo);

                // Act
                Carrito resultado = carritoServicio.obtenerCarritoAbierto(usuarioId);

                // Assert
                assertNotNull(resultado);
                assertEquals(usuarioId, resultado.getUsuarioId());

                verify(repo, times(1)).save(any(Carrito.class));
        }

        @Test
        void AgregarUnaPeliculaNuevaAlCarrito() {

                // Arrange
                String carritoId = "carrito-1";
                Integer peliculaId = 1;
                int cantidad = 2;

                Carrito carrito = new Carrito();

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                ClientePeliculas.PeliculaRemota pelicula = new ClientePeliculas.PeliculaRemota(
                                peliculaId,
                                "Titanic",
                                new BigDecimal("2500"),
                                "imagen.jpg");

                when(clientePeliculas.obtenerPorId(peliculaId))
                                .thenReturn(pelicula);

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Carrito resultado = carritoServicio.agregarItem(carritoId, peliculaId, cantidad);

                // Assert
                assertNotNull(resultado);

                assertEquals(1, resultado.getItems().size());

                assertEquals(
                                peliculaId,
                                resultado.getItems().get(0).getPeliculaId());

                assertEquals(
                                cantidad,
                                resultado.getItems().get(0).getCantidad());

                verify(clientePeliculas, times(1))
                                .obtenerPorId(peliculaId);

                verify(repo, times(1))
                                .save(any(Carrito.class));
        }

        @Test
        void noDeberiaAgregarUnaPeliculaSiElCarritoEstaConfirmado() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();
                carrito.setEstado(CarritoEstado.CONFIRMADO);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                // Act & Assert
                IllegalStateException excepcion = assertThrows(
                                IllegalStateException.class,
                                () -> carritoServicio.agregarItem(carritoId, 1, 1));

                assertTrue(excepcion.getMessage().contains("no es editable"));

                verify(clientePeliculas, never()).obtenerPorId(any());

                verify(repo, never()).save(any());
        }

        @Test
        void ActualizarLaCantidadDeUnItem() {

                // Arrange
                String carritoId = "carrito-1";
                Integer peliculaId = 1;

                Carrito carrito = new Carrito();

                CarritoItem item = new CarritoItem();
                item.setPeliculaId(peliculaId);
                item.setTituloSnapshot("Titanic");
                item.setPrecioUnitario(new BigDecimal("2500"));
                item.setCantidad(1);

                carrito.agregarItem(item);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Carrito resultado = carritoServicio.actualizarCantidad(
                                carritoId,
                                peliculaId,
                                5);

                // Assert
                assertEquals(
                                5,
                                resultado.getItems().get(0).getCantidad());

                verify(repo).save(any(Carrito.class));
        }

        @Test
        void noDeberiaActualizarLaCantidadSiElCarritoEstaConfirmado() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();
                carrito.setEstado(CarritoEstado.CONFIRMADO);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                // Act & Assert
                IllegalStateException excepcion = assertThrows(
                                IllegalStateException.class,
                                () -> carritoServicio.actualizarCantidad(
                                                carritoId,
                                                1,
                                                5));

                assertTrue(excepcion.getMessage().contains("no es editable"));

                verify(repo, never()).save(any(Carrito.class));
        }

        @Test
        void deberiaEliminarUnItemDelCarrito() {

                // Arrange
                String carritoId = "carrito-1";
                Integer peliculaId = 1;

                Carrito carrito = new Carrito();

                CarritoItem item = new CarritoItem();
                item.setPeliculaId(peliculaId);
                item.setTituloSnapshot("Titanic");
                item.setPrecioUnitario(new BigDecimal("2500"));
                item.setCantidad(2);

                carrito.agregarItem(item);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Carrito resultado = carritoServicio.eliminarItem(carritoId, peliculaId);

                // Assert
                assertTrue(resultado.getItems().isEmpty());

                verify(repo).save(any(Carrito.class));
        }

        @Test
        void noDeberiaEliminarUnItemSiElCarritoEstaConfirmado() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();
                carrito.setEstado(CarritoEstado.CONFIRMADO);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                // Act & Assert
                IllegalStateException excepcion = assertThrows(
                                IllegalStateException.class,
                                () -> carritoServicio.eliminarItem(
                                                carritoId,
                                                1));

                assertTrue(excepcion.getMessage().contains("no es editable"));

                verify(repo, never()).save(any(Carrito.class));
        }

        @Test
        void RealizarCheckoutCorrectamente() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();

                CarritoItem item = new CarritoItem();
                item.setPeliculaId(1);
                item.setTituloSnapshot("Titanic");
                item.setPrecioUnitario(new BigDecimal("2500"));
                item.setCantidad(2);

                carrito.agregarItem(item);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Carrito resultado = carritoServicio.checkout(carritoId);

                // Assert
                assertEquals(CarritoEstado.CONFIRMADO, resultado.getEstado());

                verify(compraEventPublisher).enviarEvento(any());

                verify(repo).save(any(Carrito.class));
        }

        @Test
        void noDeberiaPermitirCheckoutConCarritoVacio() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                // Act & Assert
                IllegalStateException excepcion = assertThrows(
                                IllegalStateException.class,
                                () -> carritoServicio.checkout(carritoId));

                assertEquals("Carrito vacío", excepcion.getMessage());

                verify(compraEventPublisher, never()).enviarEvento(any());

                verify(repo, never()).save(any(Carrito.class));
        }

        @Test
        void PropagarLaExcepcionSiFallaRabbitMQ() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();

                CarritoItem item = new CarritoItem();
                item.setPeliculaId(1);
                item.setTituloSnapshot("Titanic");
                item.setPrecioUnitario(new BigDecimal("2500"));
                item.setCantidad(2);

                carrito.agregarItem(item);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                doThrow(new RuntimeException("Error RabbitMQ"))
                                .when(compraEventPublisher)
                                .enviarEvento(any());

                // Act & Assert
                RuntimeException excepcion = assertThrows(
                                RuntimeException.class,
                                () -> carritoServicio.checkout(carritoId));

                assertEquals("Error RabbitMQ", excepcion.getMessage());

                verify(repo, never()).save(any(Carrito.class));
        }

        @Test
        void EnviarElEventoConLosDatosCorrectos() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();
                carrito.setUsuarioId("evangelina");

                CarritoItem item = new CarritoItem();
                item.setPeliculaId(1);
                item.setTituloSnapshot("Titanic");
                item.setPrecioUnitario(new BigDecimal("2500"));
                item.setCantidad(2);

                carrito.agregarItem(item);

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                ArgumentCaptor<CompraEventDTO> captor = ArgumentCaptor.forClass(CompraEventDTO.class);

                // Act
                carritoServicio.checkout(carritoId);

                // Assert
                verify(compraEventPublisher)
                                .enviarEvento(captor.capture());

                CompraEventDTO evento = captor.getValue();

                assertEquals(
                                carrito.getUsuarioId(),
                                evento.getUsuarioId());

                assertEquals(
                                carrito.getTotal(),
                                evento.getTotal());

                assertEquals(
                                1,
                                evento.getItems().size());

                assertEquals(
                                item.getPeliculaId(),
                                evento.getItems().get(0).getPeliculaId());

                assertEquals(
                                item.getTituloSnapshot(),
                                evento.getItems().get(0).getTituloSnapshot());

                assertEquals(
                                item.getPrecioUnitario(),
                                evento.getItems().get(0).getPrecioUnitario());

                assertEquals(
                                item.getCantidad(),
                                evento.getItems().get(0).getCantidad());
        }

        @Test
        void AsociarUnUsuarioAlCarrito() {

                // Arrange
                String carritoId = "carrito-1";
                String usuarioId = "evangelina";

                Carrito carrito = new Carrito();

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Carrito resultado = carritoServicio.asociarUsuario(
                                carritoId,
                                usuarioId);

                // Assert
                assertEquals(
                                usuarioId,
                                resultado.getUsuarioId());

                verify(repo).save(any(Carrito.class));
        }

        @Test
        void ObtenerUnCarritoExistente() {

                // Arrange
                String carritoId = "carrito-1";

                Carrito carrito = new Carrito();

                when(repo.findById(carritoId))
                                .thenReturn(Optional.of(carrito));

                // Act
                Carrito resultado = carritoServicio.obtener(carritoId);

                // Assert
                assertNotNull(resultado);

                verify(repo).findById(carritoId);
        }

        @Test
        void LanzarExcepcionCuandoNoExisteElCarrito() {

                // Arrange
                String carritoId = "carrito-inexistente";

                when(repo.findById(carritoId))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(
                                java.util.NoSuchElementException.class,
                                () -> carritoServicio.obtener(carritoId));

                verify(repo).findById(carritoId);
        }

        @Test
        void AsociarElCarritoAnonimoCuandoElUsuarioNoTieneCarrito() {

                // Arrange
                String carritoAnonimoId = "anonimo";
                String usuarioId = "evangelina";

                Carrito carritoAnonimo = new Carrito();

                when(repo.findById(carritoAnonimoId))
                                .thenReturn(Optional.of(carritoAnonimo));

                when(repo.findByUsuarioIdAndEstado(
                                usuarioId,
                                CarritoEstado.ABIERTO))
                                .thenReturn(Optional.empty());

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Carrito resultado = carritoServicio.asociarOFusionar(
                                carritoAnonimoId,
                                usuarioId);

                // Assert
                assertEquals(
                                usuarioId,
                                resultado.getUsuarioId());

                verify(repo).save(any(Carrito.class));
        }

        @Test
        void FusionarLosCarritos() {

                // Arrange
                String carritoAnonimoId = "anonimo";
                String usuarioId = "evangelina";

                Carrito carritoAnonimo = new Carrito();

                CarritoItem item = new CarritoItem();
                item.setPeliculaId(1);
                item.setTituloSnapshot("Titanic");
                item.setPrecioUnitario(new BigDecimal("2500"));
                item.setCantidad(2);

                carritoAnonimo.agregarItem(item);

                Carrito carritoUsuario = new Carrito();

                when(repo.findById(carritoAnonimoId))
                                .thenReturn(Optional.of(carritoAnonimo));

                when(repo.findByUsuarioIdAndEstado(
                                usuarioId,
                                CarritoEstado.ABIERTO))
                                .thenReturn(Optional.of(carritoUsuario));

                when(repo.save(any(Carrito.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Carrito resultado = carritoServicio.asociarOFusionar(
                                carritoAnonimoId,
                                usuarioId);

                // Assert
                assertEquals(
                                1,
                                resultado.getItems().size());

                verify(repo).delete(carritoAnonimo);

                verify(repo).save(any(Carrito.class));
        }
}
