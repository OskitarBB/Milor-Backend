package milor_backend.config;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Usuario;
import milor_backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        if (usuarioRepository.count() == 0) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

            usuarioRepository.save(Usuario.builder()
                    .username("soporte")
                    .password(encoder.encode("12345")) // Cifrado automático
                    .rol("SOPORTE")
                    .build());

            usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .password(encoder.encode("12345")) // Cifrado automático
                    .rol("ADMIN")
                    .build());

            System.out.println(">>> Usuarios por defecto (Soporte y Admin) creados con contraseñas encriptadas.");
        }
    }
}