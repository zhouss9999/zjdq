package wy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
@SuppressWarnings("all")

@Component
public class ReadExcel {
	/**
	 * Excel 2003
	 */
	private final static String XLS = "xls";
	/**
	 * Excel 2007
	 */
	private final static String XLSX = "xlsx";
	/**
	 * 分隔符
	 */
	private final static String SEPARATOR = "|";

	/**
	 * 由Excel文件的Sheet导出至List
	 * 
	 * @param file
	 * @param sheetNum
	 * @return
	 */
	public List<List<String>> exportListFromExcel(File file, int sheetNum)
			throws IOException {
		return exportListFromExcel(new FileInputStream(file),
				FilenameUtils.getExtension(file.getName()), sheetNum);
	}

	/**
	 * 由Excel流的Sheet导出至List
	 * 
	 * @param is
	 * @param extensionName
	 * @param sheetNum
	 * @return
	 * @throws IOException
	 */
	public List<List<String>> exportListFromExcel(InputStream is,
			String extensionName, int sheetNum) throws IOException {

		Workbook workbook = null;

		if (extensionName.toLowerCase().equals(XLS)) {
			workbook = new HSSFWorkbook(is);
		} else if (extensionName.toLowerCase().equals(XLSX)) {
			workbook = new XSSFWorkbook(is);
		}

		return exportListFromExcel(workbook, sheetNum);
	}

	/**
	 * 由指定的Sheet导出至List
	 * 
	 * @param workbook
	 * @param sheetNum
	 * @return
	 * @throws IOException
	 */
	private List<List<String>> exportListFromExcel(Workbook workbook,
			int sheetNum) {

		Sheet sheet = workbook.getSheetAt(sheetNum);

		// 解析公式结果
		FormulaEvaluator evaluator = workbook.getCreationHelper()
				.createFormulaEvaluator();

		List<List<String>> list = new ArrayList<List<String>>();

		int minRowIx = sheet.getFirstRowNum();
		int maxRowIx = sheet.getLastRowNum();
		System.out.println("minRowIx"+minRowIx+" maxRowIx "+maxRowIx);
		for (int rowIx = minRowIx; rowIx <= maxRowIx; rowIx++) {
			List colList = new ArrayList();
			Row row = sheet.getRow(rowIx);
			// StringBuilder sb = new StringBuilder();

			short minColIx = row.getFirstCellNum();
			short maxColIx = row.getLastCellNum();
			//System.out.println("minColIx "+minColIx+" maxColIx "+maxColIx);
			for (short colIx = minColIx; colIx < maxColIx; colIx++) {
				Cell cell = row.getCell(new Integer(colIx));
				CellValue cellValue = evaluator.evaluate(cell);  
				int cellType = cell.getCellType();
				if(cellType==Cell.CELL_TYPE_BOOLEAN) {  
                	colList.add(cellValue.getBooleanValue());  
				}
                if(cellType==Cell.CELL_TYPE_NUMERIC){
                    // 这里的日期类型会被转换为数字类型，需要判别后区分处理  
                    if (DateUtil.isCellDateFormatted(cell)) {  
                    	colList.add(cell.getDateCellValue());  
                    } else {  
                        //把手机号码转换为字符串  
                         DecimalFormat df = new DecimalFormat("#");  
                         colList.add(df.format(cellValue.getNumberValue()));  
                    }  
                 }
                if(cellType==Cell.CELL_TYPE_STRING) {
                	colList.add(cellValue.getStringValue());  
                }
                if(cellType!=Cell.CELL_TYPE_BOOLEAN && cellType!=Cell.CELL_TYPE_NUMERIC && cellType!=Cell.CELL_TYPE_STRING){
                	colList.add(cell.toString().trim());
                }
				//colList.add(cell.toString().trim());
			}
			//System.out.println("colList.size() "+colList.size());
			list.add(colList);
		}
		return list;
	}

}