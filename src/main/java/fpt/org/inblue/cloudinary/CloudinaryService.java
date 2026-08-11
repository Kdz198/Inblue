package fpt.org.inblue.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fpt.org.inblue.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map<String, String> uploadImg(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary
                .uploader()
                .upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "my_app/images", "resource_type", "image", "allowed_formats", new String[] {
                                    "jpg", "jpeg", "png", "gif", "webp"
                                }));
        String public_id = (String) uploadResult.get("public_id");
        String secure_url = uploadResult.get("secure_url").toString();
        Map<String, String> result = new HashMap<String, String>();
        result.put("public_id", public_id);
        result.put("secure_url", secure_url);

        return result;
    }

    public Map<String, String> uploadDocument(MultipartFile file) throws IOException {
        String publicId = "my_app/docs/" + UUID.randomUUID().toString();
        Map uploadResult = cloudinary
                .uploader()
                .upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "public_id",
                                publicId,
                                "resource_type",
                                "auto",
                                "access_mode",
                                "public",
                                "overwrite",
                                true));

        Map<String, String> result = new HashMap<>();
        result.put("public_id", (String) uploadResult.get("public_id"));
        result.put("secure_url", (String) uploadResult.get("secure_url"));
        return result;
    }

    public String uploadAudio(File audioFile){
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    audioFile,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "folder", "interview-tts-audio",
                            "public_id", "tts_" + UUID.randomUUID()
                    )
            );
            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload Cloudinary", e);
        }
    }

    public Map<String, String> uploadDocument(byte[] bytes, String fileName) throws IOException {
        String publicId = "my_app/docs/" + UUID.randomUUID().toString();
        Map uploadResult = cloudinary
                .uploader()
                .upload(
                        bytes,
                        ObjectUtils.asMap(
                                "public_id",
                                publicId,
                                "resource_type",
                                "auto",
                                "access_mode",
                                "public",
                                "overwrite",
                                true,
                                "filename",
                                fileName));

        Map<String, String> result = new HashMap<>();
        result.put("public_id", (String) uploadResult.get("public_id"));
        result.put("secure_url", (String) uploadResult.get("secure_url"));
        return result;
    }

    public Map deleteImage(String publicId) throws IOException {
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
        // có thể xóa cache CDN hoặc ko ( thường thì sẽ bị mất sau 1 thời giannn vài phút - vài giờ
        return result;
    }

    public Map deletePdf(String publicId) throws IOException {
        Map result =
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw", "invalidate", true));
        // có thể xóa cache CDN hoặc ko ( thường thì sẽ bị mất sau 1 thời giannn vài phút - vài giờ
        return result;
    }

    public boolean validate(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        System.out.println(contentType);
        System.out.println(fileName);

        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }

        if (fileName == null || !fileName.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
            return false;
        }
        return true;
    }
}
