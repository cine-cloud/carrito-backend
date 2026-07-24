package com.unrn.repository;

import com.unrn.model.Carrito;
import com.unrn.model.CarritoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface CarritoRepositorio extends JpaRepository<Carrito, String> {

    List<Carrito> findByEstado(CarritoEstado estado);

    Optional<Carrito> findByUsuarioIdAndEstado(
        String usuarioId,
        CarritoEstado estado
    );
}

