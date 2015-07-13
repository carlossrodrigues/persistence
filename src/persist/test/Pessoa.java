package persist.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import persistence.Entity;

public class Pessoa implements Entity{
	private int id;
	private String nome;
	private Cidade cidade;
	private List<Telefone> telefones;
	private Calendar dataNascimento;
	@Override
	public int getId() {
		return id;
	}
	@Override
	public void setId(int id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public Cidade getCidade() {
		return cidade;
	}
	public void setCidade(Cidade cidade) {
		this.cidade = cidade;
	}
	public List<Telefone> getTelefones() {
		return telefones;
	}
	public void setTelefones(List<Telefone> telefones) {
		this.telefones = telefones;
	}
	public Calendar getDataNascimento() {
		return dataNascimento;
	}
	public void setDataNascimento(Calendar dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	public void addTelefone(Telefone t){
		if(telefones == null)
			telefones = new ArrayList<Telefone>();
		
		t.setPessoa(this);
		telefones.add(t);
	}
}