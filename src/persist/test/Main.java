package persist.test;

import java.util.List;

import persistence.EntityManager;
import persistence.Persistence;



public class Main {
	public static void main(String[] args) throws Exception{
		Persistence persistence = new Persistence("localhost", "mydb", "root", "");
		
		persistence.map(Pessoa.class);
		persistence.map(Telefone.class);
		persistence.map(Cidade.class);
		
		persistence.show();
		
		EntityManager manager = persistence.getEntityManager();
		manager.openConnection();

		Cidade c = manager.findById(Cidade.class, 3);
		
		/*Pessoa p = new Pessoa();
		p.setNome("Teste ATP");
		p.setDataNascimento(Calendar.getInstance());
		p.setCidade(c);
		p.addTelefone(new Telefone("123124"));
		p.addTelefone(new Telefone("432423"));
		
		manager.begin();
		manager.save(p);
		manager.commit();
		
		p.setNome("teste mudado");
		
		manager.begin();
		manager.save(p);
		manager.commit();
		*/
		
		List<Pessoa> p = manager.find(Pessoa.class, null);
		System.out.println(p);
	}
}
