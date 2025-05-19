package com.example.academia.servicios;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.entidades.TareaEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TareaService {


    Page<TareaEntity> findAll(int page, int size, String sort, String direction);

    Optional<TareaEntity> findById(Long id);

    List<TareaEntity> findAllLista();

    Page<TareaEntity> findByNombre(String nombre, int page, int size, String sort, String direction);

    Page<TareaEntity> findByFechaLimiteAntes(LocalDate fecha, int page, int size, String sort, String direction);

    Page<TareaEntity> findByFechaLimiteDespues(LocalDate fecha, int page, int size, String sort, String direction);

    TareaEntity saveTarea(TareaEntity tarea);

    TareaEntity uploadDocumento(Long tareaId, MultipartFile file) throws IOException; // Manera de Cargar un  archivo como MultipartFile

    DocumentoDTO downloadDocumento(Long tareaId);

    void deleteTarea(Long id);
}