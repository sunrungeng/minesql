package minesql.controller;

import minesql.common.Const;
import minesql.pojo.User;
import minesql.util.UserUtils;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.drop.Drop;

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
            File database = new File(Const.DATABASELOAD+databaseName);
            database.mkdirs();
            System.out.println("Query OK");
        }
    }

    public void dropDatabase(User user,String dropDatabaseString){
        if(!UserUtils.judgePrivilege(user,"drop")){
            return;
        }
        try {
            Statement statement = CCJSqlParserUtil.parse(dropDatabaseString);
            Drop drop = (Drop)statement;
            String databaseName = drop.getName();
            if(!this.isDatabaseExists(databaseName)){
                System.out.println("ERROR : Cant drop database '" + databaseName + "'; database doesn't exist");
                return;
            }
            File file = new File(Const.DATABASELOAD + "/" + databaseName);
            file.delete();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }
    }

    //test
    public static void main(String[] args) {
        DatabaseController databaseController = new DatabaseController();

//        System.out.println(databaseController.createOrUseDatabases());
//        databaseController.dropDatabase("drop database mmall");
    }

    public static boolean isDatabaseExists(String databaseName){
        File file = new File(Const.DATABASELOAD + "/" + databaseName);
        if(file.isDirectory()){
            return true;
        }
        return false;
    }

    public static void showDatabases(){
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

}
