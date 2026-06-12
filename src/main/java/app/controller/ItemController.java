package app.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import app.DTO.ItemCreateRequest;
import app.DTO.ItemDTO;
import app.entity.Usuario;
import app.service.ImageService;
import app.service.ItemService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/quiosques")
public class ItemController {
	
	@Autowired
    private ItemService itemService;
	
	@GetMapping("/itens/{id}")
	public ResponseEntity<ItemDTO> get(
			@PathVariable Long id){
		
		ItemDTO item = this.itemService.findById(id);
		
		return ResponseEntity.ok(item);
	}
	
	@PostMapping("/me/categorias/{id_categoria}/itens")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<ItemDTO> save(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_categoria,
			@RequestBody @Valid ItemCreateRequest data){
		
		ItemDTO item = this.itemService.save(administrador, id_categoria, data);
		
	    return ResponseEntity.status(HttpStatus.CREATED).body(item);
	}
	
	@PutMapping("/me/categorias/{id_categoria}/itens/{id}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<ItemDTO> update(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_categoria,
			@PathVariable Long id,
			@RequestBody @Valid ItemCreateRequest data){	
		
		ItemDTO item = this.itemService.update(administrador, id_categoria, id, data);
		
		return ResponseEntity.ok(item);
	}
	
	@DeleteMapping("/me/categorias/{id_categoria}/itens/{id}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<Void> delete(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_categoria,
			@PathVariable Long id){
		
		this.itemService.deleteById(administrador,  id_categoria, id);
			
		return ResponseEntity.noContent().build();
	}
	
	@PostMapping("/me/categorias/{id_categoria}/itens/{id}/imagem")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<String> updateImagem(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_categoria,
			@PathVariable Long id,
			@RequestParam MultipartFile file){		
		
		String nomeArquivo = this.itemService.uploadImagemItem(administrador, id, file);
		
		return ResponseEntity.ok(nomeArquivo);
	}
	
	@DeleteMapping("/me/categorias/{id_categoria}/itens/{id}/imagem")
	@PreAuthorize("hasAnyRole('FUNCIONARIO')")
	public ResponseEntity<Void> deleteImagem(
			@AuthenticationPrincipal Usuario administrador,
			@PathVariable Long id_categoria,
			@PathVariable Long id) {		
		
		this.itemService.deleteImagemItem(administrador, id);
		
		return ResponseEntity.noContent().build();
	}
}
