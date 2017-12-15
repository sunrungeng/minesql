package minesql.controller;

import minesql.common.Const;
import minesql.pojo.Table;
import minesql.pojo.User;
import minesql.util.UserUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellType;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by srg
 *
 * @date 2017/11/24
 */
public class IndexController {

    //create index unique indexName on tableName(sno desc) ... ;
    public void createIndex(User user, String databaseName, String str) {
        if (!UserUtils.judgePrivilege(user, "create")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        String[] strings = str.split(" ");
        String indexName = strings[3];
        String tableName = strings[5];
        tableName = tableName.substring(0,tableName.indexOf("("));
        //获取列名以及排序值
        int index1 = str.indexOf("("),index2 = str.indexOf(")");
        System.out.println(tableName);
        String columnAndOrder = str.substring(index1+1,index2);
        String[] strings1 = columnAndOrder.split(" ");
        String columnName = strings1[0],orderBy = "asc";
        if(strings1.length==2)orderBy = strings1[1];
        //判断是否已经存在这个文件夹,不存在就创建
        File dir = new File(Const.INDEX_LOAD + databaseName);
        ViewController.isDirExist(dir);
        File file = new File(Const.INDEX_LOAD+databaseName+"/"+indexName+".xls");
        File table = new File(Const.DATABASE_LOAD+databaseName+"/"+tableName+".xls");
        //判断是否存在这个表
        if(!table.exists()){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR+"The table is not existed");
            return;
        }
        //判断是否已经存在这个索引,存在就报错返回,不存在就创建
        if(file.exists()){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR+"The index is existed");
            return;
        }else{
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            InputStream in = new FileInputStream(table);
            POIFSFileSystem fin = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet = workbook.getSheetAt(0);
            List<List<String>> columnValueList = new ArrayList<>();
            int num = -1;
            for (int i = 0;i < sheet.getRow(0).getPhysicalNumberOfCells();i++){
                HSSFCell cell = sheet.getRow(0).getCell(i);
                if (cell.getStringCellValue().equals(columnName)){
                    break;
                }
            }
            //判断是否不存在这个列
            if(num == -1){
                System.out.println(Const.Error.SQL_SYNTAX_ERROR+"The column is not existed");
                return;
            }
            //判断是否为int型列  是否为uni 或 pri
            Table table1 = TableController.getTableProperty(table);
            if(!table1.getType().get(num).contains("int")){
                System.out.println(Const.Error.SQL_SYNTAX_ERROR+"The column is not int");
                return;
            }
            if(!table1.getKey().get(num).equals("UNI") && !table1.getKey().get(num).equals("PRI")){
                System.out.println(Const.Error.SQL_SYNTAX_ERROR+"The column is not unique");
                return;
            }
            //存的是Excel中的行数和数据  从1开始
            for(int i = 1;i <= sheet.getLastRowNum();i++){
                HSSFRow row = sheet.getRow(i);
                HSSFCell cell = row.getCell(num);
                String cellValue = cell.getStringCellValue();
                List<String> temp = new ArrayList<>();
                temp.add(cellValue);temp.add(Integer.toString(i));
                columnValueList.add(temp);
            }
            //对list排序
            if(orderBy.equals("asc")){
                Collections.sort(columnValueList, new Comparator<List<String>>() {
                    @Override
                    public int compare(List<String> o1, List<String> o2) {
                        return Integer.parseInt(o1.get(0))-Integer.parseInt(o2.get(0));
                    }
                });
            }else{
                Collections.sort(columnValueList, new Comparator<List<String>>() {
                    @Override
                    public int compare(List<String> o1, List<String> o2) {
                        return Integer.parseInt(o2.get(0))-Integer.parseInt(o1.get(0));
                    }
                });
            }
            //写入index.xls  并且第二个sheet存入创建的语句
            FileOutputStream fos = new FileOutputStream(file);
            HSSFWorkbook workbook1 = new HSSFWorkbook();
            HSSFSheet sheet1 = workbook1.getSheetAt(0);
            HSSFRow row = sheet1.createRow(0);
            HSSFCell cell = row.createCell(0);
            cell.setCellType(CellType.STRING);
            cell.setCellValue(columnName);
            HSSFCell cell1 = row.createCell(1);
            cell1.setCellType(CellType.STRING);
            cell1.setCellValue("no");
            for(int i = 1;i <= columnValueList.size();i++){
                HSSFRow row1 = sheet1.createRow(i);
                HSSFCell cell2 = row1.createCell(0);
                cell2.setCellType(CellType.STRING);
                cell2.setCellValue(columnValueList.get(i-1).get(0));
                HSSFCell cell3 = row1.createCell(1);
                cell3.setCellType(CellType.STRING);
                cell3.setCellValue(columnValueList.get(i-1).get(1));
            }
            HSSFSheet sheet2 = workbook1.getSheetAt(1);
            HSSFRow row2 = sheet2.createRow(0);
            HSSFCell cell2 = row2.createCell(0);
            cell2.setCellType(CellType.STRING);
            cell2.setCellValue(str);
            workbook1.write(fos);
            fos.close();
            fin.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void dropIndex(User user, String databaseName, String str) {
        if (!UserUtils.judgePrivilege(user, "drop")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        String[] strings = str.split(" ");
        String indexName = strings[2];
        File dir = new File(Const.INDEX_LOAD + databaseName);
        if(!dir.exists()){
            System.out.println(Const.Error.DATABASE_NOT_EXISTED);return;
        }
        File file = new File(Const.INDEX_LOAD + databaseName + "/" + indexName + ".xls");
        if(!file.exists()){
            System.out.println(Const.Error.INDEX_NOT_EXISTED);
            return;
        }
        file.delete();
        System.out.println(Const.COMMON_SUCCESS);
    }

    public void helpIndex(String databaseName, String str) {
        if(databaseName == null || databaseName.equals("")){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        String[] strings = str.split(" ");
        if(strings.length != 3){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
        String indexName = strings[2];
        File file = new File(Const.INDEX_LOAD+databaseName+"/"+indexName+".txt");
        try {
            InputStream in = new FileInputStream(file);
            POIFSFileSystem fin = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet = workbook.getSheetAt(1);
            HSSFRow row = sheet.getRow(0);
            HSSFCell cell = row.getCell(0);
            System.out.println(cell.getStringCellValue());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UserController userController = new UserController();
        User user = userController.login();
        IndexController indexController = new IndexController();
        indexController.createIndex(user,"test","create index unique indexName on test11(sno desc)");
    }
}
