package milor_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import milor_backend.entity.ModalidadConsumo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricasDTO {
    private BigDecimal totalRecaudado;
    private int totalMenusVendidos;
    private int totalLocal;
    private int totalLlevar;
    private int totalConEntrada;
    private int totalSinEntrada;
    private Map<Long, DetallePlatoMetrica> conteoPorPlato;
    private List<OrdenRecienteDTO> ultimasVentas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetallePlatoMetrica {
        private String nombre;
        private int vendidos;
        private String stockRestante;
        private boolean activo;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemOrdenDTO {
        private String platoNombre;
        private String entradaNombre;
        private ModalidadConsumo modalidad;
        private long cantidad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrdenRecienteDTO {
        private Long id;
        private LocalDateTime fechaHora;
        private ModalidadConsumo modalidad;
        private BigDecimal total;
        private List<ItemOrdenDTO> items;
    }
}