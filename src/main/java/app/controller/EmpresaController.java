package app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import app.DTO.EmpresaOverviewDTO;
import app.DTO.FuncionarioOverviewDTO;
import app.DTO.PedidoOverviewDTO;
import app.DTO.QuiosqueCreateDTO;
import app.DTO.QuiosqueDTO;
import app.DTO.QuiosqueOverviewDTO;
import app.DTO.QuiosqueUpdateDTO;
import app.DTO.QuiosqueViewDTO;
import app.entity.Funcionario;
import app.entity.Usuario;
import app.service.EmpresaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/empresas")
public class EmpresaController {
	
	@Autowired
	EmpresaService empresaService;
	
//	@GetMapping("/me")
//	@PreAuthorize("hasRole('PROPRIETARIO')")
//	public ResponseEntity<String> view(
//			@AuthenticationPrincipal Funcionario usuario){
//		return ResponseEntity.status(HttpStatus.OK).body(this.empresaService.findByUser(usuario));
//	}
	
	@GetMapping("/me/overview")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<EmpresaOverviewDTO> overview(
			@AuthenticationPrincipal Funcionario usuario){
		return ResponseEntity.status(HttpStatus.OK).body(this.empresaService.overview(usuario));
	}
	
	@GetMapping("/me/overview/funcionarios")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<List<FuncionarioOverviewDTO>> overviewFuncionarios(
			@AuthenticationPrincipal Funcionario usuario){
		return ResponseEntity.status(HttpStatus.OK).body(this.empresaService.overviewFuncionarios(usuario));
	}

	@GetMapping("/me/overview/quiosques")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<List<QuiosqueOverviewDTO>> overviewQuiosques(
			@AuthenticationPrincipal Funcionario usuario){
		return ResponseEntity.status(HttpStatus.OK).body(this.empresaService.overviewQuiosques(usuario));
	}
	
//	@GetMapping("/me/overview/pedidos")
//	@PreAuthorize("hasRole('PROPRIETARIO')")
//	public ResponseEntity<List<PedidoOverviewDTO>> overviewPedidos(
//			@AuthenticationPrincipal Funcionario usuario){
//		return ResponseEntity.status(HttpStatus.OK).body(this.empresaService.overviewPedidos(usuario));
//	}
	
	// Pageble teste
	@GetMapping("/me/overview/pedidos")
	@PreAuthorize("hasRole('PROPRIETARIO')")
	public ResponseEntity<Page<PedidoOverviewDTO>> overviewPedidos(
			@AuthenticationPrincipal Funcionario usuario
			,Pageable pageable){
		return ResponseEntity.status(HttpStatus.OK).body(this.empresaService.overviewPedidos(usuario, pageable));
	}
	
}
