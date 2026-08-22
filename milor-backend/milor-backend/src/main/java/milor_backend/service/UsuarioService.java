package milor_backend.service;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Usuario;
import milor_backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario guardarUsuario(Usuario usuario) {
        // Guarda la contraseña en texto plano directamente
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getUsername().equalsIgnoreCase("admin") || usuario.getUsername().equalsIgnoreCase("soporte")) {
            throw new RuntimeException("Acción bloqueada: No se pueden eliminar las cuentas principales del sistema.");
        }

        usuarioRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Usuario autenticar(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Compara la contraseña en texto plano directamente
        if (!password.equals(usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return usuario;
    }
}