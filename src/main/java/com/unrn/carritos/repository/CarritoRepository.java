
package com.unrn.carritos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unrn.carritos.model.Carrito;

import java.util.List;

public interface CarritoRepository extends JpaRepository<Carrito, String> {
  List<Carrito> findByUsuarioId(String usuarioId);
}
