package br.com.cpqd.gees.relatorios.modelo;

import java.util.LinkedList;
import java.util.List;

public class Relatorios {

	private Config config;
	
	private List<Planilha> relatorios = new LinkedList<Planilha>();

	public List<Planilha> getRelatorios() {
		return relatorios;
	}

	public void setRelatorios(List<Planilha> relatorios) {
		this.relatorios = relatorios;
	}
	
	public void addPlanilha(Planilha planilha){
		relatorios.add(planilha);
	}

	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
}
