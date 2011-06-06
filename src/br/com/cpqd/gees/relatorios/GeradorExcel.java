package br.com.cpqd.gees.relatorios;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import au.com.bytecode.opencsv.ResultSetHelper;
import au.com.bytecode.opencsv.ResultSetHelperService;
import br.com.cpqd.gees.relatorios.modelo.Planilha;

public class GeradorExcel implements GeradorPlanilha {

	private Planilha planilha;
	private WritableWorkbook workbook;
	private File tempFile;

	public GeradorExcel(Planilha planilha) {
		super();
		this.planilha = planilha;
		WorkbookSettings settings = new WorkbookSettings();
		settings.setTemporaryFileDuringWriteDirectory(new File(System
				.getProperties().getProperty("java.io.tmpdir")));
		settings.setUseTemporaryFileDuringWrite(true);
		try {
			tempFile = File.createTempFile("rel", ".xls");
		} catch (IOException e) {
			System.out
					.println("Não foi possivel criar arquivo temporario, verifique a opcao java.io.tmpdir");
			System.exit(-1);
		}

		try {
			workbook = Workbook.createWorkbook(tempFile);
		} catch (IOException e) {
			System.out
					.println("Não foi possivel criar uma planilha temporaria, verifique o erro abaixo:");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public boolean addTab(String nome, String sql, Connection cnn) {
		ResultSet resultSet = null;
		Statement stmt = null;
		boolean retornouDado = false;
		try {
			WritableSheet createSheet = workbook.createSheet(nome,
					workbook.getNumberOfSheets() + 1);
			stmt = cnn.createStatement();
			resultSet = stmt.executeQuery(sql);
			BorderLineStyle borderLineStyle = BorderLineStyle.THIN;
			WritableFont headerFont = new WritableFont(WritableFont.ARIAL);
			headerFont.setBoldStyle(WritableFont.BOLD);
			headerFont.setPointSize(12);
			WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
			headerFormat.setAlignment(Alignment.CENTRE);
			headerFormat.setBackground(Colour.GRAY_25);
			headerFormat.setBorder(Border.ALL, borderLineStyle);

			ResultSetHelper helper = new ResultSetHelperService();
			String[] columnNames = helper.getColumnNames(resultSet);

			
			CellView[] headerColumnView = new CellView[columnNames.length];
			for (int i = 0; i < headerColumnView.length; i++) {
				headerColumnView[i] = new CellView();
				headerColumnView[i].setSize(0);
			}
			
			for (int i = 0; i < columnNames.length; i++) {
				Label label = new Label(i, 0, columnNames[i]);
				headerColumnView[i].setSize(columnNames[i].length() + 10);
				label.setCellFormat(headerFormat);
				createSheet.addCell(label);
				createSheet.setRowView(0, 600);
			}
			
			String[] values = null;
			int counter = 1;

			WritableFont dataFont = new WritableFont(WritableFont.ARIAL);
			dataFont.setPointSize(10);
			
			WritableCellFormat dataFormat = new WritableCellFormat(dataFont);
			WritableCellFormat integerFormat = new WritableCellFormat(dataFont,
					NumberFormats.INTEGER);
			WritableCellFormat floatFormat = new WritableCellFormat(dataFont,
					NumberFormats.FLOAT);

			integerFormat.setBorder(Border.ALL, borderLineStyle);
			dataFormat.setBorder(Border.ALL, borderLineStyle);
			floatFormat.setBorder(Border.ALL,borderLineStyle);

			while (resultSet.next()) {
				values = helper.getColumnValues(resultSet);

				retornouDado = true;
				for (int i = 0; i < values.length; i++) {
					Number number = null;
					WritableCellFormat currentFormat = null;
					switch (resultSet.getMetaData().getColumnType(i + 1)) {
					case Types.BIGINT:
						number = new Number(i, counter, resultSet.getLong(i+ 1));
					case Types.INTEGER:
					case Types.TINYINT:
					case Types.SMALLINT:
						number = number != null ? number : new Number(i,
								counter, resultSet.getInt(i+ 1));
						currentFormat = integerFormat;
					case Types.DECIMAL:
						number = number != null ? number : new Number(i,
								counter, resultSet.getDouble(i+ 1));
					case Types.DOUBLE:
						number = number != null ? number : new Number(i,
								counter, resultSet.getDouble(i+ 1));
					case Types.FLOAT:
						number = number != null ? number : new Number(i,
								counter, resultSet.getFloat(i+ 1));
					case Types.REAL:
						number = number != null ? number : new Number(i,
								counter, resultSet.getDouble(i+ 1));
					case Types.NUMERIC:
						number = number != null ? number : new Number(i,
								counter, resultSet.getDouble(i+ 1));
						currentFormat = currentFormat != null ? currentFormat
								: dataFormat;
						number.setCellFormat(currentFormat);
						createSheet.addCell(number);
						break;
					default:
						Label label = new Label(i, counter, values[i]);
						label.setCellFormat(dataFormat);
						createSheet.addCell(label);
					}
					if(headerColumnView[i].getSize() < values[i].length() ){
						headerColumnView[i].setSize(values[i].length());
					}
				}
				counter++;
			
			}
			for (int i = 0; i < columnNames.length; i++) {
				headerColumnView[i].setSize(headerColumnView[i].getSize() * 256);
				createSheet.setColumnView(i, headerColumnView[i]);
			}
		} catch (SQLException e) {
			System.out.println("Ocorreu um erro ao trazer os dados do banco:");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException ignored) {

		} catch (RowsExceededException e) {
			System.out.println("No relatório " + planilha.getNome() + " a aba "
					+ nome + " retornou muitos registros... ");
			System.exit(-1);
		} catch (WriteException e) {
			System.out.println("Ocorreu um erro ao gravar a planilha Excel:");
			e.printStackTrace();
			System.exit(-1);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ignored) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException ignored) {
				}
			}
		}
		return retornouDado;
	}

	public void finalizaRelatorio() {
		try {
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			System.out.println("Ocorreu um erro ao gravar na área temporária");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void cancelar() {
		finalizaRelatorio();
		tempFile.delete();
	}

	public void adicionaAnexos(Multipart content) throws MessagingException {
		MimeBodyPart att = new MimeBodyPart();
		DataSource ds = new FileDataSource(tempFile);
		att.setDataHandler(new DataHandler(ds));
		att.setFileName(planilha.getNome() + ".xls");
		content.addBodyPart(att);
	}

}
