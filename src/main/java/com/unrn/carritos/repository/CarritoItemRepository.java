package com.unrn.carritos.repository;

import com.unrn.carritos.model.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, String> {
  
  @Query("SELECT ci FROM CarritoItem ci JOIN FETCH ci.carrito WHERE ci.peliculaId = :peliculaId")
  List<CarritoItem> findByPeliculaId(@Param("peliculaId") Integer peliculaId);
}

