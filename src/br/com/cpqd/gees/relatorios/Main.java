package br.com.cpqd.gees.relatorios;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.digester.Digester;
import org.xml.sax.SAXException;

import br.com.cpqd.gees.relatorios.modelo.Config;
import br.com.cpqd.gees.relatorios.modelo.Planilha;
import br.com.cpqd.gees.relatorios.modelo.Relatorios;
import br.com.cpqd.gees.relatorios.modelo.Tab;

import com.kenai.cronparser.CronTabExpression;

public class Main {

	/**
	 * @param args
	 * @throws SAXException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, SAXException {
		
		
		
		Relatorios relatorios = createConfig();

		Calendar nowCalendar = Calendar.getInstance();
		Connection cnn = null;
		Session mailSession = null;

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Date now = new Date();
		try {
			for (Planilha planilha : relatorios.getRelatorios()) {

				try {
					CronTabExpression expression = CronTabExpression
							.parse(planilha.getAgendamento());
					if (!expression.matches(nowCalendar)) {
						System.out.println("Pulando a execução do relatório "
								+ planilha.getNome());
						continue;
					}
				} catch (ParseException e) {
					System.out.println("Relatorio " + planilha.getNome()
							+ " está com erro na string do agendamento");
					e.printStackTrace();
					continue;
				}

				if (cnn == null) {
					try {
						Class.forName(relatorios.getConfig().getDriver());
					} catch (ClassNotFoundException e) {
						System.out
								.println("Erro Geral!!! não foi encontrada a classe : "
										+ relatorios.getConfig().getDriver());
						System.exit(-1);
					}
					try {
						cnn = DriverManager.getConnection(relatorios
								.getConfig().getUrl(), relatorios.getConfig()
								.getUsuario(), relatorios.getConfig()
								.getSenha());
						cnn.setAutoCommit(false);
					} catch (SQLException e) {
						System.out
								.println("Erro Geral!!! Foi possivel conectar em : "
										+ relatorios.getConfig().getUrl()
										+ " ("
										+ relatorios.getConfig().getUsuario()
										+ "/"
										+ relatorios.getConfig().getSenha()
										+ ")");
						e.printStackTrace();
						System.exit(-1);
					}

					GeradorPlanilha gerador = FactoryGerador
							.criaGerador(planilha);
					boolean enviarResultado = true;
					for (Tab aTab : planilha.getTabs()) {
						if (!gerador.addTab(aTab.getNome(), aTab.getConsulta(),
								cnn)) {
							if (!aTab.isEnviarSeVazio()) {
								System.out
										.println("Tab "
												+ aTab.getNome()
												+ " do relatorio "
												+ planilha.getNome()
												+ " retornou vazio... pulando o relatorio");
								enviarResultado = false;
								break;
							}

						}
					}
					

					if (!enviarResultado) {
						gerador.cancelar();
						continue;
					}
					gerador.finalizaRelatorio();
					
					if (mailSession == null) {
						Properties props = new Properties();
						props.put("mail.smtp.host", relatorios.getConfig()
								.getSmtpserver());
						mailSession = Session.getDefaultInstance(props, null);
						mailSession.setDebug(false);
					}

					Message msg = new MimeMessage(mailSession);

					try {
						InternetAddress from = new InternetAddress(relatorios
								.getConfig().getRemetente());
						msg.setFrom(from);

						List<String> destinatarios = planilha
								.getDestinatarios();
						InternetAddress[] to = new InternetAddress[destinatarios
								.size()];
						for (int i = 0; i < destinatarios.size(); i++) {
							to[i] = new InternetAddress(destinatarios.get(i));
						}
						msg.setRecipients(Message.RecipientType.TO, to);

						msg.setSentDate(new Date());
						msg.setSubject(planilha.getSubject() + sdf.format(now));
						MimeBodyPart body = new MimeBodyPart();
						body.setText(planilha.getBody());

						Multipart content = new MimeMultipart();

						content.addBodyPart(body);

						gerador.adicionaAnexos(content);

						msg.setContent(content);

						Transport.send(msg);
					} catch (MessagingException e) {
						System.out
								.println("Ocorreu o erro abaixo na criação e/ou envio do email com o resultado:");
						e.printStackTrace();
						System.exit(-1);
					}
				}

			}
		} finally {
			if (cnn != null) {
				try {
					cnn.rollback();
					cnn.close();
				} catch (SQLException ignored) {

				}
			}
			
		}
	}

	private static Relatorios createConfig() throws IOException, SAXException {
		Digester digester = new Digester();
		digester.setValidating(false);
		digester.addObjectCreate("relatorios", Relatorios.class);
		digester.addObjectCreate("relatorios/config", Config.class);
		//digester.addSetProperties("relatorios/config");
		digester.addCallMethod("relatorios/config/driver", "setDriver",0, new Class[]{String.class});
		digester.addCallMethod("relatorios/config/usuario", "setUsuario",0, new Class[]{String.class});
		digester.addCallMethod("relatorios/config/senha", "setSenha",0, new Class[]{String.class});
		digester.addCallMethod("relatorios/config/url", "setUrl",0, new Class[]{String.class});
		digester.addCallMethod("relatorios/config/remetente", "setRemetente",0, new Class[]{String.class});
		digester.addCallMethod("relatorios/config/smtpserver", "setSmtpserver",0, new Class[]{String.class});
		digester.addSetNext("relatorios/config", "setConfig");
		digester.addObjectCreate("relatorios/relatorio", Planilha.class);
		digester.addSetProperties("relatorios/relatorio");
		digester.addSetNext("relatorios/relatorio", "addPlanilha");
		digester.addCallMethod("relatorios/relatorio/destinatarios/destinatario",
				"addDestinatario", 0, new Class[]{String.class});
		digester.addCallMethod("relatorios/relatorio/body", "setBody",0, new Class[]{String.class});
		digester.addObjectCreate("relatorios/relatorio/tab", Tab.class);
		digester.addSetProperties("relatorios/relatorio/tab");
		digester.addCallMethod("relatorios/relatorio/tab/consulta",
				"setConsulta", 0, new Class[]{String.class});
		digester.addSetNext("relatorios/relatorio/tab", "addTab");

		
		
		
		InputStream in = Main.class.getResourceAsStream("/relatorios.xml");
		
		Relatorios relatorios = (Relatorios) digester.parse(in);
		return relatorios;
	}

}
