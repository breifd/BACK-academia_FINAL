package com.example.academia.DTOs.Created;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntregaCreateDTO {
    private Long tareaId;
    private Long alumnoId;
    private String comentarios;

    // No incluimos documento ya que se subirá en una petición separada
    // No incluimos fechaEntrega ya que se asignará automáticamente
    // No incluimos estado ya que se determinará según la fecha límite
    // No incluimos nota ya que la asignará el profesor posteriormente
}