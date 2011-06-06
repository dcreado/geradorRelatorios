package br.com.cpqd.gees.relatorios;

import br.com.cpqd.gees.relatorios.modelo.Planilha;

public class FactoryGerador {

	
	public static GeradorPlanilha criaGerador(Planilha planilha){
		if("xls".equalsIgnoreCase(planilha.getFormato()) || "excel".equalsIgnoreCase(planilha.getFormato())){
			return new GeradorExcel(planilha);
		}
		System.out.println( planilha.getNome() + ": formato invalido");
		System.exit(-1);
		return null;
	}
}
