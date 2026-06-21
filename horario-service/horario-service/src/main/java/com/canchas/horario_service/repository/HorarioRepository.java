package com.canchas.horario_service.repository;

import com.canchas.horario_service.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    List<Horario> findByIdCancha(Long idCancha);

    List<Horario> findByIdCanchaAndDisponibleTrue(Long idCancha);
}