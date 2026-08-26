//package app.entity;
//
//import java.util.List;
//
//import app.DTO.EmpresaCreateDTO;
//import app.enums.StatusConta;
//import jakarta.persistence.Embedded;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.OneToMany;
//import jakarta.persistence.OneToOne;
//
//@Entity
//public class Empresa {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id; 
//
//	@OneToOne
//	@JoinColumn(name = "proprietario_id")
//	private Usuario proprietario;
//	
//	private String cnpj;
//	
//	private String razaoSocial; // Não sei se é necessario
//	
//	private String nomeFantasia;
//	
//	@Embedded
//	private Endereco endereco;
//	
//	private String email;
//	
//	private StatusConta status;
//	
//	@OneToMany(mappedBy = "empresa")
//	private List<Quiosque> quiosques;
//
//	public Empresa(Usuario proprietario, String cnpj, String nomeFantasia, String email, StatusConta status) {
//		super();
//		this.proprietario = proprietario;
//		this.cnpj = cnpj;
//		this.nomeFantasia = nomeFantasia;
//		this.email = email;
//		this.status = status;
//	}
//	
//	public Empresa(Usuario proprietario, String cnpj, String nomeFantasia, String email, Endereco endereco, StatusConta status) {
//		super();
//		this.proprietario = proprietario;
//		this.cnpj = cnpj;
//		this.nomeFantasia = nomeFantasia;
//		this.email = email;
//		this.status = status;
//		this.endereco = endereco;
//	}
//	
//	
//	public void atualizarDados(EmpresaCreateDTO empresa) {
//		
//		if (empresa.nomeFantasia() != null) {
//		    this.nomeFantasia = empresa.nomeFantasia();
//		}
//		
//		if (empresa.cnpj() != null) {
//			this.cnpj = empresa.cnpj() ;
//		}
//		
//		if (empresa.email() != null) {
//			this.email = empresa.email();
//		}
//
//		if (empresa.cep() != null) {
//		    this.endereco.setCep(empresa.cep());
//		}
//
//		if (empresa.uf() != null) {
//			this.endereco.setUf(empresa.uf());
//		}
//
//		if (empresa.cidade() != null) {
//			this.endereco.setCidade(empresa.cidade());
//		}
//	}
//
//	public Empresa() {
//		super();
//	}
//
//	public Long getId() {
//		return id;
//	}
//
//	public void setId(Long id) {
//		this.id = id;
//	}
//
//	public Usuario getProprietario() {
//		return proprietario;
//	}
//
//	public void setProprietario(Usuario proprietario) {
//		this.proprietario = proprietario;
//	}
//
//	public String getCnpj() {
//		return cnpj;
//	}
//
//	public void setCnpj(String cnpj) {
//		this.cnpj = cnpj;
//	}
//
//	public String getRazaoSocial() {
//		return razaoSocial;
//	}
//
//	public void setRazaoSocial(String razaoSocial) {
//		this.razaoSocial = razaoSocial;
//	}
//
//	public String getNomeFantasia() {
//		return nomeFantasia;
//	}
//
//	public void setNomeFantasia(String nomeFantasia) {
//		this.nomeFantasia = nomeFantasia;
//	}
//
//	public Endereco getEndereco() {
//		return endereco;
//	}
//
//	public void setEndereco(Endereco endereco) {
//		this.endereco = endereco;
//	}
//
//	public String getEmail() {
//		return email;
//	}
//
//	public void setEmail(String email) {
//		this.email = email;
//	}
//
//	public StatusConta getStatus() {
//		return status;
//	}
//
//	public void setStatus(StatusConta status) {
//		this.status = status;
//	}
//
//	public List<Quiosque> getQuiosques() {
//		return quiosques;
//	}
//
//	public void setQuiosques(List<Quiosque> quiosques) {
//		this.quiosques = quiosques;
//	}
//	
//	
//}
