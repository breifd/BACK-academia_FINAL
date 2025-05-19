package com.example.academia.controller;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.entidades.TareaEntity;
import com.example.academia.servicios.TareaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tareas")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class TareaController {

    private final TareaService tareaService;

    @GetMapping
    public ResponseEntity<Page<TareaEntity>> getAllTareas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TareaEntity> getTareaById(@PathVariable Long id) {
        return tareaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/listar")
    public ResponseEntity<List<TareaEntity>> getAllTareas() {
        return ResponseEntity.ok(tareaService.findAllLista());
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<TareaEntity>> findByNombre(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByNombre(nombre, page, size, sort, direction));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<Page<TareaEntity>> findPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByFechaLimiteDespues(LocalDate.now(), page, size, sort, direction));
    }

    @GetMapping("/vencidas")
    public ResponseEntity<Page<TareaEntity>> findVencidas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(tareaService.findByFechaLimiteAntes(LocalDate.now(), page, size, sort, direction));
    }

    @PostMapping
    public ResponseEntity<TareaEntity> createTarea(@RequestBody TareaEntity tarea) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tareaService.saveTarea(tarea));
    }

    @PostMapping("/{id}/documento")
    public ResponseEntity<TareaEntity> uploadDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            TareaEntity tarea = tareaService.uploadDocumento(id, file);
            return ResponseEntity.ok(tarea);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/documento")
    public ResponseEntity<byte[]> downloadDocumento(@PathVariable Long id) {
        try {
            DocumentoDTO documento = tareaService.downloadDocumento(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(documento.getTipoArchivo())); // indica el tipo de archivo
            headers.setContentDispositionFormData("attachment", documento.getNombreArchivo()); // Fuerza la descarga y le da un nombre al archivo

            return new ResponseEntity<>(documento.getContenido(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TareaEntity> updateTarea(@PathVariable Long id, @RequestBody TareaEntity tarea) {
        return tareaService.findById(id)
                .map(existingTarea -> {
                    // Comprobamos si la tarea tiene Documento, lo movemos a la nueva instancia para no perderlo
                    if (existingTarea.getDocumento() != null && existingTarea.getDocumento().length > 0) {
                        tarea.setDocumento(existingTarea.getDocumento());
                        tarea.setNombreDocumento(existingTarea.getNombreDocumento());
                        tarea.setTipoDocumento(existingTarea.getTipoDocumento());
                    }
                    // Establece el id de la tarea para no crear ninguna nueva y la dewvuelve con la respuesta del servidor
                    tarea.setId(id);
                    return ResponseEntity.ok(tareaService.saveTarea(tarea));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTarea(@PathVariable Long id) {
        return tareaService.findById(id)
                .map(tarea -> {
                    tareaService.deleteTarea(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}