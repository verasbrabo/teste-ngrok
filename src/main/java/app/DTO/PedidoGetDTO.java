package app.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import app.entity.Pedido;
import app.enums.StatusPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PedidoGetDTO(
		UUID id,
		@NotNull
		Long id_quiosque,
		@NotNull
	    String nome_quiosque,
	    @NotNull
	    Long id_cliente,
	    @NotNull
	    String nome_cliente,
	    
	    Long id_entregador,
	    
	    String nome_entregador,
	    @NotNull
	    BigDecimal valorTotal,
	    @NotNull
		LocalDateTime dataHoraPedido,
		// Momento em que o pedido entrou em PREPARANDO (base da janela de 30 min
		// para o cliente poder cancelar). Nulo enquanto CRIADO.
		LocalDateTime dataHoraPreparando,
		// Momento em que o pedido foi finalizado/entregue. Nulo enquanto não
		// finalizado.
		LocalDateTime dataHoraEntrega,
		Long tempoEstimado,
	    @NotNull
	    List<@Valid ItemPedidoGetDTO> itens,
	    @NotNull
	    double latitudeEntrega,
	    @NotNull
	    double longitudeEntrega,
	    @NotNull
	    StatusPedido status,
	    String motivo,
	    // Nota da avaliação do cliente (1..5); nula enquanto não avaliado.
	    Integer nota,
	    @NotNull
	    boolean interno)
{
	public static PedidoGetDTO from(Pedido p) {
	    // A coordenada pode ser nula (ex.: pedido sem lat/long persistidos). Sem
	    // este guard, um único pedido sem coordenada lançava NPE e derrubava a
	    // listagem inteira (GET /pedidos), escondendo todo o histórico.
	    var coordenada = p.getCoordenada();
	    // Pedidos de balcão usam um nome avulso; os demais, o nome do cliente.
	    String nomeCliente = p.getNomeCliente() != null && !p.getNomeCliente().isBlank()
	        ? p.getNomeCliente()
	        : p.getCliente().getNome();
	    return new PedidoGetDTO(
	    	p.getId(),
	    	p.getQuiosque().getId(),
	    	p.getQuiosque().getNome(),
	    	p.getCliente().getId(),
	    	nomeCliente,
	    	p.getEntregador() != null ? p.getEntregador().getId() : null,
	    	p.getEntregador() != null ? p.getEntregador().getNome() : null,
	        p.getValorTotal(),
	        p.getDataHoraPedido(),
	        p.getDataHoraPreparando(),
	        p.getDataHoraEntrega(),
	        p.getTempoEstimado(),
	        p.getItemPedidos().stream().map(ItemPedidoGetDTO::from).toList(),
	        coordenada != null ? coordenada.getLatitude() : 0.0,
	        coordenada != null ? coordenada.getLongitude() : 0.0,
	        p.getStatus(),
	        p.getMotivoCancel(),
	        p.getNota(),
	        p.isInterno()
	    );
	}
	

}
