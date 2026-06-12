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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.DTO.AcompanhamentoCreateDTO;
import app.DTO.AcompanhamentoViewDTO;
import app.entity.Usuario;
import app.service.AcompanhamentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/quiosques/me/acompanhamentos")
public class AcompanhamentoController {
	@Autowired
	private AcompanhamentoService acompanhamentoService;
	
	@PostMapping
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<AcompanhamentoViewDTO> save(
			@AuthenticationPrincipal Usuario administrador,
			@RequestBody @Valid AcompanhamentoCreateDTO data){
	    return ResponseEntity.status(HttpStatus.CREATED).body(this.acompanhamentoService.save(administrador, data));
	}
	
	@GetMapping
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<List<AcompanhamentoViewDTO>> findByQuiosque(
			@AuthenticationPrincipal Usuario administrador){
			List<AcompanhamentoViewDTO> acompanhamento =  this.acompanhamentoService.findByQuiosque(administrador);
			return ResponseEntity.ok(acompanhamento);
	}
	
	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<AcompanhamentoViewDTO> update(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id,
			@RequestBody @Valid AcompanhamentoCreateDTO data){
		
			return ResponseEntity.ok(this.acompanhamentoService.update(administrador, id, data));
	}
	
	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id){
			this.acompanhamentoService.deleteById(administrador, id);
			return ResponseEntity.noContent().build();
	}
}
