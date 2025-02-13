package com.example.transactionalms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String idEntidad;
    private String fecha;
    private String mensaje;
    private String recurso;
    private Boolean estado;
}
