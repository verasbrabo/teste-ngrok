package app.auth.DTO;

import jakarta.validation.constraints.Email;

public record EmailDTO(
		@Email
		String email) {
	

}
