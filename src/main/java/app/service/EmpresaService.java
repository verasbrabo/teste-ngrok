package app.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.EmpresaOverviewDTO;
import app.DTO.FuncionarioOverviewDTO;
import app.DTO.PedidoOverviewDTO;
import app.DTO.QuiosqueCreateDTO;
import app.DTO.QuiosqueOverviewDTO;
import app.auth.AuthRepository;
import app.entity.AvaliacaoResumo;
import app.entity.Coordenada;
import app.entity.Endereco;
import app.entity.Funcionario;
import app.entity.ItemPedido;
import app.entity.Pedido;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.StatusConta;
import app.repository.ItemPedidoRepository;
import app.repository.PedidoRepository;
import app.repository.QuiosqueMetricasProjection;
import app.repository.QuiosqueRepository;

@Service
public class EmpresaService {
	
	@Autowired
	QuiosqueRepository quiosqueRepository;
	@Autowired
	PedidoRepository pedidoRepository;
	@Autowired
	AuthRepository funcionarioRepository;
	@Autowired
	ItemPedidoRepository itemPedidoRepository;
	
	public EmpresaOverviewDTO overview(Funcionario usuario) {
		List<Quiosque> quiosques = this.quiosqueRepository.findByProprietario(usuario);
		
		if (quiosques.isEmpty()) {
	        return new EmpresaOverviewDTO(
	                List.of(),
	                List.of(),
	                List.of()
	        );
	    }
		
		List<Long> quiosqueIds =
                quiosques.stream()
                        .map(Quiosque::getId)
                        .toList();
		
		List<QuiosqueOverviewDTO> quiosqueOverview = getQuiosqueOverview(quiosques, quiosqueIds);
		
		List<FuncionarioOverviewDTO> funcionarioOverview = getFuncionarioOverview(quiosqueIds);
		
		List<PedidoOverviewDTO> pedidoOverview = getPedidoOverview(quiosqueIds);		
		
		return new EmpresaOverviewDTO(
				quiosqueOverview, 
				funcionarioOverview, 
				pedidoOverview);
	}
	
	public List<QuiosqueOverviewDTO> overviewQuiosques(Funcionario usuario){
		List<Quiosque> quiosques = this.quiosqueRepository.findByProprietario(usuario);
		
		if (quiosques.isEmpty()) {
	        return List.of();       
	    }
		
		List<Long> quiosqueIds =
                quiosques.stream()
                        .map(Quiosque::getId)
                        .toList();
		
		return getQuiosqueOverview(quiosques, quiosqueIds);
	}
	
	public List<FuncionarioOverviewDTO> overviewFuncionarios(Funcionario usuario){		
//		List<Quiosque> quiosques = this.quiosqueRepository.findByProprietario(usuario);
		
		List<Long> quiosqueIds = this.quiosqueRepository.findIdsByProprietario(usuario);
		
		if (quiosqueIds.isEmpty()) {
	        return List.of();       
	    }
		
//		List<Long> quiosqueIds =
//                quiosques.stream()
//                        .map(Quiosque::getId)
//                        .toList();
		
		return getFuncionarioOverview(quiosqueIds);
	}
	
	public Page<PedidoOverviewDTO> overviewPedidos(Funcionario usuario, Pageable pageable){
//		List<Quiosque> quiosques = this.quiosqueRepository.findByProprietario(usuario);
		List<Long> quiosqueIds = this.quiosqueRepository.findIdsByProprietario(usuario);
		
		if (quiosqueIds.isEmpty()) {
	        return Page.empty();       
	    }
		
//		List<Long> quiosqueIds =
//                quiosques.stream()
//                        .map(Quiosque::getId)
//                        .toList();
		
		return getPedidoOverview(quiosqueIds, pageable);
	}
	
	private Page<PedidoOverviewDTO> getPedidoOverview(List<Long> quiosqueIds, Pageable pageable) {
//		Pageable pageableOrdenado = PageRequest.of(
//			    pageable.getPageNumber(),
//			    pageable.getPageSize(),
//			    Sort.by(Sort.Direction.DESC, "dataHoraPedido")
//			);
		
		Page<Pedido> pedidos = pedidoRepository.findByQuiosqueIdInOrderByDataHoraPedidoDesc(quiosqueIds, pageable);
		
		List<UUID> pedidoIds = pedidos.stream()
		        .map(Pedido::getId)
		        .toList();
		
//		List<ItemPedido> itens = itemPedidoRepository.findByPedidoIdIn(pedidoIds);
		List<ItemPedido> itens =
			    itemPedidoRepository.findByPedidoIdInWithDetails(pedidoIds);
		
		Map<UUID, List<ItemPedido>> itensPorPedido =
		        itens.stream()
		                .collect(Collectors.groupingBy(
		                        item -> item.getPedido().getId()
		                ));
		
		List<PedidoOverviewDTO> overview = pedidos.stream()
		        .map(p -> {

		            List<ItemPedido> itensDoPedido =
		                    itensPorPedido.getOrDefault(
		                            p.getId(),
		                            List.of()
		                    );

		            return PedidoOverviewDTO.from(
		                    p,
		                    p.getQuiosque(),
		                    itensDoPedido
		            );
		        })
		        .toList();
		
		return new PageImpl<>(
	            overview,
	            pageable,
	            pedidos.getTotalElements()
	    );
	}
	
	private List<PedidoOverviewDTO> getPedidoOverview(List<Long> quiosqueIds) {
		List<Pedido> pedidos = pedidoRepository.findByQuiosqueIdInOrderByDataHoraPedidoDesc(quiosqueIds);
		
		List<UUID> pedidoIds = pedidos.stream()
		        .map(Pedido::getId)
		        .toList();
		
		List<ItemPedido> itens =
			    itemPedidoRepository.findByPedidoIdInWithDetails(pedidoIds);
		
		Map<UUID, List<ItemPedido>> itensPorPedido =
		        itens.stream()
		                .collect(Collectors.groupingBy(
		                        item -> item.getPedido().getId()
		                ));
		
		List<PedidoOverviewDTO> overview = pedidos.stream()
		        .map(p -> {

		            List<ItemPedido> itensDoPedido =
		                    itensPorPedido.getOrDefault(
		                            p.getId(),
		                            List.of()
		                    );

		            return PedidoOverviewDTO.from(
		                    p,
		                    p.getQuiosque(),
		                    itensDoPedido
		            );
		        })
		        .toList();
		
		return overview;
	}
	
	private List<FuncionarioOverviewDTO> getFuncionarioOverview(List<Long> quiosqueIds) {
		List<Funcionario> funcionarios = funcionarioRepository.findByQuiosqueIdIn(quiosqueIds);
		List<FuncionarioOverviewDTO> overview =
	                funcionarios.stream()
	                        .map(f ->   FuncionarioOverviewDTO.from(f, f.getQuiosque())).toList();
		return overview;
	}
	
	private List<QuiosqueOverviewDTO> getQuiosqueOverview(List<Quiosque> quiosques, List<Long> quiosqueIds) {

//        ZoneId zone = ZoneId.of("America/Sao_Paulo");
//        ZonedDateTime agora = ZonedDateTime.now(zone);

		LocalDateTime agora = LocalDateTime.now();
		
        LocalDate hoje = agora.toLocalDate();
        LocalTime horarioAtual = agora.toLocalTime();

        LocalDateTime inicioHoje =
                hoje.atStartOfDay();

        LocalDateTime fimHoje =
                agora;

        LocalDateTime inicioOntem =
                hoje.minusDays(1).atStartOfDay();

        LocalDateTime fimOntem =
                LocalDateTime.of(
                        hoje.minusDays(1),
                        horarioAtual
                );

        List<QuiosqueMetricasProjection> projections =
                pedidoRepository.buscarMetricasOverview(
                		quiosqueIds,
                        inicioHoje,
                        fimHoje,
                        inicioOntem,
                        fimOntem
                );

        Map<Long, QuiosqueMetricasProjection> metricasPorQuiosque =
                projections.stream()
                        .collect(Collectors.toMap(
                                QuiosqueMetricasProjection::getQuiosqueId,
                                Function.identity()
                        ));

        List<QuiosqueOverviewDTO> overview =
                quiosques.stream()
                        .map(q -> {

                            QuiosqueMetricasProjection metricas =
                                    metricasPorQuiosque.get(q.getId());

                            return QuiosqueOverviewDTO.from(
                                    q,
                                    metricas
                            );
                        })
                        .toList();

        return overview;
    }
	
}
