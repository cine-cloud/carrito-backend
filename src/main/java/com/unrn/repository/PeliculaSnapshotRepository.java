package com.unrn.repository;

import com.unrn.model.PeliculaSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeliculaSnapshotRepository extends JpaRepository<PeliculaSnapshot, Integer> {
}
