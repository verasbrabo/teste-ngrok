package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.DTO.CupomCreateDTO;
import app.DTO.CupomRequestDTO;
import app.entity.Usuario;
import app.service.CupomService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/quiosques/{id_quiosque}")
public class CupomController {

	@Autowired
	private CupomService cupomService;
	
	@PostMapping("/cupons")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<CupomCreateDTO> save(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_quiosque,
			@RequestBody @Valid CupomRequestDTO data){
	    return ResponseEntity.status(HttpStatus.CREATED).body(this.cupomService.save(administrador, id_quiosque, data));
	}

	/** Lista todos os cupons para a administração do proprietário. */
	@GetMapping("/cupons/gerenciamento")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<List<CupomCreateDTO>> findAllByQuiosque(
			@AuthenticationPrincipal Usuario proprietario,
			@PathVariable Long id_quiosque) {
		return ResponseEntity.ok(cupomService.findAllByQuiosque(proprietario, id_quiosque));
	}
	
	@GetMapping("/cupons")
	public ResponseEntity<List<CupomCreateDTO>> findByQuiosque(
			@PathVariable Long id_quiosque){
			List<CupomCreateDTO> cupons =  this.cupomService.findByQuiosque(id_quiosque);
			return ResponseEntity.ok(cupons);
	}
	
	@PutMapping("/cupons/{id}")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<CupomCreateDTO> update(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_quiosque,
			@PathVariable Long id,
			@RequestBody @Valid CupomRequestDTO data){
		
			return ResponseEntity.ok(this.cupomService.update(administrador, id_quiosque, id, data));
	}
	
	@PatchMapping("/cupons/{id}")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<CupomCreateDTO> updateStatus(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_quiosque,
			@PathVariable Long id){
		
			return ResponseEntity.ok(this.cupomService.updateStatus(administrador, id_quiosque, id));
	}
}
