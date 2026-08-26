package app.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import app.entity.ItemPedido;
import app.entity.Pedido;
import app.entity.Quiosque;
import app.enums.StatusPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PedidoOverviewDTO(
		Long id_quiosque,
		@NotNull
	    String nome_quiosque,
	    @NotNull
	    Long id_cliente,
	    @NotNull
	    String nome_cliente,
	    @NotNull
	    BigDecimal valorTotal,
	    @NotNull
		LocalDateTime dataHoraPedido,
		List<ItemPedidoGetDTO> itens,
		StatusPedido status		
		) {

	public static PedidoOverviewDTO from(Pedido p, Quiosque q, List<ItemPedido> itens) {
		Long clienteId = null;
		String nomeCliente;

		if (p.isInterno()) {
		    nomeCliente = p.getNomeCliente();
		} else if (p.getCliente() != null) {
		    clienteId = p.getCliente().getId();
		    nomeCliente = p.getCliente().getNome();
		} else {
		    nomeCliente = null;
		}	
		return new PedidoOverviewDTO(
				q.getId(),
				q.getNome(),
				clienteId,
				nomeCliente,
				p.getValorTotal(),
				p.getDataHoraPedido(),
				itens.stream()
		                .map(ItemPedidoGetDTO::fromEmpresa)
		                .toList(),
				p.getStatus()
				);
	}
}
