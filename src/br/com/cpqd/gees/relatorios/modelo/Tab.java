package br.com.cpqd.gees.relatorios.modelo;

public class Tab {

	private String nome;
	private String consulta;
	private boolean enviarSeVazio = true;

	public boolean isEnviarSeVazio() {
		return enviarSeVazio;
	}

	public void setEnviarSeVazio(boolean enviarSeVazio) {
		this.enviarSeVazio = enviarSeVazio;
	}

	public void setEnviarSeVazio(String enviarSeVazio) {
		this.enviarSeVazio = Boolean.parseBoolean(enviarSeVazio.toLowerCase());
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getConsulta() {
		return consulta;
	}

	public void setConsulta(String consulta) {
		this.consulta = consulta;
	}

}
