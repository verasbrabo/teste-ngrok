package app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.DTO.PedidoDTO;
import app.DTO.PedidoInternoDTO;
import app.DTO.PedidoGetDTO;
import app.DTO.AtualizarProximidadeDTO;
import app.DTO.AtualizarStatusDTO;
import app.DTO.AvaliarDTO;
import app.DTO.MotivoCancelamentoDTO;
import app.DTO.PedidoCreateResponseDTO;
import app.DTO.ValidarCodigoDTO;
import app.entity.Cliente;
import app.entity.Pedido;
import app.entity.Usuario;
import app.enums.StatusPedido;
import app.service.PedidoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
public class PedidoController {

	@Autowired
	PedidoService pedidoService;
	
	// Busca um pedido do PRÓPRIO usuário (findByClienteAndId); o app do
	// cliente usa como fallback de notificação, então CLIENTE também acessa.
	@GetMapping("/pedidos/{id}")
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	public ResponseEntity<PedidoGetDTO> getMeuPedido(
	        @AuthenticationPrincipal Usuario admin, 
	        @PathVariable UUID id) {
		
		Pedido pedido = pedidoService.getMeuPedido(admin, id);
	    
	    return ResponseEntity.ok(PedidoGetDTO.from(pedido));
	}
	
	@PreAuthorize("hasRole('CLIENTE')")
	@GetMapping("/pedidos")
	public List<PedidoGetDTO> getMeusPedidos(
	        @AuthenticationPrincipal Usuario cliente,
	        @RequestParam(required = false) StatusPedido status
//	        ,Pageable pageable
	        ) {

		List<Pedido>  pages = pedidoService.getMeusPedidos(cliente, status);
	    
	    return pages.stream().map(PedidoGetDTO::from).toList();
	    
	}
	
	@PreAuthorize("hasRole('CLIENTE')")
	@GetMapping("/pedidos/ativos")
	public List<PedidoGetDTO> getMeusPedidosAtivos(
	        @AuthenticationPrincipal Usuario cliente
//	        ,Pageable pageable
	        ) {

		List<Pedido>  pages = pedidoService.getMeusPedidosAtivos(cliente);
	    
	    return pages.stream().map(PedidoGetDTO::from).toList();
	    
	}
	
	@PreAuthorize("hasRole('CLIENTE')")
	@PostMapping("/pedidos")
	public ResponseEntity<PedidoGetDTO> createMeuPedido(
								@AuthenticationPrincipal Cliente cliente,
								@RequestBody @Valid PedidoDTO data) {

		return ResponseEntity.status(HttpStatus.CREATED).body(this.pedidoService.createMeuPedido(cliente, data));
	}

	@PreAuthorize("hasRole('FUNCIONARIO')")
	@PostMapping("/quiosques/{id_quiosque}/pedidos/interno")
	public ResponseEntity<PedidoGetDTO> createPedidoInterno(
								@AuthenticationPrincipal Usuario funcionario,
								@PathVariable Long id_quiosque,
								@RequestBody @Valid PedidoInternoDTO data) {

		return ResponseEntity.status(HttpStatus.CREATED).body(this.pedidoService.createPedidoInterno(funcionario, id_quiosque, data));
	}
	
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	@PostMapping("/pedidos/{id}/cancelar")
	public ResponseEntity<String> cancelarPedido(
			@AuthenticationPrincipal Usuario cliente,
			@PathVariable UUID id
			){
		return ResponseEntity.ok(this.pedidoService.cancelarPedido(cliente, id));
	}
	
	@PreAuthorize("hasRole('CLIENTE')")
	@GetMapping("/pedidos/{id}/codigo")
	public ResponseEntity<ValidarCodigoDTO> getCodigo(
											@AuthenticationPrincipal Usuario cliente,
											@PathVariable UUID id) {
		return ResponseEntity.ok(new ValidarCodigoDTO(this.pedidoService.getCodigo(cliente, id)));
	}
	
	@PreAuthorize("hasRole('CLIENTE')")
	@PostMapping("/pedidos/{id}/avaliar")
	public ResponseEntity<String> avaliarPedido(
			@AuthenticationPrincipal Usuario cliente,
			@PathVariable UUID id,
			@RequestBody AvaliarDTO data) {
		
	return ResponseEntity.ok(pedidoService.avaliarPedido(cliente, id, data));
	}

	@PreAuthorize("hasRole('FUNCIONARIO')")
	@PostMapping("/quiosques/{id_quiosque}/pedidos/{id}/validar-codigo")
	public ResponseEntity<Boolean> validarCodigo(
						 @AuthenticationPrincipal Usuario entregador,
						 @PathVariable Long id_quiosque,
						 @PathVariable UUID id, 
						 @RequestBody @Valid ValidarCodigoDTO data) {
		return ResponseEntity.ok(pedidoService.validarCodigo(entregador, id_quiosque, id, data));
	}
	
	@GetMapping("/quiosques/{id_quiosque}/pedidos")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public List<PedidoGetDTO> getPedidos(
	        @AuthenticationPrincipal Usuario admin,
	        @PathVariable Long id_quiosque,
	        @RequestParam(required = false) StatusPedido status
//	        ,Pageable pageable
	        ) {
		// N + 1
		List<Pedido> pedidos = pedidoService.getPedidos(admin, id_quiosque, status);
	    
	    return pedidos.stream().map(PedidoGetDTO::from).toList();
	}
	
	@GetMapping("/quiosques/{id_quiosque}/pedidos/ativos")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public List<PedidoGetDTO> getPedidosAtivos(
	        @AuthenticationPrincipal Usuario admin,
	        @PathVariable Long id_quiosque
//	        ,Pageable pageable
	        ) {
		
		List<Pedido> pedidos = pedidoService.getPedidosAtivos(admin, id_quiosque);
	    
	    return pedidos.stream().map(PedidoGetDTO::from).toList();
	}
	
	@GetMapping("/quiosques/{id_quiosque}/pedidos/entregar")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public ResponseEntity<PedidoGetDTO> getPedidoEntregador(
	        @AuthenticationPrincipal Usuario admin,
	        @PathVariable Long id_quiosque) {
		
		Pedido pedido = pedidoService.getPedidoEntregador(admin, id_quiosque);
	    
	    return ResponseEntity.ok(PedidoGetDTO.from(pedido));
	}
	
	@PostMapping("/quiosques/{id_quiosque}/pedidos/{id}/entregador")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public ResponseEntity<PedidoGetDTO> entregadorPedido(
			@AuthenticationPrincipal Usuario admin,
			@PathVariable Long id_quiosque,
			@PathVariable UUID id) {
		
		return ResponseEntity.ok(pedidoService.entregadorPedido(admin, id_quiosque, id));
	}
	
	@PatchMapping("/quiosques/{id_quiosque}/pedidos/{id}")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public ResponseEntity<PedidoGetDTO> updatePedido(
			@AuthenticationPrincipal Usuario admin,
			@PathVariable Long id_quiosque,
			@PathVariable UUID id,
	        @RequestBody  AtualizarStatusDTO data) {
	    return ResponseEntity.ok(pedidoService.updatePedido(id, id_quiosque, admin, data));
	}

	// Atualiza a telemetria de proximidade calculada pelo app do quiosque.
	// Nao finaliza pedido; serve apenas para expor o RSSI medio quando necessario.
	@PatchMapping("/quiosques/{id_quiosque}/pedidos/{id}/proximidade")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public ResponseEntity<PedidoGetDTO> atualizarProximidadeEntrega(
			@AuthenticationPrincipal Usuario admin,
			@PathVariable Long id_quiosque,
			@PathVariable UUID id,
	        @RequestBody @Valid AtualizarProximidadeDTO data) {
	    return ResponseEntity.ok(pedidoService.atualizarProximidadeEntrega(id, id_quiosque, admin, data));
	}
	
	@PreAuthorize("hasAnyRole('FUNCIONARIO','CLIENTE')")
	@PostMapping("/quiosque/{id_quiosque}/pedidos/{id}/cancelar")
	public ResponseEntity<PedidoGetDTO> quiosqueCancelarPedidoQ(
			@AuthenticationPrincipal Usuario cliente,
			@PathVariable Long id_quiosque,
			@PathVariable UUID id,
			@RequestBody MotivoCancelamentoDTO data
			){
		return ResponseEntity.ok(this.pedidoService.quiosqueCancelarPedido(cliente, id_quiosque, id, data));
	}
	
	@PatchMapping("/quiosques/{id_quiosque}/pedidos/{id}/rejeitar")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public ResponseEntity<PedidoCreateResponseDTO> rejeitarPedido(
			@AuthenticationPrincipal Usuario admin,
			@PathVariable Long id_quiosque,
			@PathVariable UUID id) {
	    return ResponseEntity.ok(pedidoService.rejeitarPedido(admin, id_quiosque, id));
	}
}
