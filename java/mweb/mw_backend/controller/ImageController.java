package mweb.mw_backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mweb.mw_backend.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final CloudinaryService cloudinaryService;

    /**
     * Endpoint para subir una imagen a Cloudinary
     * 
     * @param file El archivo de imagen
     * @param folder La carpeta donde guardar (opcional, default: "general")
     * @return JSON con la URL de la imagen subida
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        
        try {
            // Validar que el archivo no esté vacío
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El archivo está vacío");
                return ResponseEntity.badRequest().body(error);
            }

            // Validar que sea una imagen
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El archivo debe ser una imagen");
                return ResponseEntity.badRequest().body(error);
            }

            // Subir la imagen
            String imageUrl = cloudinaryService.uploadImage(file, folder);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("message", "Imagen subida exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error al subir imagen", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al subir la imagen: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Endpoint para eliminar una imagen de Cloudinary
     * 
     * @param imageUrl La URL de la imagen a eliminar
     * @return Mensaje de confirmación
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteImage(@RequestParam String imageUrl) {
        try {
            String publicId = cloudinaryService.extractPublicId(imageUrl);
            
            if (publicId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "URL de imagen inválida");
                return ResponseEntity.badRequest().body(error);
            }
            
            cloudinaryService.deleteImage(publicId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Imagen eliminada exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            log.error("Error al eliminar imagen", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al eliminar la imagen: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}