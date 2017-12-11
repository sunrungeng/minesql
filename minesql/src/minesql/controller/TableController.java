package minesql.controller;

import minesql.common.Const;
import minesql.pojo.User;
import minesql.util.FileUtils;
import minesql.util.StringUtils;
import minesql.util.UserUtils;
import minesql.util.XlsUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectItem;
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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by srg
 *
 * @date 2017/11/20
 */
public class TableController {
    public TableController() {
    }

    public void createTable(User user, String databaseName, String createStr){
        if(!UserUtils.judgePrivilege(user,"create")){
            return;
        }
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        createStr = createStr.replaceAll("'","");
        int theFirstBracket = createStr.indexOf("(");
        int theLastBracket = createStr.lastIndexOf(")");

        //取出正确表名
        String tableName = null;
        String frontCreate = createStr.substring(0,theFirstBracket);
        String[] frontCreateStrings = frontCreate.split(" ");
        if(frontCreateStrings[0].equals("create")){
            if(frontCreateStrings[1].equals("table")){
                tableName = frontCreateStrings[2];
                //如果表已存在，返回
                if(this.isTableExists(databaseName,tableName)){
                    System.out.println("ERROR : Table '" + tableName + "' already exists");
                    return;
                }
//                System.out.println(tableName);
            }else{
                System.out.println("ERROR : You have an error in your SQL syntax;");
                return;
            }
        }else{
            System.out.println("ERROR : You have an error in your SQL syntax;");
            return;
        }

        //取出表的构造语句并写入xls文件
        File table = new File("database/"+databaseName+"/"+tableName+".xls");
        OutputStream fos = null;
        String createString = createStr.substring(theFirstBracket + 1,theLastBracket);
//        System.out.println(createString);
        String[] createStrings = createString.split(",");
        int count = 0;

        // 创建工作薄
        HSSFWorkbook workbook = new HSSFWorkbook();
        // 创建工作表
        HSSFSheet sheet1 = workbook.createSheet("sheet1");
        HSSFSheet sheet2 = workbook.createSheet("sheet2");

        HSSFRow row1 =sheet1.createRow(0);
        HSSFRow rows2 = sheet2.createRow(0);
        rows2.createCell(0).setCellValue("Field");
        rows2.createCell(1).setCellValue("Type");
        rows2.createCell(2).setCellValue("Null");
        rows2.createCell(3).setCellValue("Key");
        rows2.createCell(4).setCellValue("Default");
        rows2.createCell(5).setCellValue("Check");
//        rows2.createCell(5).setCellValue("Extra");
        try{
            for (String stringItem:createStrings) {
                stringItem = stringItem.trim();
                String[] columnString = stringItem.split(" ");
                //检测主键
                if(stringItem.startsWith("primary key")){
                    String columnName = StringUtils.getStringInBracket(stringItem);
                    int numOfColumn = StringUtils.getNumByColumnName(createStrings,columnName);
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(3).setCellValue("PRI");
                    continue;
                }
                //检测唯一性
                if(stringItem.startsWith("unique")){
                    String columnName = StringUtils.getStringInBracket(stringItem);
                    int numOfColumn = StringUtils.getNumByColumnName(createStrings,columnName);
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(3).setCellValue("UNI");
                    continue;
                }
                //检测check
                if(stringItem.startsWith("check")){
                    String columnName = StringUtils.getStringInBracket(stringItem);
                    int numOfColumn = StringUtils.getNumOfColumnInCreate(createStrings,columnName);
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(5).setCellValue(columnName);
                    continue;
                }
                //外键
                if(stringItem.startsWith("foreign key")){
                    String[] strings = stringItem.split(" ");
                    String foreignKeyColumn = StringUtils.getStringInBracket(strings[2]);
                    int numOfColumn = StringUtils.getNumOfColumnInCreate(createStrings,foreignKeyColumn);
                    String foreignKey = "FOR " + strings[4];
                    HSSFRow rows4 = sheet2.getRow(numOfColumn + 1);
                    rows4.createCell(3).setCellValue(foreignKey);
                    continue;
                }
                HSSFRow rows3 = sheet2.createRow(count + 1);
                row1.createCell(count).setCellValue(columnString[0]);
                rows3.createCell(0).setCellValue(columnString[0]);
                rows3.createCell(1).setCellValue(columnString[1]);

                if (stringItem.contains("primary key") || stringItem.contains("not null")){
                    rows3.createCell(2).setCellValue("NO");
                    if(stringItem.contains("primary key")){
                        rows3.createCell(3).setCellValue("PRI");
                    }
                } else {
                    rows3.createCell(2).setCellValue("YES");
                }

                if(stringItem.contains("unique")){
                    rows3.createCell(3).setCellValue("UNI");
                }

                if(stringItem.contains("foreign key")){
                    int indexOfForeign = stringItem.indexOf("foreign key");
                    String str = stringItem.substring(indexOfForeign,stringItem.length());
                    String[] strings = str.split(" ");
                    String foreignKeyString = "FOR "+ strings[1];
                    rows3.createCell(3).setCellValue(foreignKeyString);
                }

                if(stringItem.contains("default")){
                    int index = stringItem.indexOf("default");
                    int theLeftSpace = stringItem.indexOf(" ",index);
                    int theRightSpace = stringItem.indexOf(" ",theLeftSpace + 1);
                    String defaultValue = null;
                    if(theRightSpace != -1){
                        defaultValue = stringItem.substring(theLeftSpace + 1,theRightSpace);
                    }else{
                        defaultValue = stringItem.substring(theLeftSpace + 1,stringItem.length());
                    }

                    rows3.createCell(4).setCellValue(defaultValue);
                }
                count++;
            }

            // 写入数据
            fos = new FileOutputStream(table);
            workbook.write(fos);
            fos.close();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        //create table
    }

    public void dropTable(User user, String databaseName, String dropString){
        if(!UserUtils.judgePrivilege(user,"create")){
            return;
        }
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(dropString);
            Drop drop = (Drop)statement;
            String tableName = drop.getName();
            if(!this.isTableExists(databaseName,tableName)){
                System.out.println("ERROR : Unknown table '" + tableName + "'");
                return;
            } else {
                File file = new File(Const.DATABASELOAD + databaseName + "/" + tableName + ".xls");
                file.delete();
            }

        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        TableController tableController = new TableController();
        UserController userController = new UserController();
        User user = userController.login();
//        for(int i = 1;i <= 7;i++){
//            tableController.createTable(user,"test","create table test"+i+"(sno int(20) default 20,sname char(20),sex char(10) not null,primary key(sno),unique(sname),check(sno>0),foreign key (sex) references person(sex))");
//        }
//        tableController.insertTableData("test","insert test4 values(20,adfa,male),(30,srg,female)");
//        tableController.deleteTableData1("test","delete from test2");
//        tableController.dropTable("test","drop table test1");
//        tableController.selectTableData("test","select sno,sname from test2 where sname = srg");
//        tableController.showTables("test");
//        tableController.helpTable("test","help table test10");
//        tableController.updateTableData(user,"test","update test8 set sno = 1,sex = ss where sname = srg");
    }

    public void insertTableData(User user, String databaseName, String insertString){
        if(!UserUtils.judgePrivilege(user,"insert")){
            return;
        }
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(insertString);
            Insert insert = (Insert)statement;
            String tableName = this.insert_table(insertString);
            List<Column> insertColumns = insert.getColumns();
            System.out.println(tableName);

            File file1 = new File(Const.DATABASELOAD+databaseName+"/"+tableName+".xls");
            File file2 = new File(Const.DATABASELOAD+databaseName+"/"+tableName+"cp.xls");

            FileUtils.copyFile(file1,file2);
            InputStream in = new FileInputStream(file2);
            FileOutputStream fos = new FileOutputStream(file1);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet1 = workbook.getSheetAt(0);
            HSSFSheet sheet2 = workbook.getSheetAt(1);
            List<String> tableColumnsList = new ArrayList<String>();
            //要插入的数据的列数列表
            List<Integer> numOfColumnList = new ArrayList<Integer>();
            for(int i = 0;i < sheet1.getRow(0).getPhysicalNumberOfCells();i++){
                Cell cell = sheet1.getRow(0).getCell(i);
                cell.setCellType(CellType.STRING);
                tableColumnsList.add(cell.getStringCellValue());
            }
            if(insertColumns != null){
                for(int i = 0;i < insertColumns.size();i++){
                    String insertColumnName = insertColumns.get(i).getColumnName();
                    int j = 0;
                    for(;j < tableColumnsList.size();j++){
                        if(insertColumnName.equals(tableColumnsList.get(j))){
                            numOfColumnList.add(j);
                            break;
                        }
                    }
                     if(j == tableColumnsList.size()) {
                        System.out.println("ERROR : Unknown column '" + insertColumnName + "' in 'field list'");
                        return;
                    }
                }
            } else {
                for(int i = 0;i < tableColumnsList.size();i++){
                    numOfColumnList.add(i);
                }
            }

            //todo 现在不考虑约束的问题
            List<List<String>> valuesList = this.insert_values(insertString);
            int num = sheet1.getLastRowNum() + 1;
            System.out.println(num);
            for(int i = 0;i < valuesList.size();i++){
                Row row = sheet1.createRow(num++);
                for(int j = 0;j < valuesList.get(i).size();j++){
                    Cell cell = row.createCell(numOfColumnList.get(j),CellType.STRING);
                    cell.setCellValue(valuesList.get(i).get(j));
                }
            }

            workbook.write(fos);
            fos.close();
            in.close();
            file2.delete();

            XlsUtils.delLineNull(Const.DATABASELOAD+databaseName+"/"+tableName+".xls");
        } catch (JSQLParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String insert_table(String sql)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insertStatement = (Insert) statement;
        String string_tablename = insertStatement.getTable().getName();
        return string_tablename;
    }

    // ********* insert table column
    public static List<String> insert_column(String sql)
            throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Insert insertStatement = (Insert) statement;
        List<Column> table_column = insertStatement.getColumns();
        List<String> str_column = new ArrayList<String>();
        if(table_column != null){
            for (int i = 0; i < table_column.size(); i++) {
                str_column.add(table_column.get(i).toString());
            }
        }
        return str_column;
    }

    // ********* Insert values ExpressionList
    public static List<List<String>> insert_values(String sql) {
        int indexOfValues = sql.indexOf("values");
        int beginIndex = sql.indexOf("(",indexOfValues);
        int endIndex = sql.indexOf(")",beginIndex);
        String values = sql.substring(beginIndex + 1,endIndex);
        List<List<String>> valuesList = new ArrayList<>();
        while(endIndex != -1){
            String[] str_values = values.split(",");
            List<String> stringList = new ArrayList<String>();
            for(int i = 0;i < str_values.length;i++){
                stringList.add(str_values[i]);
            }
            valuesList.add(stringList);
            beginIndex = sql.indexOf("(",beginIndex + 1);
            endIndex = sql.indexOf(")",endIndex + 1);
            if(beginIndex == -1 || endIndex == -1){
                break;
            }
            values = sql.substring(beginIndex + 1,endIndex);
        }
        return valuesList;
    }

    //todo 将1 2 函数合并
    public void deleteTableData1(User user, String databaseName, String deleteString){
        if(!UserUtils.judgePrivilege(user,"delete")){
            return;
        }
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            deleteString = deleteString.toLowerCase();
            deleteString = deleteString.replaceAll("'","");
            Statement statement = CCJSqlParserUtil.parse(deleteString);
            Delete delete = (Delete)statement;
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(delete);
            //todo 目前是单表，还有多表

            String tableName = tableList.get(0);
            if(!this.isTableExists(databaseName,tableName)){
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
            if(deleteString.contains("where")){
                List<String> columnList = new ArrayList<String>();
                for(int k = 0;k < sheet.getRow(0).getPhysicalNumberOfCells();k++){
                    Cell cell = sheet.getRow(0).getCell(k);
                    columnList.add(cell.getStringCellValue());
                    System.out.println(cell.getStringCellValue());
                }
                Expression where_expression = delete.getWhere();
                String whereString = where_expression.toString();

                String[] strings = whereString.split("and");
                String s = strings[0];
                if(s.contains(">=")){

                }else if(s.contains("<=")){

                }else if(s.contains("=")){
                    String[] strs = s.split("=");
                    strs[0] = strs[0].trim();
                    strs[1] = strs[1].trim();
                    int numberOfDeleteColumn = 0;
                    for(int i = 0;i < columnList.size();i++){
                        if(strs[0].equals(columnList.get(i))){
                            numberOfDeleteColumn = i;
                            System.out.println(numberOfDeleteColumn);
                            break;
                        }
                    }
                    for(int i = 1;i < sheet.getLastRowNum();i++){
                        Row row = sheet.getRow(i);
                        HSSFCell cell = (HSSFCell) row.getCell(numberOfDeleteColumn);
                        cell.setCellType(CellType.STRING);
                        if(cell.getRichStringCellValue().toString().equals(strs[1])){
                            numberOfDeleteRow.add(i);
                        }
                    }
                }
                in.close();

            }else {
                int numOfColumn = sheet.getLastRowNum();
                for(int i = 1;i <= numOfColumn;i++){
                    numberOfDeleteRow.add(i);
                }
            }

            this.deleteTableData2(filePath,numberOfDeleteRow);

        } catch (JSQLParserException e) {
//            e.printStackTrace();
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
        } catch (FileNotFoundException e) {
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void deleteTableData2(String filePath,List<Integer> numList){
        try {
            FileInputStream in = new FileInputStream(filePath);
            POIFSFileSystem fs = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            FileOutputStream out = new FileOutputStream(filePath);
            HSSFSheet sheet = workbook.getSheetAt(0);
            List<Integer> numberOfDeleteRow = numList;
            for (int i = 0;i < numberOfDeleteRow.size();i++){
                Row row = sheet.createRow(numberOfDeleteRow.get(i));
            }
            workbook.write(out);
            in.close();
            out.close();
            XlsUtils.delLineNull(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateTableData(User user,String databaseName,String updateString){
        if(!UserUtils.judgePrivilege(user,"update")){
            return;
        }
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(updateString);
            Update update = (Update)statement;
            //表名
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(update);
            //todo 目前单表
            String tableName = tableList.get(0);
            if(!isTableExists(databaseName,tableName)){
                System.out.println("ERROR : Table '" + databaseName + "." + tableName + "' doesn't exist");
                return;
            }
            //得到set的column
            List<Column> columnList = update.getColumns();
            List<String> str_columnList = new ArrayList<String>();
            if(columnList != null){
                for(int i = 0;i < columnList.size();i++){
                    str_columnList.add(columnList.get(i).toString());
                    System.out.println(columnList.get(i).toString());
                }
            }
            //得到set的列的下标
            List<Integer> numOfUpdateColList = this.getNumOfColList(databaseName,tableName,str_columnList);
            //
            List<Expression> update_values = update.getExpressions();
            List<String> str_values = new ArrayList<String>();
            if (update_values != null) {
                for (int i = 0; i < update_values.size(); i++) {
                    str_values.add(update_values.get(i).toString());
//                    System.out.println((update_values.get(i).toString()));
                }
            }

            File file1 = new File(Const.DATABASELOAD+databaseName+"/"+tableName+".xls");
            File file2 = new File(Const.DATABASELOAD+databaseName+"/"+tableName+"cp.xls");

            FileUtils.copyFile(file1,file2);
            InputStream in = new FileInputStream(file2);
            FileOutputStream fos = new FileOutputStream(file1);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(0);
            List<Integer> numOfResultRow = new ArrayList<Integer>();
            List<String> allColumnList = new ArrayList<String>();
            for(int i = 0;i < sheet.getRow(0).getPhysicalNumberOfCells();i++){
                Cell cell = sheet.getRow(0).getCell(i);
                cell.setCellType(CellType.STRING);
                allColumnList.add(cell.getStringCellValue());
            }
            //where
            if(updateString.contains("where")){
                Expression where_expression = update.getWhere();
                String where = where_expression.toString();

                //获取where表达式  获取sno 所在列数  获取符合条件的行数 添加到numOfResultRow
                String[] where_terms = where.split("and");
                for(int i = 0;i < where_terms.length;i++){
                    String[] where_term = where_terms[i].split("=");
                    where_term[0] = where_term[0].trim();
                    where_term[1] = where_term[1].trim();
                    int numOfColumn = -1;
                    for(int j = 0;j < allColumnList.size();j++){
                        if(allColumnList.get(j).equals(where_term[0])){
                            numOfColumn = j;
                            break;
                        }
                    }
                    if(numOfColumn == -1){
                        System.out.println("ERROR : Unknown column '" + where_term[0] + "' in 'where clause'");
                        return;
                    }
                    for(int k = 1;k <= sheet.getLastRowNum();k++){
                        HSSFRow row = sheet.getRow(k);
                        Cell cell = row.getCell(numOfColumn);
                        if(cell == null){
                            continue;
                        }
                        cell.setCellType(CellType.STRING);
                        if(cell.getRichStringCellValue().toString().equals(where_term[1])){
                            numOfResultRow.add(k);
                        }
                    }
                }
            } else {
                for(int i = 1;i <= sheet.getLastRowNum();i++){
                    numOfResultRow.add(i);
                }
            }

            for(int i = 0;i < numOfResultRow.size();i++){
                Row row = sheet.getRow(numOfResultRow.get(i));
                for(int j = 0,k = 0;k < allColumnList.size();k++){
                    if(k != numOfUpdateColList.get(j)){
//                        row.createCell(k).setCellValue(row.getCell(k).getStringCellValue());
                    } else {
                        row.getCell(k).setCellValue(str_values.get(j));
                        j++;
                        if(j == numOfUpdateColList.size()){
                            break;
                        }
                    }
                }
            }

            file2.delete();
            workbook.write(fos);
            fos.close();
            in.close();
            //todo 是否关闭IO流

        } catch (JSQLParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectTableData(User user, String databaseName, String selectString){
        if(!UserUtils.judgePrivilege(user,"select")){
            return;
        }
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        try {
            //构造jsqlparse
            Statement statement = CCJSqlParserUtil.parse(selectString);
            Select select = (Select)statement;
            SelectBody selectBody = select.getSelectBody();
            PlainSelect plainSelect = (PlainSelect)selectBody;
            //todo 目前是单表查询   //得到表名
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(select);
            String tableName = tableList.get(0);
//            System.out.println(tableName);
            //打开相应文件
            File file = new File(Const.DATABASELOAD + "/" + databaseName + "/" + tableName + ".xls");
            if(!this.isTableExists(databaseName,tableName)){
                System.out.println("ERROR : Table '" + databaseName + "." + tableName + "' doesn't exist");
                return;
            }
            InputStream in = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(0);
            //获取selectItem的内容，并添加到numOfSelectCol_list中
            //todo 利用getNumOfColList替换
            List<Integer> numOfSelectCol_list = new ArrayList<Integer>();
            List<String> columnList = new ArrayList<String>();
            for(int k = 0;k < sheet.getRow(0).getPhysicalNumberOfCells();k++){
                Cell cell = sheet.getRow(0).getCell(k);
                columnList.add(cell.getStringCellValue());
//                System.out.println(cell.getStringCellValue());
            }
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            if(selectItems != null){
                if(selectItems.get(0).toString().equals("*")){
                    for(int i = 0;i < columnList.size();i++){
                        numOfSelectCol_list.add(i);
                    }
                }else{
                    for(int i = 0;i < selectItems.size();i++){
                        String s = selectItems.get(i).toString();
                        int j = 0;
                        for(;j < columnList.size();j++){
                            if(s.equals(columnList.get(j))){
                                numOfSelectCol_list.add(j);
                                break;
                            }
                        }
                        if(j == columnList.size()){
                            System.out.println("ERROR : Unknown column '" + s + "' in 'field list'");
                            return;
                        }
//                        System.out.println(s);
                    }
                }
            }
            List<Integer> numOfResultRow = new ArrayList<Integer>();
            //获取where表达式  获取sno 所在列数  获取符合条件的行数 添加到numOfResultRow
            if(selectString.contains("where")){
                Expression where_expression = plainSelect.getWhere();
                String where = where_expression.toString();
                String[] where_terms = where.split("and");
                for(int i = 0;i < where_terms.length;i++){
                    String[] where_term = where_terms[i].split("=");
                    where_term[0] = where_term[0].trim();
                    where_term[1] = where_term[1].trim();
                    int numOfColumn = -1;
                    for(int j = 0;j < columnList.size();j++){
                        if(columnList.get(j).equals(where_term[0])){
                            numOfColumn = j;
                            break;
                        }
                    }
                    if(numOfColumn == -1){
                        System.out.println("ERROR : Unknown column '" + where_term[0] + "' in 'where clause'");
                        return;
                    }
                    for(int k = 1;k <= sheet.getLastRowNum();k++){
                        HSSFRow row = sheet.getRow(k);
                        Cell cell = row.getCell(numOfColumn);
                        cell.setCellType(CellType.STRING);
                        if(cell.getRichStringCellValue().toString().equals(where_term[1])){
                            numOfResultRow.add(k);
                        }
                    }
                }
            } else {
                for(int i = 1;i <= sheet.getLastRowNum();i++){
                    numOfResultRow.add(i);
                }
            }

            for (int i = 0;i < numOfResultRow.size();i++){
                HSSFRow row = sheet.getRow(numOfResultRow.get(i));
                if(XlsUtils.isBlankRow(row)){
                    continue;
                }
                for(int j = 0;j < numOfSelectCol_list.size();j++){
                    Cell cell = row.getCell(numOfSelectCol_list.get(j));
                    if(cell != null){
                        cell.setCellType(CellType.STRING);
                        System.out.print(cell.getRichStringCellValue().toString());
                    }
                }
                System.out.println();
            }
            in.close();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void helpTable(String databaseName,String tableString){
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        String[] tableStrs = tableString.split(" ");
        String tableName = tableStrs[2];
        File file = new File(Const.DATABASELOAD + databaseName + "/" + tableName + ".xls");
        try {
            FileInputStream in = new FileInputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(1);
            for(int i = 0;i <= sheet.getLastRowNum();i++){
                Row row = sheet.getRow(i);
                for(int j = 0;j < sheet.getRow(0).getPhysicalNumberOfCells();j++){
                    Cell cell = row.getCell(j);
                    if(cell != null){
                        cell.setCellType(CellType.STRING);
                        System.out.printf("%-15s",cell.getRichStringCellValue());
                    }else{
                        System.out.printf("%-15s","");
                    }
                }
                System.out.println();
            }
        } catch (FileNotFoundException e) {
            System.out.println("ERROR : The table '" + tableName + "' doesn't exist");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showTables(String databaseName) {
        if(!DatabaseController.isDatabaseExists(databaseName)){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        File file = new File("database/"+databaseName);
        File[] tables = file.listFiles();
        if(tables.length == 0){
            System.out.println("Empty set");
            return;
        }
        for(int i = 0;i < tables.length;i++){
            System.out.println(tables[i].getName().substring(0,tables[i].getName().length()-4));
        }
        return;
    }

    //判断table是否已经存在，存在返回真
    private boolean isTableExists(String databaseName,String tableName){
        boolean isTableExists = false;
        File table = new File("database/"+databaseName+"/"+tableName+".xls");
        if(table.exists()){
            isTableExists = true;
        }
        return isTableExists;
    }

    private static List<Integer> getNumOfColList(String database,String tableName,List<String> str_colList){
        List<Integer> numOfColList = new ArrayList<Integer>();
        try {
            InputStream in = new FileInputStream(new File(Const.DATABASELOAD+database+"/"+tableName+".xls"));
            HSSFWorkbook workbook = new HSSFWorkbook(in);
            HSSFSheet sheet = workbook.getSheetAt(0);
            for(int i = 0;i < str_colList.size();i++){
                for(int j = 0;j < sheet.getRow(0).getPhysicalNumberOfCells();j++){
                    Cell cell = sheet.getRow(0).getCell(j);
                    cell.setCellType(CellType.STRING);
                    String colName = cell.getStringCellValue();
                    if(colName.equals(str_colList.get(i))){
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

//    public static List<Integer> getResultRowListByWhere(String databaseName,String tableName,String sql, String sqlType,List<String>columnList){
//        List<Integer> numOfResultRow = new ArrayList<Integer>();
//        File file = new File(Const.DATABASELOAD + databaseName + "/" + tableName + ".xls");
//        try {
//            InputStream in = new FileInputStream(file);
//            HSSFWorkbook workbook = new HSSFWorkbook(in);
//            HSSFSheet sheet = workbook.getSheetAt(0);
//            Statement statement = CCJSqlParserUtil.parse(sql);
//            Update update = null;
//            if(sqlType.equals("update")){
//                update = (Select)statement;
//            }
//            if(sql.contains("where")){
//                Expression where_expression = update.getWhere();
//                String where = where_expression.toString();
//                String[] where_terms = where.split("and");
//                for(int i = 0;i < where_terms.length;i++){
//                    String[] where_term = where_terms[i].split("=");
//                    where_term[0] = where_term[0].trim();
//                    where_term[1] = where_term[1].trim();
//                    int numOfColumn = -1;
//                    for(int j = 0;j < columnList.size();j++){
//                        if(columnList.get(j).equals(where_term[0])){
//                            numOfColumn = j;
//                            break;
//                        }
//                    }
//                    if(numOfColumn == -1){
//                        System.out.println("ERROR : Unknown column '" + where_term[0] + "' in 'where clause'");
//                        return;
//                    }
//                    for(int k = 1;k <= sheet.getLastRowNum();k++){
//                        HSSFRow row = sheet.getRow(k);
//                        Cell cell = row.getCell(numOfColumn);
//                        cell.setCellType(CellType.STRING);
//                        if(cell.getRichStringCellValue().toString().equals(where_term[1])){
//                            numOfResultRow.add(k);
//                        }
//                    }
//                }
//            } else {
//                for(int i = 1;i <= sheet.getLastRowNum();i++){
//                    numOfResultRow.add(i);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            System.out.println("ERROR : The table '" + tableName + "' doesn't exist");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSQLParserException e) {
//            e.printStackTrace();
//        }
//
//    }
}
