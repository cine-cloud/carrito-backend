package com.unrn.services;

import com.unrn.model.Carrito;
import com.unrn.model.CarritoEstado;
import com.unrn.repository.CarritoRepositorio;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unrn.config.RabbitMQConfig;
import com.unrn.dto.PeliculaEventDTO;

@Service
public class CarritoEventListener {

    private final CarritoRepositorio carritoRepo;

    public CarritoEventListener(CarritoRepositorio carritoRepo) {
        this.carritoRepo = carritoRepo;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    @Transactional
    public void recibirEvento(PeliculaEventDTO pelicula) {
        System.out.println("📩 Evento recibido del servicio Película:");
        System.out.println("   - ID: " + pelicula.peliculaId());
        System.out.println("   - Título: " + pelicula.titulo());
        System.out.println("   - Precio: " + pelicula.precio());
        
        // Buscar todos los carritos abiertos que contengan esta película
        var carritosAfectados = carritoRepo.findCarritosAbiertosConPelicula(
            CarritoEstado.ABIERTO, 
            pelicula.peliculaId()
        );
        
        if (carritosAfectados.isEmpty()) {
            System.out.println("   ℹ️ No hay carritos abiertos con esta película");
            return;
        }
        
        // Actualizar el precio en todos los carritos abiertos
        int actualizados = 0;
        for (Carrito carrito : carritosAfectados) {
            carrito.actualizarPrecio(pelicula.peliculaId(), pelicula.precio());
            carritoRepo.save(carrito);
            actualizados++;
        }
        
        System.out.println("   ✅ Precio actualizado en " + actualizados + " carrito(s) abierto(s)");
    }
}