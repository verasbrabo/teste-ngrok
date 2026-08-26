package app.DTO;
import java.time.LocalTime;

import app.entity.Quiosque;
import app.enums.StatusConta;
public record QuiosqueCreateDTO(	
		Long id,
		String nome,
		String email,
		LocalTime openingTime,  
	    LocalTime closingTime,	    
	    Long distAtendimento,
		String cep,
		String uf,
		String cidade,
		String referencia,
		String rua,
		String numero,
		Double latitude,
		Double longitude,
		StatusConta status
		
		) {
	
	public static QuiosqueCreateDTO from(Quiosque q) {
    	return new QuiosqueCreateDTO(
    			q.getId(),
				q.getNome(),
				q.getEmail(),
				q.getOpeningTime(),
				q.getClosingTime(),
				q.getDistAtendimento(),
				q.getEndereco().getCep(),
				q.getEndereco().getUf(),
				q.getEndereco().getCidade(),
				q.getEndereco().getReferencia(),
				q.getEndereco().getRua(),
				q.getEndereco().getNumero(),
				q.getCoordenada().getLatitude(),
				q.getCoordenada().getLongitude(),
				q.getStatus()
				);
	}

}
