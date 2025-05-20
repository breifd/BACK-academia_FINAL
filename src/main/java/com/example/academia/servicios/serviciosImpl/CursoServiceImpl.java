package com.example.academia.servicios.serviciosImpl;

import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.repositorios.AlumnoRepository;
import com.example.academia.repositorios.CursoRepository;
import com.example.academia.repositorios.ProfesorRepository;
import com.example.academia.servicios.CursoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final ProfesorRepository profesorRepository;
    private final AlumnoRepository alumnoRepository;

    // Creamos un metodo para crear el pageable que añadiremos a los metodos del servicio para mostrar los datos en formato pagina
    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection= direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sort == null || sort.isEmpty()) sort= "id";
        // Tener cuidado intercalar el orden de los atributos
        return PageRequest.of(page,size, sortDirection, sort);
    }

    @Override
    public Page<CursoEntity> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return cursoRepository.findAll(pageable);
    }

    @Override
    public Optional<CursoEntity> findById(Long id) {
        return cursoRepository.findById(id);
    }

    @Override
    public List<CursoEntity> findAllLista() {
        return cursoRepository.findAll();
    }

    @Override
    public CursoEntity updateCursoBasicInfo(Long id, CursoEntity cursoNuevo) {
        CursoEntity cursoExistente = cursoRepository.findByIdWithDetails(id).orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + id));
        // Actualizar solo información básica
        cursoExistente.setNombre(cursoNuevo.getNombre());
        cursoExistente.setDescripcion(cursoNuevo.getDescripcion());
        cursoExistente.setNivel(cursoNuevo.getNivel());
        cursoExistente.setPrecio(cursoNuevo.getPrecio());
        // Guardar manteniendo las relaciones existentes
        return cursoRepository.save(cursoExistente);
    }

    @Override
    public Page<CursoEntity> findByNombre(String nombre, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        if(nombre != null || !nombre.isEmpty()) return cursoRepository.findCursosByNombreContainingIgnoreCase(nombre,pageable);
        else return cursoRepository.findAll(pageable);

    }

    @Override
    public Page<CursoEntity> findByNivel(CursoEntity.NivelCurso nivel, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return cursoRepository.findByNivel(nivel, pageable);
    }

    @Override
    public CursoEntity saveCurso(CursoEntity curso) {
        return cursoRepository.save(curso);
    }

    @Override
    public void deleteCurso(Long id) {
        cursoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CursoEntity assignProfesorToCurso(Long cursoId, Long profesorId) {
        CursoEntity curso = cursoRepository.findById(cursoId).orElseThrow(()-> new ValidationException("Curso con id "+cursoId+" no encontrado"));
        ProfesorEntity profesor= profesorRepository.findById(profesorId).orElseThrow(()-> new ValidationException("Profesor con id "+profesorId+" no encontrado"));
        //verificamos que el profesor ya esté asignado a este curso
        if(curso.getProfesores().contains(profesor)){
            throw new ValidationException("Profesor ya existente");
        }
        curso.getProfesores().add(profesor);
        profesor.getCursos().add(curso);
        return cursoRepository.save(curso);
    }

    @Override
    @Transactional
    public CursoEntity removeProfesorFromCurso(Long cursoId, Long profesorId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));

        ProfesorEntity profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ValidationException("Profesor no encontrado con ID: " + profesorId));

        // Verificar si está asignado
        if (!curso.getProfesores().contains(profesor)) {
            throw new ValidationException("El profesor no está asignado a este curso");
        }


        curso.getProfesores().remove(profesor);
        profesor.getCursos().remove(curso);
        return cursoRepository.save(curso);
    }

    @Override
    public Set<ProfesorEntity> getProfesoresByCurso(Long cursoId) {
       CursoEntity curso= cursoRepository.findById(cursoId).orElseThrow(()-> new ValidationException("Curso no encontrado"));
       return curso.getProfesores();
    }

    @Override
    public Page<CursoEntity> findCursosByProfesor(Long profesorId, int page, int size, String sort, String direction) {
       Pageable pageable = createPageable(page, size, sort, direction);
       return cursoRepository.findByProfesoresId(profesorId, pageable);
    }

    @Override
    public CursoEntity enrollAlumnoInCurso(Long cursoId, Long alumnoId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));

        AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

        // Verificar si ya está inscrito
        if (curso.getAlumnos().contains(alumno)) {
            throw new ValidationException("El alumno ya está inscrito en este curso");
        }

        // 🔧 LÓGICA MOVIDA AL SERVICIO (no en la entidad)
        curso.getAlumnos().add(alumno);
        alumno.getCursos().add(curso);
        return cursoRepository.save(curso);
    }

    @Override
    public CursoEntity unenrollAlumnoFromCurso(Long cursoId, Long alumnoId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));

        AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

        // Verificar si está inscrito
        if (!curso.getAlumnos().contains(alumno)) {
            throw new ValidationException("El alumno no está inscrito en este curso");
        }

        // 🔧 LÓGICA MOVIDA AL SERVICIO (no en la entidad)
        curso.getAlumnos().remove(alumno);
        alumno.getCursos().remove(curso);
        return cursoRepository.save(curso);
    }

    @Override
    public Set<AlumnoEntity> getAlumnosByCurso(Long cursoId) {
        CursoEntity curso= cursoRepository.findById(cursoId).orElseThrow(()-> new ValidationException("Curso no encontrado con ID: " + cursoId));
        return curso.getAlumnos();
    }

    @Override
    public Page<CursoEntity> findCursosByAlumno(Long alumnoId, int page, int size, String sort, String direction) {
       Pageable pageable = createPageable(page, size, sort, direction);
       return cursoRepository.findByAlumnosId(alumnoId, pageable);
    }

    @Override
    public Page<CursoEntity> findCursosConPlazasDisponibles(int plazasMinimas, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        int capacidadMaxima=30;
        return cursoRepository.findCursosConPlazasDisponibles(plazasMinimas,capacidadMaxima, pageable);
    }

    @Override
    public CursoEntity getCursoWithDetails(Long cursoId) {
        return cursoRepository.findByIdWithDetails(cursoId).orElseThrow(()-> new ValidationException("Curso no encontrado"));
    }


    //Métodos de utilidad


    public int getNumeroProfesores(CursoEntity curso) {
        return curso.getProfesores() != null ? curso.getProfesores().size() : 0;
    }

    public int getNumeroAlumnos(CursoEntity curso) {
        return curso.getAlumnos() != null ? curso.getAlumnos().size() : 0;
    }

    public boolean isProfesorAssignedToCurso(CursoEntity curso, Long profesorId) {
        return curso.getProfesores() != null &&
                curso.getProfesores().stream()
                        .anyMatch(profesor -> profesor.getId().equals(profesorId));
    }

    public boolean isAlumnoEnrolledInCurso(CursoEntity curso, Long alumnoId) {
        return curso.getAlumnos() != null &&
                curso.getAlumnos().stream()
                        .anyMatch(alumno -> alumno.getId().equals(alumnoId));
    }

    public int getPlazasDisponibles(CursoEntity curso, int maxAlumnos) {
        int numAlumnos = getNumeroAlumnos(curso);
        return Math.max(0, maxAlumnos - numAlumnos);
    }
}
