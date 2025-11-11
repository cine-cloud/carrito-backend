
package com.unrn.repository;

import com.unrn.model.Carrito;
import com.unrn.model.CarritoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CarritoRepositorio extends JpaRepository<Carrito, String> {
  List<Carrito> findByUsuarioId(String usuarioId);
  
  @Query("SELECT DISTINCT c FROM Carrito c JOIN c.items i WHERE c.estado = :estado AND i.peliculaId = :peliculaId")
  List<Carrito> findCarritosAbiertosConPelicula(@Param("estado") CarritoEstado estado, @Param("peliculaId") Integer peliculaId);
}

