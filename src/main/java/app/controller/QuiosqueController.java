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

import app.DTO.AvaliacaoDTO;
import app.DTO.EmpresaOverviewDTO;
import app.DTO.QuiosqueCreateDTO;
import app.DTO.QuiosqueDTO;
import app.DTO.QuiosqueUpdateDTO;
import app.DTO.QuiosqueNearByResponseDTO;
import app.DTO.QuiosqueViewDTO;
import app.entity.Funcionario;
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
			@AuthenticationPrincipal Funcionario usuario,
			@RequestBody @Valid QuiosqueDTO data) {		
			QuiosqueCreateDTO quiosque = this.quiosqueService.save(usuario, data);
			 return ResponseEntity.status(HttpStatus.CREATED).body(quiosque);						
	}

	/**
	 * Retorna o quiosque vinculado ao gerente ou funcionário autenticado.
	 */
	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('GERENTE', 'FUNCIONARIO')")
	public ResponseEntity<QuiosqueViewDTO> viewFuncionario(
			@AuthenticationPrincipal Funcionario usuario) {
		return ResponseEntity.ok(this.quiosqueService.findByFuncionario(usuario));
	}

	 @PutMapping("/{id_quiosque}")
	 @PreAuthorize("hasAnyRole('GERENTE')")
	 public ResponseEntity<QuiosqueCreateDTO> update(
			 @AuthenticationPrincipal Usuario usuario,
			 @PathVariable Long id_quiosque,
			 @RequestBody QuiosqueUpdateDTO data){
		 QuiosqueCreateDTO quiosque = this.quiosqueService.update(usuario, id_quiosque, data);
		 return ResponseEntity.status(HttpStatus.OK).body(quiosque);	
	 }
	  
	 
	 @PatchMapping("/{id_quiosque}/status")
	 @PreAuthorize("hasAnyRole('GERENTE')")
	 public ResponseEntity<QuiosqueCreateDTO> updateStatus(
			 @AuthenticationPrincipal Usuario usuario,
			 @PathVariable Long id_quiosque){
		 QuiosqueCreateDTO quiosque = this.quiosqueService.updateStatus(usuario, id_quiosque);
		 return ResponseEntity.status(HttpStatus.OK).body(quiosque);	
	 }	 	 
	 
	@GetMapping("/{id_quiosque}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<QuiosqueViewDTO> view(
			@PathVariable Long id_quiosque){
		return  ResponseEntity.status(HttpStatus.OK).body(
				this.quiosqueService.findById(id_quiosque));
	}
	
	@GetMapping("/{id_quiosque}/overview")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<EmpresaOverviewDTO> overview(
			@AuthenticationPrincipal Funcionario usuario,
			@PathVariable Long id_quiosque){
		return  ResponseEntity.status(HttpStatus.OK).body(
				this.quiosqueService.overview(usuario, id_quiosque));
	}
	
	@GetMapping("/{id_quiosque}/avaliacoes")
	public ResponseEntity<List<AvaliacaoDTO>> viewAvaliacoes(
			@PathVariable Long id_quiosque){
		
		return  ResponseEntity.status(HttpStatus.OK).body(
				this.quiosqueService.findAvaliacoesById(id_quiosque));
	}
	
	@DeleteMapping("/{id_quiosque}")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<Void> delete(
			 @AuthenticationPrincipal Usuario usuario,
			 @PathVariable Long id_quiosque){
		 this.quiosqueService.delete(usuario, id_quiosque);
		 return ResponseEntity.status(HttpStatus.NO_CONTENT).build();	
	}
	
	@PostMapping("/{id_quiosque}/imagem")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<String> updateImagem(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_quiosque,
			@RequestParam MultipartFile file){		
		
		String nomeArquivo = this.quiosqueService.uploadImagemQuiosque(administrador, id_quiosque, file);
		
		return ResponseEntity.ok(nomeArquivo);
	}
	
	@DeleteMapping("/{id_quiosque}/imagem")
	@PreAuthorize("hasAnyRole('GERENTE')")
	public ResponseEntity<Void> deleteImagem(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_quiosque) {		
		
		this.quiosqueService.deleteImagemQuiosque(administrador, id_quiosque);
		
		return ResponseEntity.noContent().build();
	}
	
}
