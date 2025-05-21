package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.DocumentoDTO;
import com.example.academia.DTOs.TareaDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.entidades.TareaEntity;
import com.example.academia.repositorios.AlumnoRepository;
import com.example.academia.repositorios.CursoRepository;
import com.example.academia.repositorios.ProfesorRepository;
import com.example.academia.repositorios.TareaRepository;
import com.example.academia.servicios.TareaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TareaServiceImpl implements TareaService {

    private final TareaRepository tareaRepository;
    private final ProfesorRepository profesorRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;

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
        validarFechas(tarea);
        return tareaRepository.save(tarea);
    }

    private void validarFechas(TareaEntity tarea){
        if (tarea.getFechaPublicacion() != null && tarea.getFechaLimite() != null) {
            if (tarea.getFechaLimite().isBefore(tarea.getFechaPublicacion())) {
                throw new IllegalArgumentException("La fecha límite no puede ser anterior a la fecha de publicación");
            }
        }
    }

    @Override
    @Transactional
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

    @Override
    public Page<TareaEntity> findTareasProfesor(Long profesorId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByProfesorId(profesorId, pageable);
    }

    @Override
    public Page<TareaEntity> findTareasCurso(Long cursoId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findByCursoId(cursoId, pageable);
    }

    @Override
    public Page<TareaEntity> findTareasAlumno(Long alumnoId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return tareaRepository.findTareasForAlumno(alumnoId, pageable);
    }

    @Override
    public List<TareaEntity> findTareasByCursoForAlumno(Long cursoId, Long alumnoId) {
        return tareaRepository.findTareasByCursoForAlumno(cursoId, alumnoId);
    }

    @Override
    @Transactional
    public TareaEntity createTarea(TareaDTO tareaDTO, Long profesorId) {
        ProfesorEntity profesor = profesorRepository.findById(profesorId).orElseThrow(() -> new ValidationException("Profesor no encontrado con ID: " + profesorId));
        CursoEntity curso = cursoRepository.findById(tareaDTO.getCursoId()).orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + tareaDTO.getCursoId()));
        //1º Comprobar si el profesor está en el curso
        boolean profesorEnCurso = curso.getProfesores().stream().anyMatch(profesor1 -> profesor1.getId().equals(profesorId));
        if (!profesorEnCurso) {
            throw new ValidationException("Profesor no encontrado en el curso");
        }

        //Creamos la tarea con los datos del dto demas
        TareaEntity tarea = new TareaEntity();
        tarea.setNombre(tareaDTO.getNombre());
        tarea.setDescripcion(tareaDTO.getDescripcion());
        tarea.setFechaPublicacion(tareaDTO.getFechaPublicacion() != null ?
                tareaDTO.getFechaPublicacion() : LocalDate.now());
        tarea.setFechaLimite(tareaDTO.getFechaLimite());
        tarea.setCurso(curso);
        tarea.setProfesor(profesor);
        tarea.setParaTodosLosAlumnos(tareaDTO.getParaTodosLosAlumnos());

        validarFechas(tarea);

        //Ahora si la tarea no es para todos tenemos que encontrar los alumnos a los que se les asigna la tarea
        if (Boolean.FALSE.equals(tareaDTO.getParaTodosLosAlumnos()) && tareaDTO.getAlumnosIds() != null && !tareaDTO.getAlumnosIds().isEmpty()) {
            Set<AlumnoEntity> alumnosAsignados = new HashSet<>();
            for (Long alumnoId : tareaDTO.getAlumnosIds()) {
                //Verificamos que el alumno existe y está en el curso
                AlumnoEntity alumno = alumnoRepository.findById(alumnoId).orElseThrow(()-> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

                if(!validarAlumnoCurso(alumnoId,curso.getId())) {
                    throw new ValidationException("Alumno no matriculado en el curso");
                }
                alumnosAsignados.add(alumno);
            }
            tarea.setAlumnosAsignados(alumnosAsignados);
        }
        return tareaRepository.save(tarea);
    }


    @Override
    public boolean canProfesorAssignTareaToAlumnoInCurso(Long profesorId, Long alumnoId, Long cursoId) {
        return tareaRepository.canProfesorAssignTareaToAlumnoInCurso(profesorId, alumnoId, cursoId);
    }

    @Override
    @Transactional
    public TareaEntity asignarTareaAAlumno(Long tareaId, Long alumnoId) {
        TareaEntity tarea = tareaRepository.findById(tareaId).orElseThrow(() -> new ValidationException("Tarea no encontrada con ID: " + tareaId));

        AlumnoEntity alumno = alumnoRepository.findById(alumnoId).orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

        if (!validarAlumnoCurso(alumnoId, tarea.getCurso().getId())) {
            throw new ValidationException("El alumno no está matriculado en el curso de esta tarea");
        }

        // Si la tarea ya es para todos, no tiene sentido asignarla a un alumno específico
        if (Boolean.TRUE.equals(tarea.getParaTodosLosAlumnos())) {
            throw new ValidationException("Esta tarea ya está asignada a todos los alumnos del curso");
        }

        // Verificar si ya está asignado
        boolean yaAsignado = tarea.getAlumnosAsignados().stream()
                .anyMatch(a -> a.getId().equals(alumnoId));

        if (yaAsignado) {
            throw new ValidationException("El alumno ya tiene asignada esta tarea");
        }

        // Añadir al alumno a la lista de asignados
        tarea.getAlumnosAsignados().add(alumno);
        return tareaRepository.save(tarea);
    }

    @Override
    @Transactional
    public TareaEntity desasignarTareaDeAlumno(Long tareaId, Long alumnoId) {
       TareaEntity tarea = tareaRepository.findById(tareaId).orElseThrow(()-> new ValidationException("Tarea no encontrado con ID: " + tareaId));
       if(Boolean.TRUE.equals(tarea.getParaTodosLosAlumnos())){
           throw new ValidationException("Tarea asignada para todos los alumnos imposible desasignar");
       }
       //Comprobamos si existe algun alumno con ese id a la tarea
       boolean estaAsignado = tarea.getAlumnosAsignados().stream().anyMatch(alumno -> alumno.getId().equals(alumnoId));
       if(!estaAsignado){
           throw new ValidationException("El alumno no tiene asignada ninguna tarea");
       }
       //si existe actualizamos los Alumnos Asignados, filtramos  por todos los alumnos con un id distinto
        //nos devuelve una lista de alumnos sin el que tiene el id, guardamos y ya tenemos desasignado el alumno de la tarea
       tarea.setAlumnosAsignados(tarea.getAlumnosAsignados().stream()
                                .filter(alumno -> !alumno.getId().equals(alumnoId))
                                .collect(Collectors.toSet()));
       return tareaRepository.save(tarea);
    }

    @Override
    public boolean validarTareaProfesor(Long tareaId, Long profesorId) {
        Optional<TareaEntity> tarea = tareaRepository.findById(tareaId);
        return tarea.isPresent() && tarea.get().getProfesor().getId().equals(profesorId);
    }

    @Override
    public boolean validarAlumnoCurso(Long alumnoId, Long cursoId) {
        Optional<CursoEntity> curso= cursoRepository.findById(cursoId);
        if(curso.isEmpty()){return false;}
        CursoEntity cursoEntity = curso.get();
        //Si algun alumno con id X está en el curso seleccionado devuelve true sino False
        return cursoEntity.getAlumnos().stream().
                                        anyMatch(alumno -> alumno.getId().equals(alumnoId));
    }
}
