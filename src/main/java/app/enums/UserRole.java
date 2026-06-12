package app.enums;

public enum UserRole {	
	PROPRIETARIO("proprietario"),
	CLIENTE("cliente"),
	GERENTE("gerente"),
	FUNCIONARIO("funcionario");
	
	private String role;
	
	UserRole(String role){
		this.role = role;
	}
	
	public String getRole() {
		return role;
	}	
}

//public enum UserRole {
//    PROPRIETARIO(List.of("ROLE_PROPRIETARIO", "ROLE_GERENTE", "ROLE_FUNCIONARIO")),
//    GERENTE(List.of("ROLE_GERENTE", "ROLE_FUNCIONARIO")),
//    FUNCIONARIO(List.of("ROLE_FUNCIONARIO"));
//
//    private final List<String> roles;
//
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return roles.stream()
//            .map(SimpleGrantedAuthority::new)
//            .toList();
//    }
//} return role.getAuthorities();
//public String getAuthority() {
//    return "ROLE_" + this.name();
//}

