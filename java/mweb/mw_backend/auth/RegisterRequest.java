package mweb.mw_backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    String name;
    
    @NotBlank(message = "Los apellidos son requeridos")
    @Size(min = 2, max = 50, message = "Los apellidos deben tener entre 2 y 50 caracteres")
    String lastName;
    
    @NotBlank(message = "La dirección es requerida")
    @Size(min = 5, max = 200, message = "La dirección debe tener entre 5 y 200 caracteres")
    String address;
    
    @NotBlank(message = "El celular es requerido")
    @Pattern(regexp = "^[0-9]{9,15}$", message = "El celular debe contener entre 9 y 15 dígitos")
    String cel;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El formato del email no es válido")
    String email;
    
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 100, message = "La contraseña debe tener al menos 8 caracteres")
    String password;

    //Falta agregar posiblemente country,city,..
}
