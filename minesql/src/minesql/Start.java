package minesql;

import minesql.common.Const;
import minesql.controller.*;
import minesql.pojo.User;
import minesql.pojo.View;
import minesql.util.StringUtils;

import java.io.*;
import java.util.Scanner;

/**
 * Created by srg
 *
 * @date 2017/11/19
 */
public class Start {
    public Start() {
    }

    public static void main(String[] args) {
        Start start = new Start();
        //welcome
        start.welcome();
        UserController userController = new UserController();
        DatabaseController databaseController = new DatabaseController();
        TableController tableController = new TableController();
        ViewController viewController = new ViewController();
        IndexController indexController = new IndexController();
        //login
        User user = userController.login();

        String databaseName = null;

        //输入命令
        Scanner sc = new Scanner(System.in);
        String str = null;
        System.out.print(Const.SQL_PREFIX1);
        str = sc.nextLine();

        while (!StringUtils.equalsIgnoreCase(str,"exit")) {
            if(str.equalsIgnoreCase("help") || str.equalsIgnoreCase("\\h")){
                start.help();
                System.out.print(Const.SQL_PREFIX1);
                str = sc.nextLine();
            }
            if (str.endsWith(";")) {
                str = str.substring(0, str.length() - 1);
                str = str.trim();
                if(str.startsWith("use")){
                    databaseName = databaseController.useDatabase(str);
                } else if (StringUtils.equalsIgnoreCase(str,"show databases")) {
                    databaseController.showDatabases();
                } else if (StringUtils.equalsIgnoreCase(str,"show tables")) {
                    tableController.showTables(databaseName);
                } else if (StringUtils.equalsIgnoreCase(str,"show users")){
                    userController.showUsers();
                } else if (str.startsWith("help")){
                    if(str.startsWith("help table")){
                        tableController.helpTable(databaseName,str);
                    } else if (str.equals("help database")){
                        databaseController.helpDatabase();
                    } else if (str.startsWith("help view")){
                        viewController.helpView(databaseName,str);
                    } else if (str.startsWith("help index")){
                        indexController.helpIndex(databaseName,str);
                    } else{
                        System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                    }
                } else if (str.contains("grant")){
                    userController.grant(user,str);
                } else if (str.contains("revoke")){
                    userController.revoke(user,str);
                } else if (str.startsWith("set user")){
                    userController.setUserRole(user,str);
                } else if (StringUtils.containsString(str,"create")) {
                    if (StringUtils.containsString(str,"table")) {
                        tableController.createTable(user,databaseName, str);
                    } else if (StringUtils.containsString(str,"database")) {
                        databaseController.createDatabase(user,str);
                    } else if (StringUtils.containsString(str,"user")) {
                        userController.createUser(user,str);
                    } else if (StringUtils.containsString(str,"view")) {
                        viewController.createView(user,databaseName,str);
                    } else if (StringUtils.containsString(str,"index")) {
                        indexController.createIndex(user,databaseName,str);
                    } else {
                        System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                    }
                } else if (StringUtils.containsString(str,"select")) {
                    if(str.contains("view")){
                        viewController.select(user,databaseName,str);
                    }else{
                        tableController.select(user,databaseName,str);
                    }
                } else if (StringUtils.containsString(str,"insert")) {
                    tableController.insertTableData(user,databaseName, str);
                } else if (StringUtils.containsString(str,"delete")) {
                    tableController.deleteTableData(user,databaseName,str);
                } else if (StringUtils.containsString(str,"update")){
                    tableController.updateTableData(user,databaseName,str);
                } else if (StringUtils.containsString(str,"drop")) {
                    if (StringUtils.containsString(str,"table")) {
                    tableController.dropTable(user,databaseName,str);
                    } else if (StringUtils.containsString(str,"database")) {
                    databaseController.dropDatabase(user,str);
                    } else if (StringUtils.containsString(str,"view")) {
                    viewController.dropView(user,databaseName,str);
                    } else if (StringUtils.containsString(str,"index")) {
                    indexController.dropIndex(user,databaseName,str);
                    } else {
                        System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                    }
                }
                System.out.print(Const.SQL_PREFIX1);
                str = sc.nextLine();
            }else{
                System.out.print(Const.SQL_PREFIX2);
                str = str + sc.nextLine();
            }
        }
        System.out.println("bye");
    }

    private void help() {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(new File("readme.txt"));
            br = new BufferedReader(fr);
            String str = br.readLine();
            while (str != null) {
                System.out.println(str);
                str = br.readLine();
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            System.out.println("找不到文件");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("输入错误");
        }
    }

    private void welcome() {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(new File("index.txt"));
            br = new BufferedReader(fr);
            String str = br.readLine();
            while (str != null) {
                System.out.println(str);
                str = br.readLine();
            }
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            System.out.println("找不到文件");
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println("输入错误");
        }
    }
}
