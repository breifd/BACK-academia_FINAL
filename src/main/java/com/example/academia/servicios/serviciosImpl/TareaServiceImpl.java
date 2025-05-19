package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.entidades.TareaEntity;
import com.example.academia.repositorios.TareaRepository;
import com.example.academia.servicios.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;

    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection=direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        if(sort==null || sort.isEmpty()) sort="id";

        return PageRequest.of(page, size, sortDirection , sort);
    }
    @Override
    public Page<TareaEntity> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findAll(pageable);
    }

    @Override
    public Optional<TareaEntity> findById(Long id) {
        return tareaRepository.findById(id);
    }

    @Override
    public List<TareaEntity> findAllLista() {
        return tareaRepository.findAll();
    }

    @Override
    public Page<TareaEntity> findByNombre(String nombre, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        if (nombre != null && !nombre.trim().isEmpty()) {
            return tareaRepository.findByNombreContainingIgnoreCase(nombre, pageable);
        } else {
            return tareaRepository.findAll(pageable);
        }
    }

    @Override
    public Page<TareaEntity> findByFechaLimiteAntes(LocalDate fecha, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByFechaLimiteBefore(fecha, pageable);
    }

    @Override
    public Page<TareaEntity> findByFechaLimiteDespues(LocalDate fecha, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByFechaLimiteAfter(fecha, pageable);
    }

    @Override
    public TareaEntity saveTarea(TareaEntity tarea) {
        if (tarea.getFechaPublicacion() != null && tarea.getFechaLimite() != null) {
            if (tarea.getFechaLimite().isBefore(tarea.getFechaPublicacion())) {
                throw new IllegalArgumentException("La fecha límite no puede ser anterior a la fecha de publicación");
            }
        }
        return tareaRepository.save(tarea);
    }

    @Override
    public TareaEntity uploadDocumento(Long tareaId, MultipartFile file) throws IOException {
        Optional<TareaEntity> tareaEntity = tareaRepository.findById(tareaId);

        if(tareaEntity.isPresent()) {
            TareaEntity tarea = tareaEntity.get();
            tarea.setDocumento(file.getBytes());
            tarea.setNombreDocumento(file.getOriginalFilename());
            tarea.setTipoDocumento(file.getContentType());

            return tareaRepository.save(tarea);
        }
        else {
        throw new RuntimeException("Tarea no encontrada con ID: " + tareaId);
        }

    }

    @Override
    public DocumentoDTO downloadDocumento(Long tareaId) {
        Optional<TareaEntity> tareaEntity = tareaRepository.findById(tareaId);

        if(tareaEntity.isPresent() && tareaEntity.get().getDocumento().length > 0 && tareaEntity.get().getDocumento()!=null) {
            return new DocumentoDTO(tareaEntity.get().getNombreDocumento(), tareaEntity.get().getTipoDocumento(), tareaEntity.get().getDocumento());
        }
        else{
            throw new RuntimeException("Tarea no encontrada con ID o no existe documento en esa tarea: " + tareaId);
        }
    }

    @Override
    public void deleteTarea(Long id) {
        tareaRepository.deleteById(id);
    }
}
