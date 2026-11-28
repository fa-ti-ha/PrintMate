package com.printmate.document;

import com.printmate.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Override
    @EntityGraph(attributePaths = "uploadedBy")
    Optional<Document> findById(Long id);
    List<Document> findByUploadedByOrderByUploadedAtDesc(User user);
    Optional<Document> findByIdAndUploadedBy(Long id, User user);
}
