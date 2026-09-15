package app.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import app.DTO.LocalizacaoEntregaCreateDTO;
import app.DTO.PontoRastreamentoEntregaDTO;
import app.DTO.RastreamentoEntregaDTO;
import app.entity.Usuario;
import app.service.RastreamentoEntregaService;
import jakarta.validation.Valid;

/**
 * Endpoints exclusivos do tracking GPS de pedidos em entrega.
 */
@RestController
public class RastreamentoEntregaController {
	@Autowired
	private RastreamentoEntregaService rastreamentoService;

	@PostMapping("/quiosques/{quiosqueId}/pedidos/{pedidoId}/rastreamento")
	@PreAuthorize("hasRole('FUNCIONARIO')")
	public ResponseEntity<PontoRastreamentoEntregaDTO> publicar(
			@AuthenticationPrincipal Usuario funcionario,
			@PathVariable Long quiosqueId,
			@PathVariable UUID pedidoId,
			@RequestBody @Valid LocalizacaoEntregaCreateDTO data) {
		return ResponseEntity.ok(
				rastreamentoService.publicar(funcionario, quiosqueId, pedidoId, data));
	}

	@GetMapping("/pedidos/{pedidoId}/rastreamento")
	@PreAuthorize("hasRole('CLIENTE')")
	public ResponseEntity<RastreamentoEntregaDTO> buscar(
			@AuthenticationPrincipal Usuario cliente,
			@PathVariable UUID pedidoId) {
		return ResponseEntity.ok(rastreamentoService.buscar(cliente, pedidoId));
	}

	@GetMapping(
			value = "/pedidos/{pedidoId}/rastreamento/eventos",
			produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	@PreAuthorize("hasRole('CLIENTE')")
	public SseEmitter acompanhar(
			@AuthenticationPrincipal Usuario cliente,
			@PathVariable UUID pedidoId) {
		return rastreamentoService.acompanhar(cliente, pedidoId);
	}
}
