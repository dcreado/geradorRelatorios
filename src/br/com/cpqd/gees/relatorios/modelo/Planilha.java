package br.com.cpqd.gees.relatorios.modelo;

import java.util.LinkedList;
import java.util.List;

public class Planilha {

	private String nome;
	private String formato;
	private List<Tab> tabs = new LinkedList<Tab>();
	private String agendamento;
	private List<String> destinatarios = new LinkedList<String>();
	private String subject;
	private String body;
	
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getAgendamento() {
		return agendamento;
	}
	public void setAgendamento(String agendamento) {
		this.agendamento = agendamento;
	}
	public List<String> getDestinatarios() {
		return destinatarios;
	}
	public void setDestinatarios(List<String> destinatarios) {
		this.destinatarios = destinatarios;
	}
	
	public void addDestinatario(String destinatario){
		destinatarios.add(destinatario);
	}
	
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getFormato() {
		return formato;
	}
	public void setFormato(String formato) {
		this.formato = formato;
	}
	public List<Tab> getTabs() {
		return tabs;
	}
	public void setTabs(List<Tab> tabs) {
		this.tabs = tabs;
	}
	
	
	public void addTab(Tab novaTab){
		tabs.add(novaTab);
	}
}
