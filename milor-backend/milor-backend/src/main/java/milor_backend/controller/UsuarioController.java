package milor_backend.controller;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Usuario;
import milor_backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            Usuario u = usuarioService.autenticar(credentials.get("username"), credentials.get("password"));
            return ResponseEntity.ok(u);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestHeader(value = "X-User-Role", required = false) String rol) {
        validarRolAdminOrSoporte(rol);
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestHeader(value = "X-User-Role", required = false) String rol, @RequestBody Usuario usuario) {
        validarRolAdminOrSoporte(rol);
        return ResponseEntity.ok(usuarioService.guardarUsuario(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@RequestHeader(value = "X-User-Role", required = false) String rol, @PathVariable Long id) {
        validarRolAdminOrSoporte(rol);
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    private void validarRolAdminOrSoporte(String rol) {
        if (rol == null || (!rol.equalsIgnoreCase("ADMIN") && !rol.equalsIgnoreCase("SOPORTE"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acceso denegado: Se requiere rol de Administrador o Soporte.");
        }
    }
}