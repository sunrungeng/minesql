package minesql.controller;

import minesql.common.Const;
import minesql.pojo.User;
import minesql.util.UserUtils;

import java.io.*;

/**
 * Created by srg
 *
 * @date 2017/11/24
 */
public class ViewController {

    //create view viewname as select sname from student
    public void createView(User user,String database, String str) {
        if(!user.getPrivileges().contains("create")){
            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
            return;
        }
        if(database == null || database.equals("")){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        String[] strings = str.split(" ");
        String viewName = strings[2];
//        if(viewName.contains("(")){
//            int index = viewName.indexOf("(");
//            viewName = viewName.substring(0,index);
//        }
        String s = str.substring(str.indexOf("as") + 2,str.length()).trim();
        File dir = new File(Const.VIEW_LOAD + database);
        isDirExist(dir);
        File file = new File(Const.VIEW_LOAD + database + "/" + viewName + ".txt");
        if(file.exists()){
            System.out.println(Const.Error.VIEW_EXISTED);
            return;
        }
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file);
            fw.write(s);
            fw.flush();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(Const.COMMON_SUCCESS);
    }

    public void dropView(User user,String database,String str){
        if(!user.getPrivileges().contains("drop")){
            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
            return;
        }
        if(database == null || database.equals("")){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        String[] strings = str.split(" ");
        String viewName = strings[2];
        File dir = new File(Const.VIEW_LOAD + database);
        if(!dir.exists()){
            System.out.println(Const.Error.DATABASE_NOT_EXISTED);return;
        }
        File file = new File(Const.VIEW_LOAD + database + "/" + viewName + ".txt");
        if(!file.exists()){
            System.out.println(Const.Error.VALUE_NOT_EXISTED);
            return;
        }
        file.delete();
        System.out.println(Const.COMMON_SUCCESS);
    }
    //判断是否为文件夹,如果不是或着不存在创建一个
    public static void isDirExist(File file){
        if(!file.isDirectory() || !file.exists()){
            file.mkdirs();
        }
    }

    public void helpView(String databaseName, String str) {
        if(databaseName == null || databaseName.equals("")){
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        String[] strings = str.split(" ");
        if(strings.length != 3){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
        String viewName = strings[2];
        File file = new File(Const.VIEW_LOAD+databaseName+"/"+viewName+".txt");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String string = null;
            while((string = br.readLine()) != null){
                System.out.println(string);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UserController userController = new UserController();
//        User user = userController.login();
        ViewController viewController = new ViewController();
//        viewController.createView(user,"test","create view test1(sno,sname) as ...");
//        viewController.dropView(user,"test","drop view test1");
        viewController.helpView("test","help view test1");
    }

    //select * from view studentm
    public void select(User user, String databaseName,String sql) {
        if (!UserUtils.judgePrivilege(user, "select")) {
            return;
        }
        if (!DatabaseController.isDatabaseExists(databaseName)) {
            System.out.println(Const.Error.NO_DB_SELECTED);
            return;
        }
        File dir = new File(Const.VIEW_LOAD + databaseName);
        if(!dir.isDirectory()){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
        String[] strings = sql.split(" ");
        String viewName = strings[4];
        File file = new File(Const.VIEW_LOAD + databaseName + "/" + viewName + ".txt");
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String str = br.readLine();
            TableController tableController = new TableController();
            tableController.select(user,databaseName,str);
        } catch (FileNotFoundException e) {
            System.out.println(Const.Error.VIEW_NOT_EXISTED);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
