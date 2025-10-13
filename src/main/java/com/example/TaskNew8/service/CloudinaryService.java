package com.example.TaskNew8.service;

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

    public Map<String, Object> uploadImage(MultipartFile file) {
        try {
           
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                    "folder", "user_profiles",
                    "resource_type", "image",
                    "transformation", new com.cloudinary.Transformation()
                        .width(500)
                        .height(500)
                        .crop("fill")
                        .quality("auto")
                )
            );
            
            log.info("Image uploaded successfully to Cloudinary. Public ID: {}", uploadResult.get("public_id"));
            return uploadResult;
            
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new RuntimeException("Failed to upload image: " + e.getMessage());
        }
    }

    public void deleteImage(String publicId) {
        try {
            if (publicId != null && !publicId.isEmpty()) {
                Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted from Cloudinary. Public ID: {}, Result: {}", publicId, result.get("result"));
            }
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary. Public ID: {}", publicId, e);
            throw new RuntimeException("Failed to delete image: " + e.getMessage());
        }
    }

    public boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isFileSizeValid(MultipartFile file, long maxSizeInMB) {
        long maxSizeInBytes = maxSizeInMB * 1024 * 1024;
        return file.getSize() <= maxSizeInBytes;
    }
}