package app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.CupomCreateDTO;
import app.DTO.CupomRequestDTO;
import app.entity.Cupom;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.TipoCupom;
import app.repository.CupomRepository;
import app.repository.QuiosqueRepository;

@Service
public class CupomService {

	private static final BigDecimal PERCENTUAL_MINIMO = BigDecimal.valueOf(5);
	private static final BigDecimal PERCENTUAL_MAXIMO = BigDecimal.valueOf(95);
	
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private CupomRepository cupomRepository;
	
	public CupomCreateDTO save(
			Usuario administrador,
			Long id_quiosque,
			CupomRequestDTO data) {
		
		Quiosque q = findQuiosqueForOwner(administrador, id_quiosque);
		validarRegras(data);
		
		Cupom c = new Cupom(
				data.tipoCupom(),
				q,
				data.valorDesconto(), 
				data.valorMinimo(),
				limiteAplicavel(data),
				data.dataExpiracao(),
				true
				);
				
		return CupomCreateDTO.from(cupomRepository.save(c));
	}
	
	public List<CupomCreateDTO> findByQuiosque(Long id_quiosque) {
		
		List<Cupom> cupons = cupomRepository.findByQuiosqueIdAndAtivoAndDataExpiracaoAfter(id_quiosque, true, LocalDateTime.now());
		
		return cupons.stream().map(CupomCreateDTO::from).toList();
	}

	public List<CupomCreateDTO> findAllByQuiosque(Usuario proprietario, Long id_quiosque) {
		Quiosque quiosque = findQuiosqueForOwner(proprietario, id_quiosque);

		// A administração precisa enxergar inclusive os cupons que já expiraram
		// ou foram desativados para compreender o histórico do quiosque.
		return cupomRepository.findByQuiosqueIdOrderByIdDesc(quiosque.getId())
				.stream()
				.map(CupomCreateDTO::from)
				.toList();
	}
	
	public CupomCreateDTO update(
			Usuario administrador,
			Long id_quiosque,
			Long id,
			CupomRequestDTO data) {
		
		findQuiosqueForOwner(administrador, id_quiosque);
		validarRegras(data);
		
		Cupom cupom = this.cupomRepository.findByIdAndQuiosqueId(id, id_quiosque)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado"));
		
		// O PUT substitui todos os dados editáveis com um contrato validado.
		cupom.setTipoCupom(data.tipoCupom());
		cupom.setValorDesconto(data.valorDesconto());
		cupom.setValorMinimo(data.valorMinimo());
		cupom.setDescontoMaximo(limiteAplicavel(data));
		cupom.setDataExpiracao(data.dataExpiracao());
			
		
		return CupomCreateDTO.from(cupomRepository.save(cupom));
	}
	
	public CupomCreateDTO updateStatus(
			Usuario administrador,
			Long id_quiosque,
			Long id) {
		
		findQuiosqueForOwner(administrador, id_quiosque);
		
		Cupom cupom = this.cupomRepository.findByIdAndQuiosqueId(id, id_quiosque)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado"));
		
		boolean expirou = LocalDateTime.now().isAfter(cupom.getDataExpiracao());
		
		if(expirou) {
			cupom.setAtivo(false);
		} else {
			cupom.setAtivo(!cupom.isAtivo());
		}

		
		
		return CupomCreateDTO.from(cupomRepository.save(cupom));
	}
	
	
	/** Valida as regras condicionais que as anotações do DTO não representam. */
	static void validarRegras(CupomRequestDTO data) {
		if (data == null
				|| data.tipoCupom() == null
				|| data.valorDesconto() == null
				|| data.valorMinimo() == null
				|| data.dataExpiracao() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preencha os dados obrigatórios do cupom");
		}

		if (data.valorDesconto().compareTo(BigDecimal.ZERO) <= 0
				|| data.valorMinimo().compareTo(BigDecimal.ZERO) <= 0
				|| (data.descontoMaximo() != null
						&& data.descontoMaximo().compareTo(BigDecimal.ZERO) <= 0)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Os valores do cupom devem ser maiores que zero");
		}

		if (!data.dataExpiracao().isAfter(LocalDateTime.now())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A validade do cupom deve estar no futuro");
		}

		if (data.tipoCupom() == TipoCupom.PERCENTUAL
				&& (data.valorDesconto().compareTo(PERCENTUAL_MINIMO) < 0
						|| data.valorDesconto().compareTo(PERCENTUAL_MAXIMO) > 0)) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "O desconto percentual deve ficar entre 5% e 95%");
		}
	}

	/** Desconsidera o teto monetário quando o desconto já é um valor fixo. */
	private static BigDecimal limiteAplicavel(CupomRequestDTO data) {
		return data.tipoCupom() == TipoCupom.PERCENTUAL ? data.descontoMaximo() : null;
	}

	private Quiosque findQuiosqueForOwner(Usuario usuario, Long id) {

	    Quiosque quiosque = quiosqueRepository.findById(id)
	        .orElseThrow(() -> new ResponseStatusException(
	            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

	    // Acesso operacional ao quiosque não é suficiente: cupons pertencem
	    // exclusivamente ao proprietário titular daquele quiosque.
	    if (!quiosque.pertenceAoProprietario(usuario)) {
	        throw new ResponseStatusException(
	            HttpStatus.FORBIDDEN, "Apenas o proprietário do quiosque pode gerenciar cupons");
	    }

	    return quiosque;
	}

}
