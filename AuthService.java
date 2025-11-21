package mweb.mw_backend.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.auth.AuthResponse;
import mweb.mw_backend.auth.LoginRequest;
import mweb.mw_backend.auth.RegisterRequest;
import mweb.mw_backend.entity.PurchaseCart;
import mweb.mw_backend.entity.User;
import mweb.mw_backend.enumeration.UserRole;
import mweb.mw_backend.jwt.JwtService;
import mweb.mw_backend.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

   private final UserRepository userRepository;
   private final JwtService jwtService;
   private final PasswordEncoder passwordEncoder;
   private final AuthenticationManager authenticationManager;

   public AuthResponse login(LoginRequest request) {
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
      User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
      String token = jwtService.getToken(user);
      return AuthResponse.builder()
            .token(token)
            .build();
   }

   public AuthResponse register(RegisterRequest request) {
      // Verificar si el email ya existe
      if (userRepository.findByEmail(request.getEmail()).isPresent()) {
         throw new RuntimeException("Ya existe una cuenta con este email");
      }
      
      // Validar formato de email
      if (!isValidEmail(request.getEmail())) {
         throw new RuntimeException("El formato del email no es válido");
      }
      
      // Validar celular (9 dígitos)
      if (!isValidCellphone(request.getCel())) {
         throw new RuntimeException("El celular debe tener 9 dígitos numéricos");
      }
      
      User user = User.builder()
            .email(request.getEmail().toLowerCase().trim())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName().trim())
            .lastName(request.getLastName().trim())
            .address(request.getAddress().trim())
            .cel(request.getCel().trim())
            .registerDate(LocalDateTime.now())
            .role(UserRole.CLIENT)
            .build();

      // Crear el carrito de compras automáticamente para el nuevo usuario
      PurchaseCart cart = PurchaseCart.builder()
            .user(user)
            .isActive(true)
            .build();
      
      // Establecer la relación bidireccional
      user.setActiveCart(cart);

      userRepository.save(user);

      return AuthResponse.builder()
            .token(jwtService.getToken(user))
            .build();
   }
   
   // Método auxiliar para validar email
   private boolean isValidEmail(String email) {
      return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
   }
   
   // Método auxiliar para validar celular
   private boolean isValidCellphone(String cel) {
      return cel != null && cel.matches("^[0-9]{9}$");
   }
}