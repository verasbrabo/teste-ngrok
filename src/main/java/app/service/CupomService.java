package app.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.CupomCreateDTO;
import app.entity.Cupom;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.repository.CupomRepository;
import app.repository.QuiosqueRepository;

@Service
public class CupomService {
	
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private CupomRepository cupomRepository;
	
	public CupomCreateDTO save(
			Usuario administrador,
			Long id_quiosque,
			CupomCreateDTO data) {
		
		Quiosque q = findQuiosqueForUser(administrador, id_quiosque);
		
		Cupom c = new Cupom(
				data.tipoCupom(),
				q,
				data.valorDesconto(), 
				data.valorMinimo(),
				data.descontoMaximo(),
				data.dataExpiracao(),
				data.ativo()
				);
				
		return CupomCreateDTO.from(cupomRepository.save(c));
	}
	
	public List<CupomCreateDTO> findByQuiosque(Long id_quiosque) {
		
		List<Cupom> cupons = cupomRepository.findByQuiosqueIdAndAtivoAndDataExpiracaoAfter(id_quiosque, true, LocalDateTime.now());
		
		return cupons.stream().map(CupomCreateDTO::from).toList();
	}
	
	public CupomCreateDTO update(
			Usuario administrador,
			Long id_quiosque,
			Long id,
			CupomCreateDTO data) {
		
		findQuiosqueForUser(administrador, id_quiosque);
		
		Cupom cupom = this.cupomRepository.findByIdAndQuiosqueId(id, id_quiosque)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cupom não encontrado"));
		
		if(data.tipoCupom() != null)
			cupom.setTipoCupom(data.tipoCupom());
		
		if(data.valorDesconto() != null)
			cupom.setValorDesconto(data.valorDesconto());
		
		if(data.valorMinimo() != null)
			cupom.setValorMinimo(data.valorMinimo());
		
		if(data.descontoMaximo() != null)
			cupom.setDescontoMaximo(data.descontoMaximo());
		
		if(data.dataExpiracao() != null)
			cupom.setDataExpiracao(data.dataExpiracao());
			
		
		return CupomCreateDTO.from(cupomRepository.save(cupom));
	}
	
	public CupomCreateDTO updateStatus(
			Usuario administrador,
			Long id_quiosque,
			Long id) {
		
		findQuiosqueForUser(administrador, id_quiosque);
		
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
	
	
	private Quiosque findQuiosqueForUser(Usuario usuario, Long id) {

	    Quiosque quiosque = quiosqueRepository.findById(id)
	        .orElseThrow(() -> new ResponseStatusException(
	            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

	    if (!quiosque.usuarioPossuiAcesso(usuario)) {
	        throw new ResponseStatusException(
	            HttpStatus.FORBIDDEN, "Usuário não tem acesso a esse quiosque");
	    }

	    return quiosque;
	}

}
