package minesql.util;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by srg
 *
 * @date 2017/11/27
 */
public class XlsUtils {

    public static void delLineNull(String infileName) {
        int key = 0;
        int MaxRowNum = 0, MaxCellNum = 0;
        try {
            FileInputStream in = new FileInputStream(infileName);
            POIFSFileSystem fs = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            FileOutputStream out = new FileOutputStream(infileName);
            int number = workbook.getNumberOfSheets();
            for (int i = 0; i < number; i++) { // 对每个sheet检查空行
                HSSFSheet sheet = workbook.getSheetAt(i); //14
//                System.out.println("正在对工作簿：" + sheet.getSheetName() + " 移除空行操作 行数"
//                        + (sheet.getLastRowNum() + 1));
                MaxRowNum = 0;
                for (int k = 0; k <= sheet.getLastRowNum(); k++) {
                    HSSFRow hRow = sheet.getRow(k);
                    //System.out.println((k + 1) + "行");
                    if (isBlankRow(hRow)) // 找到空行索引
                    {
                        int m = 0;
                        for (m = k + 1; m <= sheet.getLastRowNum(); m++) {
                            HSSFRow nhRow = sheet.getRow(m);

                            if (!isBlankRow(nhRow)) {
                                //System.out.println("下一个非空行" + (m + 1));
                                sheet.shiftRows(m, sheet.getLastRowNum(), k - m);
                                break;
                            }
                        }
                        if (m > sheet.getLastRowNum())
                            break; // 此工作簿完成
                    } else { //非空行
                        MaxRowNum++;
                        if (MaxCellNum < hRow.getLastCellNum())
                            MaxCellNum = hRow.getLastCellNum();
                    }
                }
                workbook.setPrintArea(i, 0, MaxCellNum, 0, MaxRowNum);
//                System.out.println("移除空行操作完成 " + sheet.getSheetName() + " 有效行数 " + MaxRowNum);
            }
            workbook.write(out);
            in.close();
            out.close();
        } catch (IOException e) {
//            System.out.println(key + " " + e.getMessage() + " ");
            e.printStackTrace();

        }

        System.out.println("移除空行操作完成");
    }

    /**
     * 判断excel 空行
     */
    public static boolean isBlankRow(HSSFRow row) {
        if (row == null)
            return true;
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            HSSFCell hcell = row.getCell(i);
            if (!isBlankCell(hcell))
                return false;
        }
        return true;
    }

    /**
     * 判断excel 空单元格
     */
    public static boolean isBlankCell(HSSFCell hcell) {
        if (hcell == null)
            return true;
        hcell.setCellType(hcell.CELL_TYPE_STRING);
        String content = hcell.getStringCellValue().trim();
        if (content == null || "".equals(content)) // 找到非空行
        {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        XlsUtils xlsUtils = new XlsUtils();
        xlsUtils.delLineNull("D:/code/sqlwork/minesql/database/test/test1.xls");
    }
}
