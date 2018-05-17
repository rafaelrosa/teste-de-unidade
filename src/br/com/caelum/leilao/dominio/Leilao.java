package br.com.caelum.leilao.dominio;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class Leilao {

	private String descricao;
	private Calendar data;
	private List<Lance> lances;
	private boolean encerrado = false;
	private int id;
	
	private static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	
	public Leilao(String descricao) {
		this(descricao, Calendar.getInstance());
	}
	
	public Leilao(String descricao, Calendar data) {
		this.descricao = descricao;
		this.data = data;
		this.lances = new ArrayList<Lance>();
	}
	
	public void propoe(Lance lance) {
		if(lances.isEmpty() || podeDarLance(lance.getUsuario())) {
			lances.add(lance);
		}
	}

	private boolean podeDarLance(Usuario usuario) {
		return !ultimoLanceDado().getUsuario().equals(usuario) && qtdDeLancesDo(usuario) <5;
	}

	private int qtdDeLancesDo(Usuario usuario) {
		int total = 0;
		for(Lance l : lances) {
			if(l.getUsuario().equals(usuario)) total++;
		}
		return total;
	}

	private Lance ultimoLanceDado() {
		return lances.get(lances.size()-1);
	}

	public String getDescricao() {
		return descricao;
	}

	public List<Lance> getLances() {
		return Collections.unmodifiableList(lances);
	}

	public Calendar getData() {
		return (Calendar) data.clone();
	}

	public void encerra() {
		this.encerrado = true;
	}
	
	public boolean isEncerrado() {
		return encerrado;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Leilao [descricao=");
		builder.append(descricao);
		builder.append(", data=");
		builder.append(format.format(data.getTime()));
		builder.append(", lances=");
		builder.append(lances);
		builder.append(", encerrado=");
		builder.append(encerrado);
		builder.append(", id=");
		builder.append(id);
		builder.append("]");
		return builder.toString();
	}
}
