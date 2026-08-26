package app.DTO;

import java.util.List;


public record EmpresaOverviewDTO(
		
		List<QuiosqueOverviewDTO> quiosques,
		
		List<FuncionarioOverviewDTO> funcionarios,
		
		List<PedidoOverviewDTO> pedidos
		
		
		
		) {
	
}
