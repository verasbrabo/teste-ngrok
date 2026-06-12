package app.controller;

import java.util.List;

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
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.DTO.QuiosqueCreateDTO;
import app.DTO.QuiosqueDTO;
import app.DTO.QuiosqueUpdateDTO;
import app.DTO.QuiosqueNearByResponseDTO;
import app.DTO.QuiosqueViewDTO;
import app.entity.Usuario;
import app.service.QuiosqueService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/quiosques")
//@CrossOrigin("http://localhost")
public class QuiosqueController {
	
	@Autowired
	private QuiosqueService quiosqueService;	
	
	@GetMapping("/nearby")
	public ResponseEntity<List<QuiosqueNearByResponseDTO>> findByDistancia(
			@RequestParam Double latUsuario,
			@RequestParam Double lonUsuario,
			@RequestParam Double raioM) {
	List<QuiosqueNearByResponseDTO> lista = this.quiosqueService.findByDistancia(latUsuario, lonUsuario, raioM);
	return new ResponseEntity<>(lista, HttpStatus.OK);
	}	
	
	@PostMapping
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<QuiosqueCreateDTO> save(
			@AuthenticationPrincipal Usuario usuario,
			@RequestBody @Valid QuiosqueDTO data) {		
			QuiosqueCreateDTO quiosque = this.quiosqueService.save(usuario, data);
			 return ResponseEntity.status(HttpStatus.CREATED).body(quiosque);						
	}

	 @PutMapping("/me")
	 @PreAuthorize("hasAnyRole('GERENTE')")
	 public ResponseEntity<QuiosqueCreateDTO> update(
			 @AuthenticationPrincipal Usuario usuario,
			 @RequestBody QuiosqueUpdateDTO data){
		 QuiosqueCreateDTO quiosque = this.quiosqueService.update(usuario, data);
		 return ResponseEntity.status(HttpStatus.OK).body(quiosque);	
	 }
	  
	 
	 @PatchMapping("/me/status")
	 @PreAuthorize("hasAnyRole('GERENTE')")
	 public ResponseEntity<QuiosqueCreateDTO> updateStatus(
			 @AuthenticationPrincipal Usuario usuario){
		 QuiosqueCreateDTO quiosque = this.quiosqueService.updateStatus(usuario);
		 return ResponseEntity.status(HttpStatus.OK).body(quiosque);	
	 }	 	 
	 
	@GetMapping("/{id}")
	public ResponseEntity<QuiosqueViewDTO> view(
			@PathVariable Long id){
		return  ResponseEntity.status(HttpStatus.OK).body(
				this.quiosqueService.findById(id));
	}
	
	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<QuiosqueViewDTO> viewFuncionario(
			@AuthenticationPrincipal Usuario administrador){
		return  ResponseEntity.status(HttpStatus.OK).body(
				this.quiosqueService.findByFuncionario(administrador));
	}
	
	@DeleteMapping("/me")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<Void> delete(
			 @AuthenticationPrincipal Usuario usuario){
		 this.quiosqueService.delete(usuario);
		 return ResponseEntity.status(HttpStatus.NO_CONTENT).build();	
	}
	
	@PostMapping("/me/imagem")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<String> updateImagem(
			@AuthenticationPrincipal Usuario administrador,
			@RequestParam MultipartFile file){		
		
		String nomeArquivo = this.quiosqueService.uploadImagemQuiosque(administrador, file);
		
		return ResponseEntity.ok(nomeArquivo);
	}
	
	@DeleteMapping("/me/imagem")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<Void> deleteImagem(
			@AuthenticationPrincipal Usuario administrador) {		
		
		this.quiosqueService.deleteImagemQuiosque(administrador);
		
		return ResponseEntity.noContent().build();
	}
	
}
