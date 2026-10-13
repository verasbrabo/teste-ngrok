package app.DTO;

import java.math.BigDecimal;
import java.util.List;

import app.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo de criação de um pedido interno (balcão), feito pelo próprio quiosque.
 *
 * Diferente de {@link PedidoDTO}, não exige o id do quiosque (resolvido a partir
 * do funcionário autenticado) nem coordenadas de entrega (não há entrega).
 *
 * @param itens       itens do pedido (obrigatório, ao menos um).
 * @param nomeCliente nome avulso para identificar o pedido no balcão
 *                    (opcional; quando vazio, assume "Balcão").
 * @param formaPagamento forma escolhida para receber o pagamento.
 * @param valorPagoCliente valor entregue pelo cliente quando o pagamento for
 *                         em dinheiro.
 */
public record PedidoInternoDTO(
		@NotNull
		@NotEmpty
		List<@Valid ItemPedidoDTO> itens,
		String nomeCliente,
		@NotNull
		FormaPagamento formaPagamento,
		BigDecimal valorPagoCliente
	) {}
