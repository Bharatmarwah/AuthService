package in.bm.AuthService.CONTROLLER;

import in.bm.AuthService.REQUESTDTO.CreateAdminRequestDTO;
import in.bm.AuthService.RESPONSEDTO.CreateAdminResponseDTO;
import in.bm.AuthService.SERVICE.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/admin")
@RequiredArgsConstructor
public class InternalController {

    private final AuthService authService;

    //localhost:8080/internal/admin/create
    @PostMapping("/create")
    public ResponseEntity<CreateAdminResponseDTO> createAdmin(@Valid @RequestBody CreateAdminRequestDTO requestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createAdmin(requestDTO));
    }

    //localhost:8080/internal/admin/delete/{username}
    @DeleteMapping("/delete/{username}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String username){
        authService.deleteAdmin(username);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
