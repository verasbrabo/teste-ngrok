package app.DTO;

/**
 * Dados editáveis do próprio perfil (PUT /me).
 *
 * Diferente do RegisterDTO, NÃO exige password/cpf/role: a edição de perfil
 * só altera nome/email/telefone. Campos nulos/em branco são ignorados
 * (atualização parcial) — ver {@link app.entity.Usuario#atualizarPerfil}.
 */
public record UpdateMeDTO(
		String nome,
		String email,
		String telefone) {

}
