package minesql.controller;

import minesql.common.Const;
import minesql.pojo.User;
import minesql.util.UserUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.drop.Drop;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by srg
 *
 * @date 2017/11/20
 */
public class DatabaseController {

    public DatabaseController() {
    }

    public String useDatabase(String sql){
        String[] strings = sql.split(" ");
        if(strings.length != 2){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return null;
        }
        String databaseName = strings[1];
        List<String> databaseList = this.getDatabases();
        if(databaseList.contains(databaseName)){
            System.out.println("database changed");
            return databaseName;
        }else{
            System.out.println("ERROR : Unknown database '" + databaseName + "'");
            return null;
        }
    }

    public void createDatabase(User user, String sql){
        if(!UserUtils.judgePrivilege(user,"create")){
            return;
        }
        String[] strings = sql.split(" ");
        if(strings.length != 3){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
        String databaseName = strings[2];
        if(this.isDatabaseExists(databaseName)){
            System.out.println("ERROR : Con't create database '" + databaseName +"';database exists;");
        } else {
            File database = new File(Const.DATABASE_LOAD+databaseName);
            database.mkdirs();
            System.out.println(Const.COMMON_SUCCESS);
        }
    }

    public void dropDatabase(User user,String dropDatabaseString){
        if(!UserUtils.judgePrivilege(user,"drop")){
            return;
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(dropDatabaseString);
            Drop drop = (Drop)statement;
            String databaseName = drop.getName().toString();
            if(!this.isDatabaseExists(databaseName)){
                System.out.println("ERROR : Cant drop database '" + databaseName + "'; database doesn't exist");
                return;
            }
            File file = new File(Const.DATABASE_LOAD + "/" + databaseName);
            file.delete();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
        System.out.println(Const.COMMON_SUCCESS);
    }

    //test
    public static void main(String[] args) {
        DatabaseController databaseController = new DatabaseController();
        databaseController.helpDatabase();
//        System.out.println(databaseController.createOrUseDatabases());
//        databaseController.dropDatabase("drop database mmall");
    }

    public static boolean isDatabaseExists(String databaseName){
        File file = new File(Const.DATABASE_LOAD + "/" + databaseName);
        if(file.isDirectory()){
            return true;
        }
        return false;
    }

    public void showDatabases(){
        File file = new File("database");
        File[] databases = file.listFiles();
        for(int i = 0;i < databases.length;i++){
            System.out.println(databases[i].getName());
        }
        return;
    }

    private final List<String> getDatabases(){
        List<String> databaseList = new ArrayList<String>();
        File file = new File("database");
        File[] databases = file.listFiles();
        for(int i = 0;i < databases.length;i++){
            databaseList.add(databases[i].getName().toString());
        }
        return databaseList;
    }

    public void helpDatabase() {
        List<List<String>> tableList = new ArrayList<>();
        File file = new File("database");
        File[] databases = file.listFiles();
        for(int i = 0;i < databases.length;i++){
            File file1 = databases[i];
            File[] tables = file1.listFiles();
            for(int j = 0;j < tables.length;j++){
                List<String> table = new ArrayList<>();
                table.add(file1.getName());table.add(tables[j].getName().substring(0,tables[j].getName().length()-4));
                tableList.add(table);
            }
        }

        List<List<String>> viewList = new ArrayList<>();
        File file2 = new File("view");
        File[] views = file2.listFiles();
        for(int i = 0;i < views.length;i++){
            File file1 = views[i];
            File[] view = file1.listFiles();
            for(int j = 0;j < view.length;j++){
                List<String> table = new ArrayList<>();
                table.add(file1.getName());table.add(view[j].getName().substring(0,view[j].getName().length()-4));
                viewList.add(table);
            }
        }

        List<List<String>> indexList = new ArrayList<>();
        File file3 = new File("index");
        File[] indexes = file3.listFiles();
        for(int i = 0;i < indexes.length;i++){
            File file1 = indexes[i];
            File[] index = file1.listFiles();
            for(int j = 0;j < index.length;j++){
                List<String> table = new ArrayList<>();
                table.add(file1.getName());table.add(index[j].getName().substring(0,index[j].getName().length()-4));
                indexList.add(table);
            }
        }
        printHelpDatabase(tableList,viewList,indexList);
    }

    private void printHelpDatabase(List<List<String>>table,List<List<String>>view,List<List<String>>index){
        Object[] columnNames1 = {"数据库名称","表名"};
        Object[] columnNames2 = {"数据库名称","视图名"};
        Object[] columnNames3 = {"数据库名称","索引名"};

        Object[][] rowDatas1 = new Object[table.size()][2];
        for(int i = 0;i < table.size();i++){
            for(int j = 0;j < 2;j++)
                rowDatas1[i][j] = table.get(i).get(j);
        }

        Object[][] rowDatas2 = new Object[view.size()][2];
        for(int i = 0;i < view.size();i++){
            for(int j = 0;j < 2;j++)
                rowDatas2[i][j] = view.get(i).get(j);
        }

        Object[][] rowDatas3 = new Object[index.size()][2];
        for(int i = 0;i < index.size();i++){
            for(int j = 0;j < 2;j++)
                rowDatas3[i][j] = index.get(i).get(j);
        }

        JTable tables = new JTable(rowDatas1,columnNames1);
        JTable views = new JTable(rowDatas2,columnNames2);
        JTable indexes = new JTable(rowDatas3,columnNames3);

        JScrollPane pane1 = new JScrollPane (tables);//JTable最好加在JScrollPane上
        JScrollPane pane2 = new JScrollPane (views);
        JScrollPane pane3 = new JScrollPane (indexes);

        JPanel panel = new JPanel (new GridLayout (0, 1));
        panel.setPreferredSize (new Dimension (600,400));
        panel.setBackground (Color.black);
        panel.add (pane1);
        panel.add (pane2);
        panel.add (pane3);

        JFrame frame = new JFrame ("help database");
//        frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE); 如果设置则退出就退出程序
        frame.setContentPane (panel);
        frame.pack();
        frame.setVisible(true);
    }
}
