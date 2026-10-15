package app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.DailyRevenueDTO;
import app.DTO.DailyRevenuePointDTO;
import app.DTO.EmpresaOverviewDTO;
import app.DTO.EstatisticasDTO;
import app.DTO.FuncionarioOverviewDTO;
import app.DTO.KioskRevenueDTO;
import app.DTO.OrderCategoryDTO;
import app.DTO.OrdersByCategoryDTO;
import app.DTO.PedidoOverviewDTO;
import app.DTO.QuiosqueOverviewDTO;
import app.DTO.RevenueByKioskDTO;
import app.DTO.TopProductDTO;
import app.DTO.TopProductsDTO;
import app.auth.AuthRepository;
import app.entity.Funcionario;
import app.entity.ItemPedido;
import app.entity.Pedido;
import app.entity.Quiosque;
import app.repository.FaturamentoPorQuiosqueProjection;
import app.repository.ItemPedidoRepository;
import app.repository.PedidosPorCategoriaProjection;
import app.repository.PedidoRepository;
import app.repository.ProdutoMaisVendidoProjection;
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

	public EstatisticasDTO statistics(
			Funcionario proprietario,
			LocalDate startDate,
			LocalDate endDate,
			Long kioskId) {
		if (startDate.isAfter(endDate)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"A data inicial não pode ser posterior à data final.");
		}

		List<Quiosque> quiosquesDoProprietario = quiosqueRepository.findByProprietario(proprietario);
		List<Quiosque> quiosquesSelecionados = quiosquesDoProprietario.stream()
				.filter(quiosque -> kioskId == null || quiosque.getId().equals(kioskId))
				.toList();

		if (kioskId != null && quiosquesSelecionados.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiosque não encontrado.");
		}

		LocalDateTime inicio = startDate.atStartOfDay();
		LocalDateTime fim = endDate.plusDays(1).atStartOfDay();
		List<LocalDate> datas = startDate.datesUntil(endDate.plusDays(1)).toList();

		List<FaturamentoPorQuiosqueProjection> faturamentos =
				pedidoRepository.buscarFaturamentoPorQuiosque(
						proprietario.getId(), kioskId, inicio, fim);

		Map<Long, List<FaturamentoPorQuiosqueProjection>> faturamentosPorQuiosque =
				faturamentos.stream().collect(Collectors.groupingBy(
						FaturamentoPorQuiosqueProjection::getKioskId,
						LinkedHashMap::new,
						Collectors.toList()));

		List<KioskRevenueDTO> faturamentoDosQuiosques = quiosquesSelecionados.stream()
				.map(quiosque -> montarFaturamentoDoQuiosque(
						quiosque,
						faturamentosPorQuiosque.getOrDefault(quiosque.getId(), List.of()),
						datas))
				.toList();

		Map<LocalDate, BigDecimal> totalPorData = new HashMap<>();
		for (FaturamentoPorQuiosqueProjection faturamento : faturamentos) {
			totalPorData.merge(
					faturamento.getData(),
					valorOuZero(faturamento.getFaturamento()),
					BigDecimal::add);
		}

		ZoneOffset offsetBrasil = ZoneOffset.of("-03:00");
		List<DailyRevenuePointDTO> pontosDiarios = datas.stream()
				.map(data -> new DailyRevenuePointDTO(
						data.atTime(12, 0).atOffset(offsetBrasil),
						totalPorData.getOrDefault(data, BigDecimal.ZERO)))
				.toList();

		List<TopProductDTO> produtos = itemPedidoRepository.buscarProdutosMaisVendidos(
				proprietario.getId(), kioskId, inicio, fim, PageRequest.of(0, 5))
				.stream()
				.map(this::toTopProductDTO)
				.toList();

		List<OrderCategoryDTO> categorias = itemPedidoRepository.buscarPedidosPorCategoria(
				proprietario.getId(), kioskId, inicio, fim)
				.stream()
				.map(this::toOrderCategoryDTO)
				.toList();

		return new EstatisticasDTO(
				new RevenueByKioskDTO(datas, faturamentoDosQuiosques),
				new TopProductsDTO(produtos),
				new DailyRevenueDTO(pontosDiarios),
				new OrdersByCategoryDTO(categorias));
	}

	private KioskRevenueDTO montarFaturamentoDoQuiosque(
			Quiosque quiosque,
			List<FaturamentoPorQuiosqueProjection> faturamentos,
			List<LocalDate> datas) {
		Map<LocalDate, BigDecimal> faturamentoPorData = faturamentos.stream()
				.collect(Collectors.toMap(
						FaturamentoPorQuiosqueProjection::getData,
						item -> valorOuZero(item.getFaturamento()),
						BigDecimal::add));

		List<BigDecimal> valores = datas.stream()
				.map(data -> faturamentoPorData.getOrDefault(data, BigDecimal.ZERO))
				.toList();

		return new KioskRevenueDTO(quiosque.getId(), quiosque.getNome(), valores);
	}

	private TopProductDTO toTopProductDTO(ProdutoMaisVendidoProjection produto) {
		return new TopProductDTO(
				produto.getProductId(),
				produto.getProductName(),
				Math.toIntExact(produto.getUnitsSold()));
	}

	private OrderCategoryDTO toOrderCategoryDTO(PedidosPorCategoriaProjection categoria) {
		return new OrderCategoryDTO(
				categoria.getCategoryId(),
				categoria.getCategoryName().getCategorias(),
				Math.toIntExact(categoria.getOrderCount()));
	}

	private BigDecimal valorOuZero(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
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
