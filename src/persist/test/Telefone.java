package persist.test;

import persistence.Entity;

public class Telefone implements Entity{
	private int id;
	private String descricao;
	private Pessoa pessoa;
	public Telefone(){}
	public Telefone(String tel){
		this.descricao = tel;
	}
	public Pessoa getPessoa() {
		return pessoa;
	}
	public void setPessoa(Pessoa pessoa) {
		this.pessoa = pessoa;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescricao() {
		return descricao;
	}
	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
