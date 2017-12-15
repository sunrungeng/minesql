package minesql.controller;

import minesql.common.Const;
import minesql.pojo.Table;
import minesql.pojo.User;
import minesql.util.FileUtils;
import minesql.util.StringUtils;
import minesql.util.UserUtils;
import minesql.util.XlsUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by srg
 *
 * @date 2017/11/20
 */

public class TableController {
    public TableController() {
    }

    public void createTable(User user, String databaseName, String createStr) {
        if (!UserUtils.judgePrivilege(user, "create")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        createStr = createStr.replaceAll("'", "");
        int theFirstBracket = createStr.indexOf("(");
        int theLastBracket = createStr.lastIndexOf(")");

        //取出正确表名
        String tableName = null;
        String frontCreate = createStr.substring(0, theFirstBracket);
        String[] frontCreateStrings = frontCreate.split(" ");
        if (frontCreateStrings[0].equals("create")) {
            if (frontCreateStrings[1].equals("table")) {
                tableName = frontCreateStrings[2];
                //如果表已存在，返回
                if (this.isTableExists(databaseName, tableName)) {
                    System.out.println("ERROR : Table '" + tableName + "' already exists");
                    return;
                }
//                System.out.println(tableName);
            } else {
                System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                return;
            }
        } else {
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }

        //取出表的构造语句并写入xls文件
        File table = new File("database/" + databaseName + "/" + tableName + ".xls");
        OutputStream fos = null;
        String createString = createStr.substring(theFirstBracket + 1, theLastBracket);
//        System.out.println(createString);
        String[] createStrings = createString.split(",");
        int count = 0;

        // 创建工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表
        HSSFSheet sheet1 = workbook.createSheet("sheet1");
        HSSFSheet sheet2 = workbook.createSheet("sheet2");

        HSSFRow row1 = sheet1.createRow(0);
        HSSFRow rows2 = sheet2.createRow(0);
        rows2.createCell(0).setCellValue("Field");
        rows2.createCell(1).setCellValue("Type");
        rows2.createCell(2).setCellValue("Null");
        rows2.createCell(3).setCellValue("Key");
        rows2.createCell(4).setCellValue("Default");
        rows2.createCell(5).setCellValue("Check");
//        rows2.createCell(5).setCellValue("Extra");
        try {
            for (String stringItem : createStrings) {
                stringItem = stringItem.trim();
                String[] columnString = stringItem.split(" ");
                //检测主键
                if (stringItem.startsWith("primary key")) {
                    String columnName = StringUtils.getStringInBracket(stringItem);
                    int numOfColumn = StringUtils.getNumByColumnName(createStrings, columnName);
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(3).setCellValue("PRI");
                    continue;
                }
                //检测唯一性
                if (stringItem.startsWith("unique")) {
                    String columnName = StringUtils.getStringInBracket(stringItem);
                    int numOfColumn = StringUtils.getNumByColumnName(createStrings, columnName);
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(3).setCellValue("UNI");
                    continue;
                }
                //检测check
                if (stringItem.startsWith("check")) {
                    String columnName = StringUtils.getStringInBracket(stringItem);
                    int numOfColumn = StringUtils.getNumOfColumnInCreate(createStrings, columnName);
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(5).setCellValue(columnName);
                    continue;
                }
                //外键
                if (stringItem.startsWith("foreign key")) {
                    String[] strings = stringItem.split(" ");
                    String foreignKeyColumn = StringUtils.getStringInBracket(strings[2]);
                    int numOfColumn = StringUtils.getNumOfColumnInCreate(createStrings, foreignKeyColumn);
                    String foreignKey = "FOR " + strings[4];
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(3).setCellValue(foreignKey);
                    continue;
                }
                HSSFRow rows3 = sheet2.createRow(count + 1);
                row1.createCell(count).setCellValue(columnString[0]);
                rows3.createCell(0).setCellValue(columnString[0]);
                rows3.createCell(1).setCellValue(columnString[1]);

                if (stringItem.contains("primary key") || stringItem.contains("not null") || stringItem.contains("unique key")) {
                    rows3.createCell(2).setCellValue("NO");
                    if (stringItem.contains("primary key")) {
                        rows3.createCell(3).setCellValue("PRI");
                    }
                } else {
                    rows3.createCell(2).setCellValue("YES");
                }

                if (stringItem.contains("unique")) {
                    rows3.createCell(3).setCellValue("UNI");
                }

                if (stringItem.contains("foreign key")) {
                    int indexOfForeign = stringItem.indexOf("foreign key");
                    String str = stringItem.substring(indexOfForeign, stringItem.length());
                    String[] strings = str.split(" ");
                    String foreignKeyString = "FOR " + strings[1];
                    rows3.createCell(3).setCellValue(foreignKeyString);
                }

                if (stringItem.contains("default")) {
                    int index = stringItem.indexOf("default");
                    int theLeftSpace = stringItem.indexOf(" ", index);
                    int theRightSpace = stringItem.indexOf(" ", theLeftSpace + 1);
                    String defaultValue = null;
                    if (theRightSpace != -1) {
                        defaultValue = stringItem.substring(theLeftSpace + 1, theRightSpace);
                    } else {
                        defaultValue = stringItem.substring(theLeftSpace + 1, stringItem.length());
                    }

                    rows3.createCell(4).setCellValue(defaultValue);
                }
                count++;
            }

            // 写入数据
            fos = new FileOutputStream(table);
            workbook.write(fos);
            fos.close();
            System.out.println(Const.COMMON_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //create table
    }

    public void dropTable(User user, String databaseName, String dropString) {
        if (!UserUtils.judgePrivilege(user, "create")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(dropString);
            Drop drop = (Drop) statement;
            String tableName = drop.getName().toString();
            if (!this.isTableExists(databaseName, tableName)) {
                System.out.println("ERROR : Unknown table '" + tableName + "'");
                return;
            } else {
                File file = new File(Const.DATABASE_LOAD + databaseName + "/" + tableName + ".xls");
                file.delete();
            }

        } catch (JSQLParserException e) {
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
    }

    public static void main(String[] args) {
        TableController tableController = new TableController();
        UserController userController = new UserController();
        User user = userController.login();
//        for(int i = 4;i <= 10;i++){
//            tableController.createTable(user,"test","create table test"+i+"(sno int(20) default 20,sname char(20),sex char(10) not null,primary key(sno),unique(sname),check(sno>0),foreign key (sex) references person(sex))");
//        }
//        tableController.insertTableData(user,"test","insert test10 values(20,adfa,male),(30,srg,female)");
//        tableController.deleteTableData(user,"test","delete from test12 where sname = srg");
//        tableController.dropTable("test","drop table test1");
        tableController.select(user, "school", "select sname from student left join teacher on student.sno = teacher.tno;");
//        tableController.showTables("test");
//        tableController.helpTable("test","help table test11");
//        tableController.updateTableData(user,"test","update test11 set sno = 1,sex = ss where sname = srg");

//        List<Integer> ll = tableController.getResultRowListByWhere(new File("database/test/test10.xls"),"sname like 'srg' and sex like 'male'");
//        for(int i = 0;i < ll.size();i++){
//            System.out.println(ll.get(i));
//        }
//        System.out.println(ll.size());
    }

    public void insertTableData(User user, String databaseName, String insertString) {
        if (!UserUtils.judgePrivilege(user, "insert")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(insertString);
            Insert insert = (Insert) statement;
            String tableName = this.insert_table(insertString);
            List<Column> insertColumns = insert.getColumns();
//            System.out.println(tableName);

            File file1 = new File(Const.DATABASE_LOAD + databaseName + "/" + tableName + ".xls");
            File file2 = new File(Const.DATABASE_LOAD + databaseName + "/" + tableName + "cp.xls");
            FileUtils.copyFile(file1, file2);

            InputStream in = new FileInputStream(file1);
            POIFSFileSystem fin = new POIFSFileSystem(in);

            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet1 = workbook.getSheetAt(0);
            //得到这个表的所有列的属性,id从0开始
            Table table = getTableProperty(file2);
            List<Integer> no = table.getNo();
            List<String> field = table.getField();
            List<String> type = table.getType();
            List<String> isNull = table.getIsNull();
            List<String> key = table.getKey();
            List<String> defaultValue = table.getDefaultValue();
            List<String> check = table.getCheck();

            //要插入的数据的列数列表
            List<Integer> numOfColumnList = new ArrayList<Integer>();
            if (insertColumns != null) {
                for (int i = 0; i < insertColumns.size(); i++) {
                    String insertColumnName = insertColumns.get(i).getColumnName();
                    int j = 0;
                    for (; j < field.size(); j++) {
                        if (insertColumnName.equals(field.get(j))) {
                            numOfColumnList.add(j);
                            break;
                        }
                    }
                    if (j == field.size()) {
                        System.out.println("ERROR : Unknown column '" + insertColumnName + "' in 'field list'");
                        file2.delete();
                        return;
                    }
                }
            } else {
                for (int i = 0; i < no.size(); i++) {
                    numOfColumnList.add(i);
                }
            }

            //todo foreign key check
            List<List<String>> valuesList = this.insert_values(insertString, numOfColumnList.size());
            if (valuesList == null) {
                System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            }
            int num = sheet1.getLastRowNum() + 1;
            for (int i = 0; i < valuesList.size(); i++) {
                Row row = sheet1.createRow(num++);
                for (int j = 0, k = 0; k < sheet1.getRow(0).getPhysicalNumberOfCells(); k++) {
                    Cell cell = row.createCell(k, CellType.STRING);
                    if (numOfColumnList.get(j) == k) {
                        //判断主键和唯一约束
                        if (key.get(k).equals("PRI") || key.get(k).equals("UNI")) {
                            List<String> columnValue = getColumnValue(file2, k);
                            if (columnValue.contains(valuesList.get(i).get(j))) {
                                System.out.println(Const.Error.VALUE_EXISTED);
                                file2.delete();
                                return;
                            }
                        }
                        //判断type
                        if (type.get(k).contains("int")) {
                            String data = valuesList.get(i).get(j);
                            if (!StringUtils.isNumeric(data)) {
                                System.out.println(Const.Error.VALUE_TYPE_ERROR);
                                file2.delete();
                                return;
                            }
                        }

                        cell.setCellValue(valuesList.get(i).get(j));
                        j++;
                        //若j以后的列都没在numOfColumnList里面,将j--,使其进入else判断是否可以为空值
                        if (j == numOfColumnList.size())
                            j--;
                    } else {
                        //null
                        if (isNull.get(k).equals("YES")) {
                            cell.setCellValue("");
                        } else {
                            //default
                            if (!defaultValue.get(k).equals("")) {
                                cell.setCellValue(defaultValue.get(k));
                            } else {
                                System.out.println("ERROR : the '" + field.get(k) + "' is not null");
                                file2.delete();
                                return;
                            }
                        }
                    }
                }
            }
            FileOutputStream fos = new FileOutputStream(file1);
            workbook.write(fos);
            fos.close();
            fin.close();
            in.close();
            file2.delete();
            XlsUtils.delLineNull(Const.DATABASE_LOAD + databaseName + "/" + tableName + ".xls");
            System.out.println(Const.COMMON_SUCCESS);
        } catch (JSQLParserException e) {
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //得到第k列的所有值
    private List<String> getColumnValue(File file2, int k) {
        List<String> columnValue = new ArrayList<>();
        try {
            InputStream in = new FileInputStream(file2);
            POIFSFileSystem fin = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                HSSFRow row = sheet.getRow(i);
                HSSFCell cell = row.getCell(k);
                if (cell == null)
                    columnValue.add("");
                else
                    columnValue.add(cell.getStringCellValue());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return columnValue;
    }

    //得到table的全部值 返回List<List<String>>
    private static List<List<String>> getTableValue(File file) {
        List<List<String>> tableValueList = new ArrayList<>();
        try {
            InputStream in = new FileInputStream(file);
            POIFSFileSystem fin = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                List<String> columnValueList = new ArrayList<>();
                HSSFRow row = sheet.getRow(i);
                for (int j = 0; j < sheet.getRow(0).getPhysicalNumberOfCells(); j++) {
                    HSSFCell cell = row.getCell(j);
                    if (cell == null || cell.getStringCellValue().equals("")) {
                        columnValueList.add("");
                    } else {
                        columnValueList.add(cell.getStringCellValue());
                    }
                }
                tableValueList.add(columnValueList);
            }
            fin.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return tableValueList;
        }
    }

    //得到table的全部属性 封装到table中,并返回
    public static Table getTableProperty(File file2) {
        try {
            FileInputStream in = new FileInputStream(file2);
            POIFSFileSystem fin = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet = workbook.getSheetAt(1);
            List<Integer> no = new ArrayList<Integer>();
            List<String> field = new ArrayList<String>();
            List<String> type = new ArrayList<String>();
            List<String> isNull = new ArrayList<String>();
            List<String> key = new ArrayList<String>();
            List<String> defaultValue = new ArrayList<String>();
            List<String> check = new ArrayList<String>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                no.add(i - 1);
                HSSFRow row = sheet.getRow(i);

                HSSFCell cell0 = row.getCell(0);
                if (cell0 == null) {
                    field.add("");
                } else {
                    String value0 = cell0.getStringCellValue();
                    field.add(value0);
                }

                HSSFCell cell1 = row.getCell(1);
                if (cell1 == null) {
                    type.add("");
                } else {
                    String value1 = cell1.getStringCellValue();
                    type.add(value1);
                }

                HSSFCell cell2 = row.getCell(2);
                if (cell2 == null) {
                    isNull.add("");
                } else {
                    String value2 = cell2.getStringCellValue();
                    isNull.add(value2);
                }

                HSSFCell cell3 = row.getCell(3);
                if (cell3 == null) {
                    key.add("");
                } else {
                    String value3 = cell3.getStringCellValue();
                    key.add(value3);
                }

                HSSFCell cell4 = row.getCell(4);
                if (cell4 == null) {
                    defaultValue.add("");
                } else {
                    String value4 = cell4.getStringCellValue();
                    defaultValue.add(value4);
                }

                HSSFCell cell5 = row.getCell(5);
                if (cell5 == null) {
                    check.add("");
                } else {
                    String value5 = cell5.getStringCellValue();
                    check.add(value5);
                }
            }

            Table table = new Table();
            table.setNo(no);
            table.setField(field);
            table.setType(type);
            table.setIsNull(isNull);
            table.setKey(key);
            table.setDefaultValue(defaultValue);
            table.setCheck(check);

            fin.close();
            in.close();

            return table;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取表名
    public static String insert_table(String sql)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insertStatement = (Insert) statement;
        String string_tablename = insertStatement.getTable().getName();
        return string_tablename;
    }

    //获取增加的值,支持多行添加
    public static List<List<String>> insert_values(String sql, int size) {
        int indexOfValues = sql.indexOf("values");
        int beginIndex = sql.indexOf("(", indexOfValues);
        int endIndex = sql.indexOf(")", beginIndex);
        String values = sql.substring(beginIndex + 1, endIndex);
        List<List<String>> valuesList = new ArrayList<>();
        while (endIndex != -1) {
            String[] str_values = values.split(",");
            if (str_values.length != size) {
                return null;
            }
            List<String> stringList = new ArrayList<String>();
            for (int i = 0; i < str_values.length; i++) {
                stringList.add(str_values[i]);
            }
            valuesList.add(stringList);
            beginIndex = sql.indexOf("(", beginIndex + 1);
            endIndex = sql.indexOf(")", endIndex + 1);
            if (beginIndex == -1 || endIndex == -1) {
                break;
            }
            values = sql.substring(beginIndex + 1, endIndex);
        }
        return valuesList;
    }

    public void deleteTableData(User user, String databaseName, String deleteString) {
        if (!UserUtils.judgePrivilege(user, "delete")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            deleteString = deleteString.toLowerCase();
            deleteString = deleteString.replaceAll("'", "");
            Statement statement = CCJSqlParserUtil.parse(deleteString);
            Delete delete = (Delete) statement;
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(delete);

            String tableName = tableList.get(0);
            if (!this.isTableExists(databaseName, tableName)) {
                System.out.println("ERROR : Table '" + databaseName + "." + tableName + "' doesn't exist");
                return;
            }

            String filePath = "database/" + databaseName + "/" + tableName + ".xls";
            File file = new File(filePath);
            FileInputStream in = new FileInputStream(file);
            POIFSFileSystem fs = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            HSSFSheet sheet = workbook.getSheetAt(0);

            List<Integer> numberOfDeleteRow = new ArrayList<Integer>();
            if (deleteString.contains("where")) {
                List<String> columnList = new ArrayList<String>();
                for (int k = 0; k < sheet.getRow(0).getPhysicalNumberOfCells(); k++) {
                    Cell cell = sheet.getRow(0).getCell(k);
                    columnList.add(cell.getStringCellValue());
                    System.out.println(cell.getStringCellValue());
                }
                Expression where_expression = delete.getWhere();
                String whereString = where_expression.toString();
                numberOfDeleteRow = getResultRowListByWhere(file,whereString);
            } else {
                int numOfColumn = sheet.getLastRowNum();
                for (int i = 1; i <= numOfColumn; i++) {
                    numberOfDeleteRow.add(i);
                }
            }
            for (int i = 0; i < numberOfDeleteRow.size(); i++) {
                Row row = sheet.createRow(numberOfDeleteRow.get(i));
            }
            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
//            fs.close();
            in.close();
            XlsUtils.delLineNull(filePath);

        } catch (JSQLParserException e) {
//            e.printStackTrace();
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
        } catch (FileNotFoundException e) {
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateTableData(User user, String databaseName, String updateString) {
        if (!UserUtils.judgePrivilege(user, "update")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(updateString);
            Update update = (Update) statement;
            //表名
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(update);
            //todo 目前单表
            String tableName = tableList.get(0);
            if (!isTableExists(databaseName, tableName)) {
                System.out.println("ERROR : Table '" + databaseName + "." + tableName + "' doesn't exist");
                return;
            }
            //得到set的column
            List<Column> columnList = update.getColumns();
            List<String> str_columnList = new ArrayList<String>();
            if (columnList != null) {
                for (int i = 0; i < columnList.size(); i++) {
                    str_columnList.add(columnList.get(i).toString());
                    System.out.println(columnList.get(i).toString());
                }
            }
            //得到set的列的下标
            List<Integer> numOfUpdateColList = this.getNumOfColList(databaseName, tableName, str_columnList);
            List<Expression> update_values = update.getExpressions();
            List<String> str_values = new ArrayList<String>();
            if (update_values != null) {
                for (int i = 0; i < update_values.size(); i++) {
                    str_values.add(update_values.get(i).toString());
                }
            }

            File file1 = new File(Const.DATABASE_LOAD + databaseName + "/" + tableName + ".xls");
            File file2 = new File(Const.DATABASE_LOAD + databaseName + "/" + tableName + "cp.xls");

            FileUtils.copyFile(file1, file2);
            InputStream in = new FileInputStream(file2);
            FileOutputStream fos = new FileOutputStream(file1);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(0);
            List<Integer> numOfResultRow = new ArrayList<Integer>();
            List<String> allColumnList = new ArrayList<String>();
            for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
                Cell cell = sheet.getRow(0).getCell(i);
                cell.setCellType(CellType.STRING);
                allColumnList.add(cell.getStringCellValue());
            }
            //where
            if (updateString.contains("where")) {
                Expression where_expression = update.getWhere();
                String where = where_expression.toString();
                //获取where表达式  获取sno 所在列数  获取符合条件的行数 添加到numOfResultRow
                numOfResultRow = getResultRowListByWhere(file2,where);
            } else {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    numOfResultRow.add(i);
                }
            }

            for (int i = 0; i < numOfResultRow.size(); i++) {
                Row row = sheet.getRow(numOfResultRow.get(i));
                for (int j = 0, k = 0; k < allColumnList.size(); k++) {
                    if (k != numOfUpdateColList.get(j)) {
//                        row.createCell(k).setCellValue(row.getCell(k).getStringCellValue());
                    } else {
                        row.getCell(k).setCellValue(str_values.get(j));
                        j++;
                        if (j == numOfUpdateColList.size()) {
                            break;
                        }
                    }
                }
            }
            file2.delete();
            workbook.write(fos);
            fos.close();
            in.close();
        } catch (JSQLParserException e) {
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void select(User user,String databaseName, String sql){
        if (!UserUtils.judgePrivilege(user, "select")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        //构造rowData,columnNames,最终将结果传入printTable打印
        List<List<String>> rowData = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        //实例化select对象,解析sql语句
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(sql);
            Select select = (Select) statement;
            List<String> tableList = TableController.test_select_table(sql);
//            for (String s : tableList){
//                System.out.println(s);
//            }
            if(tableList == null){
                System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                return;
            } else if (tableList.size() == 1){
                TableController.selectTableData(user, databaseName, sql);
                return;
            } else if (tableList.size() > 1){
                String tableName1 = tableList.get(0);
                String tableName2 = tableList.get(1);
                List<String> selectItems = TableController.test_select_items(sql);
                //连接
                if(sql.contains("join")){
                    File file1 = new File(Const.DATABASE_LOAD+databaseName+"/"+tableName1+".xls");
                    File file2 = new File(Const.DATABASE_LOAD+databaseName+"/"+tableName2+".xls");
                    Table table1 = getTableProperty(file1);
                    Table table2 = getTableProperty(file2);
                    List<String> columnNames1 = table1.getField();
                    List<String> columnNames2 = table2.getField();
                    List<Integer> numOfCol = new ArrayList<>();
                    if(selectItems.get(0) != "*"){
                        for (String s : selectItems){
                            columnNames.add(s);
                            numOfCol.add((Integer) columnNames1.indexOf(s));
                        }
                    }
                    List<List<String>> tableValue1 = getTableValue(file1);
                    List<List<String>> tableValue2 = getTableValue(file2);
                    List<String> join = TableController.test_select_join(sql);
                    String sqlstring = join.get(0).substring(join.get(0).indexOf("ON") + 2,join.get(0).length()).trim();
//                    System.out.println(sqlstring);
                    String[] strings = null;
                    if(sqlstring.contains("AND")){
                        strings = sqlstring.split("AND");
                    } else {
                        strings = sqlstring.split("OR");
                    }

                    List<Integer> numOfResultRow = new ArrayList<>();
                    for(int i = 0;i < strings.length;i++){
                        List<Integer> temp = new ArrayList<>();
                        String tempstr = strings[i].trim();
                        String[] columns = null;
                        if(tempstr.contains(">=")){
                            columns = tempstr.split(">=");
                        } else if (tempstr.contains("<=")){
                            columns = tempstr.split("<=");
                        } else if (tempstr.contains("=")){
                            columns = tempstr.split("=");
                        } else if (tempstr.contains(">")){
                            columns = tempstr.split(">");
                        } else if (tempstr.contains("<")){
                            columns = tempstr.split(">");
                        }
                        String column1 = columns[0].trim(),column2 = columns[1].trim();
                        List<String> values1 = new ArrayList<>(),values2 = new ArrayList<>();
                        if(!column1.contains(".")){
                            if(columnNames1.contains(column1)){
                                values1 = tableValue1.get(columnNames1.indexOf(column1));
                            } else if (columnNames2.contains(column1)){
                                values1 = tableValue2.get(columnNames2.indexOf(column1));
                            } else {
                                values1.add(column1);
                                return;
                            }
                        } else {
                            String[] strings1 = column1.split("\\.");
//                            System.out.println(strings1[1]);
                            if(columnNames1.contains(strings1[1])){
                                values1 = tableValue1.get(columnNames1.indexOf(strings1[1]));
                            } else if(columnNames2.contains(strings1[1])){
                                values1 = tableValue2.get(columnNames2.indexOf(strings1[1]));
                            }
                        }
                        if(!column2.contains(".")){
                            if(columnNames1.contains(column2)){
                                values2 = tableValue1.get(columnNames1.indexOf(column1));
                            } else if (columnNames2.contains(column2)){
                                values2 = tableValue1.get(columnNames2.indexOf(column1));
                            } else {
                                values2.add(column2);
                                return;
                            }
                        } else {
                            String[] strings1 = column1.split("\\.");
                            if(columnNames1.contains(strings1[1])){
                                values2 = tableValue1.get(columnNames1.indexOf(strings1[1]));
                            } else if(columnNames2.contains(strings1[1])){
                                values2 = tableValue2.get(columnNames2.indexOf(strings1[1]));
                            }
                        }
                        for(int k = 0;k < values1.size();k++){
                            for (int j = 0;j < values2.size();j++){
                                if(tempstr.contains(">=")){
                                    if(Integer.parseInt(values1.get(k)) >= Integer.parseInt(values2.get(j))){
                                        temp.add(k);
                                    }
                                } else if (tempstr.contains("<=")){
                                    if(Integer.parseInt(values1.get(k)) <= Integer.parseInt(values2.get(j))){
                                        temp.add(k);
                                    }
                                } else if (tempstr.contains("=")){
                                    if(values1.get(k).equals(values2.get(j))){
                                        temp.add(k);
                                    }
                                } else if (tempstr.contains(">")){
                                    if(Integer.parseInt(values1.get(k)) > Integer.parseInt(values2.get(j))){
                                        temp.add(k);
                                    }
                                } else if (tempstr.contains("<")){
                                    if(Integer.parseInt(values1.get(k)) < Integer.parseInt(values2.get(j))){
                                        temp.add(k);
                                    }
                                }
                            }
                        }
                        if (i == 0) {
                            numOfResultRow = temp;
                        } else if(sqlstring.contains("AND")){
                            int x = numOfResultRow.size();
                            for (int j = 0; j < x; j++) {
                                if (!temp.contains(numOfResultRow.get(j))) {
                                    numOfResultRow.remove((Integer) numOfResultRow.get(j));
                                    x--;
                                    j--;
                                }
                            }
                        } else if (sqlstring.contains("OR")){
                            int x = temp.size();
                            for (int j = 0; j < x; j++) {
                                if (!numOfResultRow.contains(temp.get(j))) {
                                    numOfResultRow.add((Integer) temp.get(j));
                                }
                            }
                        }
                    }
                    for (int k = 0;k < numOfResultRow.size();k++){
                        List<String> temp = new ArrayList<>();
                        for (int j = 0;j < numOfCol.size();j++){
                            temp.add(tableValue1.get(k).get(numOfCol.get(j)));
                        }
                        rowData.add(temp);
                    }
                    printTable("result",rowData,columnNames);
                } else if (sql.contains("in")){ //嵌套

                }
            }
        } catch (JSQLParserException e) {
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
//        printTable("",rowData,columnNames);
    }

    public static void selectTableData(User user, String databaseName, String selectString) {
        if (!UserUtils.judgePrivilege(user, "select")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            //构造jsqlparse
            Statement statement = CCJSqlParserUtil.parse(selectString);
            Select select = (Select) statement;
            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect) selectBody;
            //todo 目前是单表查询   //得到表名
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(select);
            String tableName = tableList.get(0);
            //打开相应文件
            File file = new File(Const.DATABASE_LOAD + "/" + databaseName + "/" + tableName + ".xls");
            if (!file.exists()) {
                System.out.println("ERROR : Table '" + databaseName + "." + tableName + "' doesn't exist");
                return;
            }
            InputStream in = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(0);

            //获取selectItem的内容，并添加到numOfSelectCol_list中 获得需要查询的列数
            List<Integer> numOfSelectCol_list = new ArrayList<Integer>();
            List<String> columnList = new ArrayList<String>();
            for (int k = 0; k < sheet.getRow(0).getPhysicalNumberOfCells(); k++) {
                Cell cell = sheet.getRow(0).getCell(k);
                columnList.add(cell.getStringCellValue());
            }
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            if (selectItems != null) {
                if (selectItems.get(0).toString().equals("*")) {
                    for (int i = 0; i < columnList.size(); i++) {
                        numOfSelectCol_list.add(i);
                    }
                } else {
                    for (int i = 0; i < selectItems.size(); i++) {
                        String s = selectItems.get(i).toString();
                        int j = 0;
                        for (; j < columnList.size(); j++) {
                            if (s.equals(columnList.get(j))) {
                                numOfSelectCol_list.add(j);
                                break;
                            }
                        }
                        if (j == columnList.size()) {
                            System.out.println("ERROR : Unknown column '" + s + "' in 'field list'");
                            return;
                        }
                    }
                }
            } else {
                System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                return;
            }
            List<Integer> numOfResultRow = new ArrayList<Integer>();
            //获取where表达式  获取sno 所在列数  获取符合条件的行数 添加到numOfResultRow
            //如果包含where,利用getResultRowListByWhere获取符合条件的行数,如果不包含where,默认全部符合
            if (selectString.contains("where")) {
                Expression where_expression = plainSelect.getWhere();
                String where = where_expression.toString();
                numOfResultRow = getResultRowListByWhere(file, where);
                if (numOfResultRow == null) {
                    return;
                }
            } else {
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    numOfResultRow.add(i);
                }
            }
            //select的列名集合
            List<String> columnNames = new ArrayList<>();
            for (int i = 0; i < numOfSelectCol_list.size(); i++) {
                columnNames.add(columnList.get(numOfSelectCol_list.get(i)));
            }
            //满足的数据集合
            List<List<String>> rowData = new ArrayList<>();
            for (int i = 0; i < numOfResultRow.size(); i++) {
                List<String> temp = new ArrayList<>();
                HSSFRow row = sheet.getRow(numOfResultRow.get(i));
                if (XlsUtils.isBlankRow(row)) {
                    continue;
                }
                for (int j = 0; j < numOfSelectCol_list.size(); j++) {
                    Cell cell = row.getCell(numOfSelectCol_list.get(j));
                    if (cell != null) {
                        temp.add(cell.getStringCellValue());
                    }
                }
                rowData.add(temp);
            }
            //orderBy
            List<OrderByElement> orderByElements = ((PlainSelect) selectBody).getOrderByElements();
            if(orderByElements != null){
                for (OrderByElement orderBy : orderByElements) {
                    String[] strings = orderBy.toString().split(" ");
                    String columnName = strings[0],order = "ASC";
                    if(strings.length == 2){
                        if(strings[1].equals("DESC"))
                            order = "DESC";
                    }
                        order = strings[1];
                    int num = columnNames.indexOf(columnName);
                    //判断这个列的value是否为int型
                    Table table = getTableProperty(file);
                    List<String> type = table.getType();
                    if(!type.get(num).contains("int")){
                        System.out.println(Const.Error.SQL_SYNTAX_ERROR+" The field isn't int.");
                        return;
                    }
                    //sort
                    if(order.equals("ASC")){
                        Collections.sort(rowData, new Comparator<List<String>>() {
                            @Override
                            public int compare(List<String> o1, List<String> o2) {
                                return Integer.parseInt(o1.get(num))-Integer.parseInt(o2.get(num));
                            }
                        });
                    }else{
                        Collections.sort(rowData, new Comparator<List<String>>() {
                            @Override
                            public int compare(List<String> o1, List<String> o2) {
                                return Integer.parseInt(o2.get(num))-Integer.parseInt(o1.get(num));
                            }
                        });
                    }
                }
            }
            printTable(tableName, rowData, columnNames);
            in.close();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //根据数据通过JFrame打印
    public static void printTable(String tableName, List<List<String>> rowData, List<String> columnName) {
        int a = rowData.size();
        int b = rowData.get(0).size();
        int c = columnName.size();
        Object[][] rowDatas = new Object[a][b];
        Object[] columnNames = new Object[c];
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++)
                rowDatas[i][j] = rowData.get(i).get(j);
        }
        for (int i = 0; i < c; i++) {
            columnNames[i] = columnName.get(i);
        }
        JTable friends = new JTable(rowDatas, columnNames);
        friends.doLayout();
        friends.setBackground(Color.lightGray);

        JScrollPane pane = new JScrollPane(friends);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.setPreferredSize(new Dimension(600, 400));
        panel.setBackground(Color.black);
        panel.add(pane);

        JFrame frame = new JFrame(tableName);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setContentPane(panel);

        frame.pack();
        frame.setVisible(true);
    }

    public void helpTable(String databaseName, String tableString) {
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        List<List<String>> rowData = new ArrayList<>();
        List<String> colunmnName = new ArrayList<>();
        String[] tableStrs = tableString.split(" ");
        String tableName = tableStrs[2];
        File file = new File(Const.DATABASE_LOAD + databaseName + "/" + tableName + ".xls");
        try {
            FileInputStream in = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(1);
            HSSFRow row1 = sheet.getRow(0);
            for (int i = 0;i < row1.getPhysicalNumberOfCells();i++){
                HSSFCell cell = row1.getCell(i);
                colunmnName.add(cell.getStringCellValue());
            }
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                List<String> temp = new ArrayList<>();
                for (int j = 0; j < sheet.getRow(0).getPhysicalNumberOfCells(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        cell.setCellType(CellType.STRING);
                        temp.add(cell.getStringCellValue());
                    } else {
                        temp.add("");
                    }
                }
                rowData.add(temp);
            }
            printTable(tableName,rowData,colunmnName);
        } catch (FileNotFoundException e) {
            System.out.println("ERROR : The table '" + tableName + "' doesn't exist");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showTables(String databaseName) {
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        File file = new File("database/" + databaseName);
        File[] tables = file.listFiles();
        if (tables.length == 0) {
            System.out.println("Empty set");
            return;
        }
        for (int i = 0; i < tables.length; i++) {
            System.out.println(tables[i].getName().substring(0, tables[i].getName().length() - 4));
        }
        return;
    }

    //判断table是否已经存在，存在返回真
    private boolean isTableExists(String databaseName, String tableName) {
        boolean isTableExists = false;
        File table = new File("database/" + databaseName + "/" + tableName + ".xls");
        if (table.exists()) {
            isTableExists = true;
        }
        return isTableExists;
    }

    //根据表和需要寻找的列的list(str_colList),返回该列在表中的位置
    private static List<Integer> getNumOfColList(String database, String tableName, List<String> str_colList) {
        List<Integer> numOfColList = new ArrayList<Integer>();
        try {
            InputStream in = new FileInputStream(new File(Const.DATABASE_LOAD + database + "/" + tableName + ".xls"));
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i < str_colList.size(); i++) {
                for (int j = 0; j < sheet.getRow(0).getPhysicalNumberOfCells(); j++) {
                    Cell cell = sheet.getRow(0).getCell(j);
                    cell.setCellType(CellType.STRING);
                    String colName = cell.getStringCellValue();
                    if (colName.equals(str_colList.get(i))) {
                        numOfColList.add(j);
                        continue;
                    }
                }
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numOfColList;
    }

    public static List<Integer> getResultRowListByWhere(File file, String where) {
        List<Integer> numOfResultRow = new ArrayList<>();
        try {
            InputStream in = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(0);
            List<String> columnList = new ArrayList<>();
            for (int i = 0; i < sheet.getRow(0).getPhysicalNumberOfCells(); i++) {
                HSSFCell cell = sheet.getRow(0).getCell(i);
                columnList.add(cell.getStringCellValue());
            }
            String[] where_terms = null;
            if(where.contains("and")){
                where_terms = where.split("and");
            } else {
                where_terms = where.split("or");
            }
            List<List<String>> tableValueList = getTableValue(file);
            for (int i = 0; i < where_terms.length; i++) {
                String where_temp = where_terms[i].trim();
                String[] where_term = null;
                List<Integer> tempNumOfRow = new ArrayList<>();
                if (where_temp.contains("like") || where_temp.contains(">") || where_temp.contains("=") || where_temp.contains("<")) {
                    where_term = where_temp.split(" ");
                    String columnName = where_term[0].trim();
                    String character = where_term[1].trim();
                    String value = where_term[2].trim();
                    int numOfColumn = -1;
                    for (int j = 0; j < columnList.size(); j++) {
                        if (columnList.get(j).equals(columnName)) {
                            numOfColumn = j;
                            break;
                        }
                    }
                    if (numOfColumn == -1) {
                        System.out.println("ERROR : Unknown column '" + where_term[0] + "' in 'where clause'");
                        return null;
                    }
                    switch (character) {
                        case "like": {
                            value = value.replaceAll("'", "");
                            value = value.replaceAll("_", ".");
                            value = value.replaceAll("%", "(.*)");
                            Pattern pattern = Pattern.compile(value);
                            String columnValue = null;
                            for (int j = 0; j < tableValueList.size(); j++) {
                                columnValue = tableValueList.get(j).get(numOfColumn);
                                Matcher matcher = pattern.matcher(columnValue);
                                if (matcher.matches()) {
                                    tempNumOfRow.add(j + 1);
                                }
                            }
                            break;
                        }
                        case ">=": {
                            String columnValue = null;
                            for (int j = 0; j < tableValueList.size(); j++) {
                                columnValue = tableValueList.get(j).get(numOfColumn);
                                if (!StringUtils.isNumeric(columnValue) || !StringUtils.isNumeric(value)) {
                                    System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                                    return null;
                                } else {
                                    if (Integer.parseInt(columnValue) >= Integer.parseInt(value)) {
                                        tempNumOfRow.add(j + 1);
                                    }
                                }
                            }
                            break;
                        }
                        case "<=": {
                            String columnValue = null;
                            for (int j = 0; j < tableValueList.size(); j++) {
                                columnValue = tableValueList.get(j).get(numOfColumn);
                                if (!StringUtils.isNumeric(columnValue) || !StringUtils.isNumeric(value)) {
                                    System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                                    return null;
                                } else {
                                    if (Integer.parseInt(columnValue) <= Integer.parseInt(value)) {
                                        tempNumOfRow.add(j + 1);
                                    }
                                }
                            }
                            break;
                        }
                        case "=": {
                            String columnValue = null;
                            for (int j = 0; j < tableValueList.size(); j++) {
                                columnValue = tableValueList.get(j).get(numOfColumn);
                                if (columnValue.equals(value)) {
                                    tempNumOfRow.add(j + 1);
                                }
                            }
                            break;
                        }
                        case ">": {
                            String columnValue = null;
                            for (int j = 0; j < tableValueList.size(); j++) {
                                columnValue = tableValueList.get(j).get(numOfColumn);
                                if (!StringUtils.isNumeric(columnValue) || !StringUtils.isNumeric(value)) {
                                    System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                                    return null;
                                } else {
                                    if (Integer.parseInt(columnValue) > Integer.parseInt(value)) {
                                        tempNumOfRow.add(j + 1);
                                    }
                                }
                            }
                            break;
                        }
                        case "<": {
                            String columnValue = null;
                            for (int j = 0; j < tableValueList.size(); j++) {
                                columnValue = tableValueList.get(j).get(numOfColumn);
                                if (!StringUtils.isNumeric(columnValue) || !StringUtils.isNumeric(value)) {
                                    System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                                    return null;
                                } else {
                                    if (Integer.parseInt(columnValue) < Integer.parseInt(value)) {
                                        tempNumOfRow.add(j + 1);
                                    }
                                }
                            }
                            break;
                        }
                    }
                } else if (where_temp.contains("is null") || where_temp.contains("is not null")) {
                    where_term = where_temp.split(" ");
                    String columnName = where_term[0].trim();
                    int num = where_term.length;
                    String columnValue = null;
                    int numOfColumn = -1;
                    for (int j = 0; j < columnList.size(); j++) {
                        if (columnList.get(j).equals(columnName)) {
                            numOfColumn = j;
                            break;
                        }
                    }
                    if (numOfColumn == -1) {
                        System.out.println("ERROR : Unknown column '" + where_term[0] + "' in 'where clause'");
                        return null;
                    }
                    for (int j = 0; j < tableValueList.size(); j++) {
                        columnValue = tableValueList.get(j).get(numOfColumn);
                        if (num == 3 && columnValue.equals("")) {
                            tempNumOfRow.add(j + 1);
                        } else if (num == 4 && !columnValue.equals("")) {
                            tempNumOfRow.add(j + 1);
                        } else {
                        }
                    }
                }
                if (i == 0) {
                    numOfResultRow = tempNumOfRow;
                } else {
                    int x = numOfResultRow.size();
                    for (int j = 0; j < x; j++) {
                        if (!tempNumOfRow.contains(numOfResultRow.get(j))) {
                            numOfResultRow.remove((Integer) numOfResultRow.get(j));
                            x--;
                            j--;
                        }
                    }
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numOfResultRow;
    }

    // *********select body items内容
    public static List<String> test_select_items(String sql)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectitems = plain.getSelectItems();
        List<String> str_items = new ArrayList<String>();
        if (selectitems != null) {
            for (int i = 0; i < selectitems.size(); i++) {
                str_items.add(selectitems.get(i).toString());
            }
        }
        return str_items;
    }

    // **********select table
    public static List<String> test_select_table(String sql)
            throws JSQLParserException {
        Statement statement = (Statement) CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) statement;
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder
                .getTableList(selectStatement);
        return tableList;
    }

    //******************* select join
    public static List<String> test_select_join(String sql)
            throws JSQLParserException {
        Statement statement = (Statement) CCJSqlParserUtil.parse(sql);
        Select selectStatement = (Select) statement;
        PlainSelect plain = (PlainSelect) selectStatement.getSelectBody();
        List<Join> joinList = plain.getJoins();
        List<String> tablewithjoin = new ArrayList<String>();
        if (joinList != null) {
            for (int i = 0; i < joinList.size(); i++) {
                tablewithjoin.add(joinList.get(i).toString());
                //注意 ， leftjoin rightjoin 等等的to string()区别
            }
        }
        return tablewithjoin;
    }

    // *******select where
    public static String test_select_where(String sql)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        Expression where_expression = plain.getWhere();
        String str = where_expression.toString();
        return str;
    }

    // ******select group by
    public static List<String> test_select_groupby(String sql)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<Expression> GroupByColumnReferences = plain
                .getGroupByColumnReferences();
        List<String> str_groupby = new ArrayList<String>();
        if (GroupByColumnReferences != null) {
            for (int i = 0; i < GroupByColumnReferences.size(); i++) {
                str_groupby.add(GroupByColumnReferences.get(i).toString());
            }
        }
        return str_groupby;
    }

    // **************select order by
    public static List<String> test_select_orderby(String sql)
            throws JSQLParserException {
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<OrderByElement> OrderByElements = plain.getOrderByElements();
        List<String> str_orderby = new ArrayList<String>();
        if (OrderByElements != null) {
            for (int i = 0; i < OrderByElements.size(); i++) {
                str_orderby.add(OrderByElements.get(i).toString());
            }
        }
        return str_orderby;
    }
}
