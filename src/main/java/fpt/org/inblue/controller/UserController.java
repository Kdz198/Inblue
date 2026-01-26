package fpt.org.inblue.controller;

import fpt.org.inblue.model.CandidateProfile;
import fpt.org.inblue.model.User;
import fpt.org.inblue.model.dto.UserInfo;
import fpt.org.inblue.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin( "*")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userService.getAll());
    }


    @Operation(summary = "dùng chung cho create và update user, nếu create thì ko có id còn update thì có id gửi kèm trong json data á")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                    mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    encoding = {
                            @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "avatar", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    }
            )
    )
    public ResponseEntity<User> createUser(@RequestPart("data") UserInfo data,
                                           @RequestPart(value = "avatar", required = false) MultipartFile avatar
                                           ) throws IOException {
        User createdUser = userService.createUser(data, avatar);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping(path = "upload-cv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "hàm này để upload cv và parse cv trả về thằng candidate profile",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @Encoding(name = "userId", contentType = "application/json"),
                                    @Encoding(name = "cvFile", contentType = "application/octet-stream")
                            }
                    )
            )
    )
    public ResponseEntity<CandidateProfile> uploadCv(
            @Parameter(
                    name = "userId",
                    schema = @Schema(type = "string", example = "1")
            )
            @RequestPart("userId") int userId,

            @RequestPart(value = "cvFile", required = false) MultipartFile cvFile
    ) throws IOException {
        return ResponseEntity.ok(userService.upCv(userId, cvFile));
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getById(id));
    }

}
