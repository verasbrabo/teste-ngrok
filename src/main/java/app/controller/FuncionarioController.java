package app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.DTO.FuncionarioResponseDTO;
import app.DTO.TrocarSenhaDTO;
import app.auth.AuthService;
import app.auth.DTO.RegisterAdminResponseDTO;
import app.auth.DTO.RegisterDTO;
import app.entity.Usuario;
import app.service.FuncionarioService;
import jakarta.validation.Valid;

@RestController
//@CrossOrigin("http://localhost")
@RequestMapping("/quiosques/me/funcionarios")
public class FuncionarioController {

	@Autowired
	private AuthService authService;
	@Autowired
	private FuncionarioService funcionarioService;
	
	@PostMapping	
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<RegisterAdminResponseDTO> save(
			@AuthenticationPrincipal Usuario usuario,
			@RequestBody @Valid RegisterDTO data){		
		RegisterAdminResponseDTO user = authService.registerFuncionario(usuario, data);
		return ResponseEntity.status(HttpStatus.CREATED).body(user);
	}
	
	@GetMapping
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<List<FuncionarioResponseDTO>> getFuncionarios(
			@AuthenticationPrincipal Usuario usuario){		
		List<FuncionarioResponseDTO> user = funcionarioService.getFuncionarios(usuario);
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}
	
	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<FuncionarioResponseDTO> getFuncionario(
			@AuthenticationPrincipal Usuario usuario,
			@PathVariable UUID id){		
		FuncionarioResponseDTO user = funcionarioService.getFuncionario(usuario, id);
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<FuncionarioResponseDTO> putFuncionario(
			@AuthenticationPrincipal Usuario usuario,
			@PathVariable UUID id,
			@RequestBody @Valid RegisterDTO data){		
		FuncionarioResponseDTO user = funcionarioService.putFuncionario(usuario,  id, data);
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}
	
	@PostMapping("/{id}/reset-password")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<String> resetPassword(
			@AuthenticationPrincipal Usuario usuario,
			@PathVariable UUID id){		
		return ResponseEntity.ok(funcionarioService.resetPassword(usuario, id));
	}
	
//	@PatchMapping("/me")
//	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
//	public ResponseEntity<Void> patchSenha(
//			@AuthenticationPrincipal Usuario usuario,
//			@PathVariable Long id_quiosque,
//			@RequestBody TrocarSenhaDTO senha){		
//		funcionarioService.patchFuncionarioSenha(usuario, id_quiosque, senha);
//		return ResponseEntity.noContent().build();
//	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<Void> deleteFuncionario(
			@AuthenticationPrincipal Usuario usuario,
			@PathVariable UUID id){		
		funcionarioService.deleteFuncionario(usuario, id);
		return ResponseEntity.noContent().build();
	}
	
	
	@PostMapping("/{id}/imagem")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<String> updateImagem(
			@AuthenticationPrincipal Usuario usuario,
			@PathVariable UUID id,
			@RequestParam MultipartFile file){		
		
		String nomeArquivo = this.funcionarioService.uploadImagemUsuario(usuario, id, file);
		
		return ResponseEntity.ok(nomeArquivo);
	}
	
	@DeleteMapping("/{id}/imagem")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<Void> deleteImagem(
			@AuthenticationPrincipal Usuario usuario,
			@PathVariable UUID id) {		
		
		this.funcionarioService.deleteImagemUsuario(usuario, id);
		
		return ResponseEntity.noContent().build();
	}
}
