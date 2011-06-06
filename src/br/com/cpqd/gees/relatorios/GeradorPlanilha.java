package br.com.cpqd.gees.relatorios;

import java.sql.Connection;

import javax.mail.MessagingException;
import javax.mail.Multipart;

public interface GeradorPlanilha {

	/**
	 * 
	 * @param nome
	 * @param sql
	 * @param cnn
	 * @return se a consulta não retornou nenhum dado
	 */
	public boolean addTab(String nome, String sql, Connection cnn);
	
	public void finalizaRelatorio();
	
	public void cancelar();

	public void adicionaAnexos(Multipart content) throws MessagingException;
	
}
