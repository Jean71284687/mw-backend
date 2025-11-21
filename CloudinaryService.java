package mweb.mw_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Sube una imagen a Cloudinary y retorna la URL pública
     * 
     * @param file El archivo de imagen a subir
     * @param folder La carpeta donde se guardará (ej: "products", "users", etc.)
     * @return La URL pública de la imagen subida
     * @throws IOException Si hay error en la subida
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        try {
            log.info("Subiendo imagen a Cloudinary. Archivo: {}, Carpeta: {}", 
                    file.getOriginalFilename(), folder);
            
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "quality", "auto",
                    "fetch_format", "auto"
            );
            
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            String imageUrl = uploadResult.get("secure_url").toString();
            
            log.info("Imagen subida exitosamente. URL: {}", imageUrl);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Error al subir imagen a Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Elimina una imagen de Cloudinary usando su public_id
     * 
     * @param publicId El ID público de la imagen en Cloudinary
     * @throws IOException Si hay error en la eliminación
     */
    public void deleteImage(String publicId) throws IOException {
        try {
            log.info("Eliminando imagen de Cloudinary. Public ID: {}", publicId);
            
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            log.info("Imagen eliminada. Resultado: {}", result.get("result"));
            
        } catch (IOException e) {
            log.error("Error al eliminar imagen de Cloudinary: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Extrae el public_id de una URL de Cloudinary
     * 
     * @param imageUrl La URL completa de la imagen en Cloudinary
     * @return El public_id de la imagen
     */
    public String extractPublicId(String imageUrl) {
        try {
            if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
                return null;
            }
            
            // URL ejemplo: https://res.cloudinary.com/cloud-name/image/upload/v1234567890/folder/image.jpg
            String[] parts = imageUrl.split("/");
            String fileName = parts[parts.length - 1];
            String folder = parts[parts.length - 2];
            
            // Remover la extensión del archivo
            String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            
            return folder + "/" + fileNameWithoutExtension;
            
        } catch (Exception e) {
            log.error("Error al extraer public_id de la URL: {}", imageUrl, e);
            return null;
        }
    }
}