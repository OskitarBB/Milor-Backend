package milor_backend.config;

import lombok.RequiredArgsConstructor;
import milor_backend.entity.Usuario;
import milor_backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        // Si no existe ningún usuario, creamos uno de Soporte y uno Admin por defecto
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(Usuario.builder()
                    .username("soporte")
                    .password("12345")
                    .rol("SOPORTE")
                    .build());

            usuarioRepository.save(Usuario.builder()
                    .username("admin")
                    .password("12345")
                    .rol("ADMIN")
                    .build());

            System.out.println(">>> Usuarios por defecto (Soporte y Admin) creados exitosamente.");
        }
    }
}