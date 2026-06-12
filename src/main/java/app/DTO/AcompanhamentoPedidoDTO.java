package app.DTO;

import java.math.BigDecimal;

import app.entity.Acompanhamento;

/**
 * Acompanhamento (adicional) como aparece dentro de um item de pedido, agora
 * com o preço. Antes a visão do pedido devolvia apenas os nomes
 * (`acompanhamentosid`), sem valor, então o app não conseguia exibir o preço
 * do adicional no histórico.
 */
public record AcompanhamentoPedidoDTO(
		Long id,
		String nome,
		BigDecimal valor) {

	public static AcompanhamentoPedidoDTO from(Acompanhamento a) {
		return new AcompanhamentoPedidoDTO(a.getId(), a.getNome(), a.getValor());
	}
}
