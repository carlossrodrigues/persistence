package persistence;

import java.util.HashMap;
import java.util.Map;

import persist.test.Pessoa;

public class Cache {
	private Map<Class, Map<Integer, Entity>> map;

	public void start(){
		map = new HashMap<Class, Map<Integer, Entity>>();
	}
	public void close(){
		map = null;
	}

	public void addToCache(Class clazz, Entity t){
		Map<Integer, Entity> map = this.map.get(clazz);

		if(map == null){
			this.map.put(clazz, map = new HashMap<Integer, Entity>());
		}

		map.put(t.getId(), t);
	}

	public <T> T getFromCache(Class<T> clazz, int id){
		Map<Integer, T> map = (Map<Integer, T>) this.map.get(clazz);
		if(map != null){
			return map.get(id);
		}
		return null;
	}

	public <T> void removeFromCache(Class<T> clazz, int id){
		Map<Integer, T> map = (Map<Integer, T>) this.map.get(clazz);
		if(map != null){
			map.remove(id);
		}
	}

	public static void main(String[] args){
		Cache c = new Cache();
		c.start();
		Pessoa p = new Pessoa();
		p.setId(1);
		c.addToCache(Pessoa.class, p);

		p = new Pessoa();
		p.setId(2);
		c.addToCache(Pessoa.class, p);

		System.out.println(c.getFromCache(Pessoa.class, 1));
		c.close();
	}
}