package mweb.mw_backend.service;

import mweb.mw_backend.dto.CategoryDTO;
import mweb.mw_backend.entity.Category;
import mweb.mw_backend.exception.ResourceNotFoundException;
import mweb.mw_backend.mappers.CategoryMapper;
import mweb.mw_backend.repository.CategoryRepository;
import mweb.mw_backend.specification.CategorySpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service para Category con paginación, filtros y operaciones CRUD seguras.
 */
@Service
@Transactional
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    /**
     * Listado paginado y filtrable de categorías.
     */
    @Transactional(readOnly = true)
    public Page<CategoryDTO> findAll(String name, Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        Specification<Category> spec = Specification.where(CategorySpecification.nameContains(name))
                .and(CategorySpecification.isActive(active));
        return repository.findAll(spec, pageable).map(CategoryMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public CategoryDTO findById(Long id) {
        Category cat = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return CategoryMapper.toDTO(cat);
    }

    public CategoryDTO create(CategoryDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        if (repository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Category with that name already exists");
        }
        Category toSave = CategoryMapper.toEntity(dto);
        Category saved = repository.save(toSave);
        return CategoryMapper.toDTO(saved);
    }

    public CategoryDTO update(Long id, CategoryDTO dto) {
        Category existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        // check name uniqueness if changed
        if (dto.getName() != null && !existing.getName().equalsIgnoreCase(dto.getName())
                && repository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("Category with that name already exists");
        }
        if (dto.getName() != null) existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        if (dto.getActive() != null) existing.setActive(dto.getActive());
        Category saved = repository.save(existing);
        return CategoryMapper.toDTO(saved);
    }

    /**
     * Soft delete: marca como inactive (active = false).
     */
    public void softDelete(Long id) {
        Category existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        existing.setActive(false);
        repository.save(existing);
    }

    /**
     * Hard delete: elimina la fila de BD.
     */
    public void hardDelete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Método útil para frontend legacy que necesita la lista completa sin paginar.
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllSimple() {
        return repository.findAll().stream().map(CategoryMapper::toDTO).toList();
    }
    
}
