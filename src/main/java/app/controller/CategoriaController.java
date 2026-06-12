package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.DTO.CategoriaCreateDTO;
import app.DTO.CategoriaViewDTO;
import app.entity.Usuario;
import app.service.CategoriaService;
import jakarta.validation.Valid;
//@CrossOrigin("http://localhost")
@RestController
@RequestMapping("/quiosques")
public class CategoriaController {
	@Autowired
	private CategoriaService categoriaService;

	
	@PostMapping("/me/categorias")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<CategoriaViewDTO> save(
			@AuthenticationPrincipal Usuario administrador,
			@RequestBody @Valid CategoriaCreateDTO data){
	    return ResponseEntity.status(HttpStatus.CREATED).body(this.categoriaService.save(administrador, data));
	}
	
	@GetMapping("/{id_quiosque}/categorias")
	public ResponseEntity<List<CategoriaViewDTO>> findByQuiosque(
			@PathVariable Long id_quiosque){
			List<CategoriaViewDTO> categorias =  this.categoriaService.findByQuiosque(id_quiosque);
			return ResponseEntity.ok(categorias);
	}
	
	@PutMapping("/me/categorias/{id}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<CategoriaViewDTO> update(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id,
			@RequestBody @Valid CategoriaCreateDTO data){
		
			return ResponseEntity.ok(this.categoriaService.update(administrador, id, data));
	}
	
	@DeleteMapping("/me/categorias/{id}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id){
			this.categoriaService.deleteById(administrador, id);
			return ResponseEntity.noContent().build();
	}
}
