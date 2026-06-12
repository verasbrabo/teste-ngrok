package app.DTO;

import java.util.List;

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
 */
public record PedidoInternoDTO(
		@NotNull
		@NotEmpty
		List<@Valid ItemPedidoDTO> itens,
		String nomeCliente
	) {}
