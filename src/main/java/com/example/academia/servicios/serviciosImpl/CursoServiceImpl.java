package com.example.academia.servicios.serviciosImpl;

import com.example.academia.DTOs.Created.CursoCreateDTO;
import com.example.academia.DTOs.CursoConDetallesDTO;
import com.example.academia.DTOs.Response.AlumnoResponseDTO;
import com.example.academia.DTOs.Response.CursoResponseDTO;
import com.example.academia.DTOs.Response.ProfesorResponseDTO;
import com.example.academia.DTOs.SimpleDTO.CursoSimpleDTO;
import com.example.academia.Exceptions.ValidationException;
import com.example.academia.entidades.AlumnoEntity;
import com.example.academia.entidades.CursoEntity;
import com.example.academia.entidades.ProfesorEntity;
import com.example.academia.mappers.AlumnoMapper;
import com.example.academia.mappers.CursoMapper;
import com.example.academia.mappers.ProfesorMapper;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final ProfesorRepository profesorRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoMapper cursoMapper;
    private final ProfesorMapper profesorMapper;
    private final AlumnoMapper alumnoMapper;

    private Pageable createPageable(int page, int size, String sort, String direction) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (sort == null || sort.isEmpty()) sort = "id";
        return PageRequest.of(page, size, sortDirection, sort);
    }

    @Override
    public Page<CursoResponseDTO> findAll(int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return cursoRepository.findAll(pageable).map(curso->{
            if (curso.getProfesores() != null) {
                curso.setProfesores(new HashSet<>(curso.getProfesores()));
            }
            if (curso.getAlumnos() != null) {
                curso.setAlumnos(new HashSet<>(curso.getAlumnos()));
            }
            return cursoMapper.toCursoResponseDTO(curso);
        });
    }

    @Override
    public Optional<CursoResponseDTO> findById(Long id) {
        return cursoRepository.findById(id).map(cursoMapper::toCursoResponseDTO);
    }

    @Override
    public Optional<CursoConDetallesDTO> findByIdWithDetails(Long id) {
        return cursoRepository.findByIdWithDetails(id).map(cursoMapper::toCursoConDetallesDTO);
    }

    @Override
    public List<CursoSimpleDTO> findAllLista() {
        return cursoRepository.findAll().stream()
                .map(cursoMapper::toCursoSimpleDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CursoResponseDTO> findByNombre(String nombre, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        if (nombre != null && !nombre.trim().isEmpty()) {
            return cursoRepository.findCursosByNombreContainingIgnoreCase(nombre, pageable)
                    .map(cursoMapper::toCursoResponseDTO);
        } else {
            return cursoRepository.findAll(pageable).map(cursoMapper::toCursoResponseDTO);
        }
    }

    @Override
    public Page<CursoResponseDTO> findByNivel(String nivel, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        try {
            CursoEntity.NivelCurso nivelEnum = CursoEntity.NivelCurso.valueOf(nivel);
            return cursoRepository.findByNivel(nivelEnum, pageable).map(cursoMapper::toCursoResponseDTO);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Nivel de curso no válido: " + nivel);
        }
    }

    @Override
    public CursoResponseDTO saveCurso(CursoCreateDTO curso) {
        CursoEntity cursoEntity = cursoMapper.toCursoEntity(curso);
        CursoEntity savedCurso = cursoRepository.save(cursoEntity);
        return cursoMapper.toCursoResponseDTO(savedCurso);
    }

    @Override
    public CursoResponseDTO updateCurso(Long id, CursoCreateDTO curso) {
        Optional<CursoEntity> cursoExistente = cursoRepository.findById(id);
        if (cursoExistente.isEmpty()) {
            throw new ValidationException("Curso no encontrado con ID: " + id);
        }

        CursoEntity cursoEntity = cursoMapper.toCursoEntity(curso);
        cursoEntity.setId(id);
        // Mantener las relaciones existentes
        cursoEntity.setProfesores(cursoExistente.get().getProfesores());
        cursoEntity.setAlumnos(cursoExistente.get().getAlumnos());
        cursoEntity.setTareas(cursoExistente.get().getTareas());

        CursoEntity updatedCurso = cursoRepository.save(cursoEntity);
        return cursoMapper.toCursoResponseDTO(updatedCurso);
    }

    @Override
    public void deleteCurso(Long id) {
        cursoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CursoResponseDTO assignProfesorToCurso(Long cursoId, Long profesorId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));
        ProfesorEntity profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ValidationException("Profesor no encontrado con ID: " + profesorId));

        if (curso.getProfesores().contains(profesor)) {
            throw new ValidationException("El profesor ya está asignado a este curso");
        }

        curso.getProfesores().add(profesor);
        profesor.getCursos().add(curso);
        CursoEntity savedCurso = cursoRepository.save(curso);

        return cursoMapper.toCursoResponseDTO(savedCurso);
    }

    @Override
    @Transactional
    public CursoResponseDTO removeProfesorFromCurso(Long cursoId, Long profesorId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));
        ProfesorEntity profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ValidationException("Profesor no encontrado con ID: " + profesorId));

        if (!curso.getProfesores().contains(profesor)) {
            throw new ValidationException("El profesor no está asignado a este curso");
        }

        curso.getProfesores().remove(profesor);
        profesor.getCursos().remove(curso);
        CursoEntity savedCurso = cursoRepository.save(curso);

        return cursoMapper.toCursoResponseDTO(savedCurso);
    }

    @Override
    public Set<ProfesorResponseDTO> getProfesoresByCurso(Long cursoId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));

        return curso.getProfesores().stream()
                .map(profesorMapper::toProfesorResponseDTO)
                .collect(Collectors.toSet());
    }

    @Override
    public Page<CursoResponseDTO> findCursosByProfesor(Long profesorId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return cursoRepository.findByProfesoresId(profesorId, pageable)
                .map(cursoMapper::toCursoResponseDTO);
    }

    @Override
    @Transactional
    public CursoResponseDTO enrollAlumnoInCurso(Long cursoId, Long alumnoId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));
        AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

        if (curso.getAlumnos().contains(alumno)) {
            throw new ValidationException("El alumno ya está matriculado en este curso");
        }

        curso.getAlumnos().add(alumno);
        alumno.getCursos().add(curso);
        CursoEntity savedCurso = cursoRepository.save(curso);

        return cursoMapper.toCursoResponseDTO(savedCurso);
    }

    @Override
    @Transactional
    public CursoResponseDTO unenrollAlumnoFromCurso(Long cursoId, Long alumnoId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));
        AlumnoEntity alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new ValidationException("Alumno no encontrado con ID: " + alumnoId));

        if (!curso.getAlumnos().contains(alumno)) {
            throw new ValidationException("El alumno no está matriculado en este curso");
        }

        curso.getAlumnos().remove(alumno);
        alumno.getCursos().remove(curso);
        CursoEntity savedCurso = cursoRepository.save(curso);

        return cursoMapper.toCursoResponseDTO(savedCurso);
    }

    @Override
    public Set<AlumnoResponseDTO> getAlumnosByCurso(Long cursoId) {
        CursoEntity curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new ValidationException("Curso no encontrado con ID: " + cursoId));

        return curso.getAlumnos().stream()
                .map(alumnoMapper::toAlumnoResponseDTO)
                .collect(Collectors.toSet());
    }

    @Override
    public Page<CursoResponseDTO> findCursosByAlumno(Long alumnoId, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        return cursoRepository.findByAlumnosId(alumnoId, pageable)
                .map(cursoMapper::toCursoResponseDTO);
    }

    @Override
    public Page<CursoResponseDTO> findCursosConPlazasDisponibles(int plazasMinimas, int page, int size, String sort, String direction) {
        Pageable pageable = createPageable(page, size, sort, direction);
        int capacidadMaxima = 30; // Valor por defecto o configurable
        return cursoRepository.findCursosConPlazasDisponibles(plazasMinimas, capacidadMaxima, pageable)
                .map(cursoMapper::toCursoResponseDTO);
    }
}
