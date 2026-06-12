package app.DTO;
import app.entity.Quiosque;
import app.enums.StatusConta;
public record QuiosqueCreateDTO(	
		Long id,
		String nome,
		String email,
		String cep,
		String uf,
		String cidade,
		Double latitude,
		Double longitude,
		StatusConta status
		
		) {
	
	public static QuiosqueCreateDTO from(Quiosque q) {
    	return new QuiosqueCreateDTO(
    			q.getId(),
				q.getNome(),
				q.getEmail(),
				q.getEndereco().getCep(),
				q.getEndereco().getUf(),
				q.getEndereco().getCidade(),
				q.getCoordenada().getLatitude(),
				q.getCoordenada().getLongitude(),
				q.getStatus()
				);
	}

}
