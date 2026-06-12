package app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import app.DTO.AcompanhamentoCreateDTO;
import app.DTO.AcompanhamentoViewDTO;
import app.entity.Acompanhamento;
import app.entity.Quiosque;
import app.entity.Usuario;
import app.enums.UserRole;
import app.repository.AcompanhamentoRepository;
import app.repository.QuiosqueRepository;
import jakarta.transaction.Transactional;

@Service
public class AcompanhamentoService {
	
	@Autowired
	private QuiosqueRepository quiosqueRepository;
	@Autowired
	private AcompanhamentoRepository acompanhamentoRepository;

	
	public List<AcompanhamentoViewDTO> findByQuiosque(Usuario usuario){
		Quiosque quiosque = findQuiosqueForUser(usuario);
		
		List<Acompanhamento> acompanhamentos = this.acompanhamentoRepository.findByQuiosque(quiosque);			
		
		return acompanhamentos.stream().map(AcompanhamentoViewDTO::from).toList();	
	}
	
	@Transactional
	public AcompanhamentoViewDTO save(Usuario usuario, AcompanhamentoCreateDTO data) {

	    Quiosque quiosque = findQuiosqueForUser(usuario);
	    
	    Acompanhamento acompanhamento = new Acompanhamento(data.nome(), data.valor());
	    
	    acompanhamento.setQuiosque(quiosque);

	    return AcompanhamentoViewDTO.from(acompanhamentoRepository.save(acompanhamento));
	}
	
	@Transactional
	public AcompanhamentoViewDTO update(Usuario usuario, Long id, AcompanhamentoCreateDTO data) {
		
		Quiosque quiosque = findQuiosqueForUser(usuario);		
		
		Acompanhamento acompanhamento = this.acompanhamentoRepository.findByQuiosqueAndId(quiosque, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Acompanhamento não encontrado nesse quiosque")));
		
		acompanhamento.setNome(data.nome());
		acompanhamento.setValor(data.valor());
		
	    return AcompanhamentoViewDTO.from(acompanhamentoRepository.save(acompanhamento));
	}
	
	@Transactional
	public void deleteById(Usuario usuario, Long id){
		
		Quiosque quiosque = findQuiosqueForUser(usuario);
		
		Acompanhamento acompanhamento = this.acompanhamentoRepository.findByQuiosqueAndId(quiosque, id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,("Acompanhamento não encontrado  nesse quiosque")));	
		
		acompanhamentoRepository.delete(acompanhamento);
	}	
	
	private Quiosque findQuiosqueForUser(Usuario usuario) {
		Quiosque quiosque;
		if(usuario.getRole().equals(UserRole.PROPRIETARIO))
			quiosque = quiosqueRepository.findByProprietario(usuario)
	        .orElseThrow(() -> new ResponseStatusException(
	            HttpStatus.NOT_FOUND, "Quiosque não encontrado"));
		else
			quiosque = quiosqueRepository.findByFuncionariosContaining(usuario)
			.orElseThrow(() -> new ResponseStatusException(
		        HttpStatus.NOT_FOUND, "Quiosque não encontrado"));

	    return quiosque;
	}
}
