package mweb.mw_backend.service;

import lombok.RequiredArgsConstructor;
import mweb.mw_backend.entity.Product;
import mweb.mw_backend.entity.Review;
import mweb.mw_backend.entity.User;
import mweb.mw_backend.repository.ProductRepository;
import mweb.mw_backend.repository.ReviewRepository;
import mweb.mw_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Review createReview(Long userId, Long productId, Integer rating, String comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Review review = Review.builder()
                .user(user)
                .product(product)
                .rating(rating)
                .comment(comment)
                .build();

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public List<Review> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Double getAverageRatingByProduct(Long productId) {
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
        return avgRating != null ? avgRating : 0.0;
    }

    @Transactional(readOnly = true)
    public Long getReviewCountByProduct(Long productId) {
        return reviewRepository.countReviewsByProductId(productId);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new RuntimeException("No tienes permisos para eliminar esta reseña");
        }
        
        reviewRepository.delete(review);
    }
}