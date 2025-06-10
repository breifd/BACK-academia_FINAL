package com.example.academia.controller;

import com.example.academia.DTOs.CalificacionDTO;
import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.Created.EntregaCreateDTO;
import com.example.academia.DTOs.Response.EntregaResponseDTO;
import com.example.academia.DTOs.Response.UsuarioResponseDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.EntregaEntity;
import com.example.academia.entidades.UsuarioEntity;
import com.example.academia.servicios.EntregaService;
import com.example.academia.servicios.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/entregas")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;
    private final UsuarioService usuarioService;
    private final ObjectMapper  objectMapper;

    @GetMapping
    public ResponseEntity<Page<EntregaResponseDTO>> getAllEntregas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return ResponseEntity.ok(entregaService.findAll(page, size, sort, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntregaResponseDTO> getEntregaById(@PathVariable Long id) {
        return entregaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createEntrega(@RequestBody EntregaCreateDTO entregaDTO) {
        try {
            // ✅ SIMPLE: Solo usar el alumnoId que viene en el DTO
            Long alumnoId = entregaDTO.getAlumnoId();

            if (alumnoId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Se requiere el ID del alumno"));
            }

            EntregaResponseDTO entrega = entregaService.crearEntrega(entregaDTO, alumnoId);
            return ResponseEntity.status(HttpStatus.CREATED).body(entrega);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/documento")
    public ResponseEntity<?> uploadDocumento(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        try {
            // ✅ SOLUCIÓN: Obtener el alumnoId desde la entrega existente
            Optional<EntregaResponseDTO> entregaOpt = entregaService.findById(id);
            if (entregaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entrega no encontrada"));
            }

            EntregaResponseDTO entregaData = entregaOpt.get();
            Long alumnoId = entregaData.getAlumno() != null ? entregaData.getAlumno().getId() : null;

            if (alumnoId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No se puede identificar al alumno de la entrega"));
            }

            System.out.println("🔧 [DEBUG] Subiendo documento para entrega ID: " + id + ", alumnoId: " + alumnoId);

            EntregaResponseDTO entrega = entregaService.uploadDocumento(id, file, alumnoId);
            return ResponseEntity.ok(entrega);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo"));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/{id}/calificar")
    public ResponseEntity<?> calificarEntrega(
            @PathVariable Long id,
            @RequestBody CalificacionDTO calificacionDTO) {

        try {
            // ✅ SOLUCIÓN: Obtener el profesorId de la tarea asociada a la entrega
            Optional<EntregaResponseDTO> entregaOpt = entregaService.findById(id);

            if (entregaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entrega no encontrada con ID: " + id));
            }

            EntregaResponseDTO entregaDTO = entregaOpt.get();
            Long profesorId = entregaService.getProfesorIdFromEntrega(id);

            // ✅ CAMBIO CRÍTICO: Validar que la entrega NO esté ya calificada (para calificar por primera vez)
            if (entregaDTO.getEstado() == EntregaEntity.EstadoEntrega.CALIFICADA) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Esta entrega ya está calificada. Use el endpoint de edición para modificar la calificación."));
            }

            // Validar que la entrega tiene tarea y profesor asociados
            if (entregaDTO.getTarea() == null || profesorId == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "La entrega no tiene una tarea o profesor válido asociado"));
            }

            System.out.println("🎯 [CALIFICAR] Calificando entrega ID: " + id + " por profesor ID: " + profesorId);

            EntregaResponseDTO entrega = entregaService.calificarEntrega(id, calificacionDTO, profesorId);
            return ResponseEntity.ok(entrega);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [CALIFICAR] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }
    @PostMapping("/{id}/calificar-con-documento")
    public ResponseEntity<?> calificarEntregaConDocumento(
            @PathVariable Long id,
            @RequestParam("calificacion") String calificacionJson,
            @RequestParam(value = "documentoProfesor", required = false) MultipartFile documentoProfesor) {

        try {
            // ✅ SOLUCIÓN: Obtener el profesorId de la tarea asociada a la entrega
            Optional<EntregaResponseDTO> entregaOpt = entregaService.findById(id);
            if (entregaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entrega no encontrada con ID: " + id));
            }

            EntregaResponseDTO entregaDTO = entregaOpt.get();
            Long profesorId = entregaService.getProfesorIdFromEntrega(id);

            // ✅ CAMBIO CRÍTICO: Validar que la entrega NO esté ya calificada (para calificar por primera vez)
            if (entregaDTO.getEstado() == EntregaEntity.EstadoEntrega.CALIFICADA) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Esta entrega ya está calificada. Use el endpoint de edición para modificar la calificación."));
            }

            System.out.println("🎯 [CALIFICAR-CON-DOC] Calificando entrega ID: " + id + " por profesor ID: " + profesorId);

            CalificacionDTO calificacionDTO = objectMapper.readValue(calificacionJson, CalificacionDTO.class);
            EntregaResponseDTO entrega = entregaService.calificarEntregaConDocumento(id, calificacionDTO, profesorId, documentoProfesor);

            return ResponseEntity.ok(entrega);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo: " + e.getMessage()));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [CALIFICAR-CON-DOC] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }
    @PutMapping("/{id}/calificacion")
    public ResponseEntity<?> editarCalificacion(
            @PathVariable Long id,
            @RequestBody CalificacionDTO calificacionDTO) {

        try {
            Optional<EntregaResponseDTO> entregaOpt = entregaService.findById(id);
            if (entregaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entrega no encontrada con ID: " + id));
            }

            Long profesorId = entregaService.getProfesorIdFromEntrega(id);

            System.out.println("🎯 [EDITAR-CALIFICACION] Editando calificación entrega ID: " + id + " por profesor ID: " + profesorId);

            EntregaResponseDTO entrega = entregaService.editarCalificacion(id, calificacionDTO, profesorId);
            return ResponseEntity.ok(entrega);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [EDITAR-CALIFICACION] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @PutMapping("/{id}/editar-calificacion")
    public ResponseEntity<?> editarCalificacionConDocumento(
            @PathVariable Long id,
            @RequestParam("calificacion") String calificacionJson,
            @RequestParam(value = "documentoProfesor", required = false) MultipartFile documentoProfesor) {

        try {
            Optional<EntregaResponseDTO> entregaOpt = entregaService.findById(id);
            if (entregaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entrega no encontrada con ID: " + id));
            }

            Long profesorId = entregaService.getProfesorIdFromEntrega(id);

            System.out.println("🎯 [EDITAR-CALIFICACION+DOC] Editando calificación entrega ID: " + id + " por profesor ID: " + profesorId);

            CalificacionDTO calificacionDTO = objectMapper.readValue(calificacionJson, CalificacionDTO.class);
            EntregaResponseDTO entrega = entregaService.editarCalificacionConDocumento(id, calificacionDTO, profesorId, documentoProfesor);

            return ResponseEntity.ok(entrega);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar el archivo: " + e.getMessage()));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [EDITAR-CALIFICACION+DOC] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }


    // Nuevo endpoint para descargar documento del profesor
    @GetMapping("/{id}/documento-profesor")
    public ResponseEntity<?> downloadDocumentoProfesor(@PathVariable Long id) {
        try {
            // ✅ VALIDACIÓN: Verificar que el usuario actual tiene permisos
            // (En modo desarrollo, permitir descarga a cualquiera)

            DocumentoDTO documento = entregaService.downloadDocumentoProfesor(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(documento.getTipoArchivo()));
            headers.setContentDispositionFormData("attachment", documento.getNombreArchivo());

            return new ResponseEntity<>(documento.getContenido(), headers, HttpStatus.OK);

        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [DOWNLOAD-DOC-PROF] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    @DeleteMapping("/{id}/documento-profesor")
    public ResponseEntity<?> eliminarDocumentoProfesor(@PathVariable Long id) {
        try {
            // Obtener la entrega
            Optional<EntregaResponseDTO> entregaOpt = entregaService.findById(id);
            if (entregaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entrega no encontrada con ID: " + id));
            }

            // Verificar permisos del profesor
            Long profesorId = entregaService.getProfesorIdFromEntrega(id);

            System.out.println("🗑️ [ELIMINAR-DOC] Eliminando documento del profesor para entrega ID: " + id);

            // Llamar al service para eliminar el documento
            EntregaResponseDTO entrega = entregaService.eliminarDocumentoProfesor(id, profesorId);

            return ResponseEntity.ok(entrega);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [ELIMINAR-DOC] Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }


    @GetMapping("/{id}/documento")
    public ResponseEntity<?> downloadDocumento(@PathVariable Long id) {
        try {
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            // Permitir descarga a cualquiera

            DocumentoDTO documento = entregaService.downloadDocumento(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(documento.getTipoArchivo()));
            headers.setContentDispositionFormData("attachment", documento.getNombreArchivo());

            return new ResponseEntity<>(documento.getContenido(), headers, HttpStatus.OK);

        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<Page<EntregaResponseDTO>> getEntregasByProfesor(
            @PathVariable Long profesorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            System.out.println("🔍 [CONTROLLER] Buscando entregas del profesor: " + profesorId);

            // ✅ USAR MÉTODO OPTIMIZADO que carga todas las relaciones
            Page<EntregaResponseDTO> entregas = entregaService.findEntregasByProfesor(profesorId, page, size, sort, direction);

            System.out.println("🔍 [CONTROLLER] Entregas encontradas: " + entregas.getContent().size());

            // ✅ DEBUG: Verificar que las relaciones se cargan correctamente
            if (!entregas.getContent().isEmpty()) {
                EntregaResponseDTO primera = entregas.getContent().get(0);
                System.out.println("🔍 [CONTROLLER] Primera entrega - Profesor de tarea: " +
                        (primera.getTarea() != null && primera.getTarea().getProfesor() != null ?
                                primera.getTarea().getProfesor().getId() : "NULL"));
            }

            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            System.err.println("❌ [CONTROLLER] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((Page<EntregaResponseDTO>) Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntrega(@PathVariable Long id) {
        try {
            // ===== MODO DESARROLLO: VALIDACIONES COMENTADAS =====
            entregaService.deleteEntrega(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ===== ENDPOINTS ESPECÍFICOS - MODO DESARROLLO =====

    @GetMapping("/pendientes")
    public ResponseEntity<?> getEntregasPendientesCalificacion(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            // Devolver todas las entregas pendientes sin restricciones
            Page<EntregaResponseDTO> entregas = entregaService.findAll(page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pendientes/count")
    public ResponseEntity<?> countEntregasPendientesCalificacion() {
        try {
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            // Devolver un conteo dummy
            return ResponseEntity.ok(Map.of("pendientes", 0L));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEntrega(
            @PathVariable Long id,
            @RequestBody EntregaCreateDTO entregaUpdateDTO) {
        try {
            // Obtener la entrega existente para validar el alumno
            Optional<EntregaResponseDTO> entregaOpt = entregaService.findById(id);
            if (entregaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Entrega no encontrada"));
            }

            EntregaResponseDTO entregaData = entregaOpt.get();
            Long alumnoId = entregaData.getAlumno() != null ? entregaData.getAlumno().getId() : null;

            if (alumnoId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No se puede identificar al alumno de la entrega"));
            }

            System.out.println("🔧 [DEBUG] Actualizando entrega ID: " + id + ", alumnoId: " + alumnoId);

            EntregaResponseDTO entregaActualizada = entregaService.updateEntrega(id, entregaUpdateDTO, alumnoId);
            return ResponseEntity.ok(entregaActualizada);

        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/alumno")
    public ResponseEntity<?> getEntregasByAlumno(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            // Devolver todas las entregas
            Page<EntregaResponseDTO> entregas = entregaService.findAll(page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/tarea/{tareaId}")
    public ResponseEntity<?> getEntregasByTarea(
            @PathVariable Long tareaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            // ===== MODO DESARROLLO: SIN VALIDACIONES =====
            Page<EntregaResponseDTO> entregas = entregaService.findByTarea(tareaId, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<?> getEntregasByEstado(
            @PathVariable String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        try {
            Page<EntregaResponseDTO> entregas = entregaService.findByEstado(estado, page, size, sort, direction);
            return ResponseEntity.ok(entregas);
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ NUEVO: Endpoint para generar entregas automáticas (para testing o ejecución manual)
    @PostMapping("/generar-entregas-vencidas")
    public ResponseEntity<?> generarEntregasVencidas() {
        try {
            entregaService.generarEntregasAutomaticasPorVencimiento();
            return ResponseEntity.ok(Map.of("message", "Entregas automáticas generadas correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al generar entregas automáticas: " + e.getMessage()));
        }
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}