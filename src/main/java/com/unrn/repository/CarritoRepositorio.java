
package com.unrn.repository;

import com.unrn.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarritoRepositorio extends JpaRepository<Carrito, String> {
  List<Carrito> findByUsuarioId(String usuarioId);
}

