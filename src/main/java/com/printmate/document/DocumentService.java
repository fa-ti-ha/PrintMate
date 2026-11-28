package com.printmate.document;

import com.printmate.user.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    Document uploadPdf(MultipartFile file, User user);
    Document getDocumentById(Long id);
    Resource loadFileAsResource(Long documentId, User requestingUser);
    List<Document> getUserDocuments(User user);
    Document getAuthorizedDocument(Long id, User user);
    Document renameDocument(Long id, String filename, User user);
    void deleteDocument(Long id, User user);
}
