package com.canchas.cancha_service.repository;

import com.canchas.cancha_service.model.Cancha;
import com.canchas.cancha_service.model.TipoCancha;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CanchaRepository extends JpaRepository<Cancha, Long> {
    List<Cancha> findByIdPropietario(Long idPropietario);
    List<Cancha> findByTipo(TipoCancha tipo);
}