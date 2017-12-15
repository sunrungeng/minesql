package minesql.controller;

import minesql.common.Const;
import minesql.pojo.User;
import minesql.util.MD5Util;
import minesql.util.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by srg
 * @date 2017/11/19
 */
public class UserController{

    public User login(){
        User user = new User();
        try {
            FileInputStream in = new FileInputStream(new File("user.xls"));
            POIFSFileSystem fin = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Scanner scanner = new Scanner(System.in);
            String loginStr = scanner.nextLine();
            String[] strings = loginStr.split(" ");
            if(!strings[0].equals("minesql") || !strings[1].equals("-u") || !strings[3].equals("-p")){
                System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                return this.login();
            }
            String username = strings[2];
            String password = MD5Util.MD5EncodeUtf8(strings[4]);
            for (int i = 1;i <= sheet.getLastRowNum();i++){
                HSSFRow row = sheet.getRow(i);
                Cell cell1 = row.getCell(1);
                if(!username.equals(cell1.getStringCellValue())){
                    continue;
                } else {
                    Cell cell2 = row.getCell(2);
                    if (!password.equals(cell2.getStringCellValue())){
                        System.out.println("ERROR : Access denied for user '" + username + "'");
                        return this.login();
                    } else {
                        Cell cell3 = row.getCell(3);
                        Cell cell4 = row.getCell(4);
                        String role = cell3.getStringCellValue();
                        String privilege = cell4.getStringCellValue();
                        List<String> privileges = new ArrayList<String>();
                        String[] privilegeStrs = privilege.split(",");
                        for (int j = 0;j < privilegeStrs.length;j++) {
                            privileges.add(privilegeStrs[j]);
                        }
                        user.setUserName(username);
                        user.setRole(role);
                        user.setPrivileges(privileges);
                        System.out.println(Const.LOGIN.SUCCESS);
                        System.out.println(Const.TIP);
//                        return user;
                        break;
                    }
                }
            }
            fin.close();
            in.close();
            return user;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createUser(User user,String createString){
        if(user.getRole().equals("user")){
            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
            return;
        }
        String[] strings = createString.split(" ");
        String userName = strings[2];
        String passWord = strings[3];
        passWord = MD5Util.MD5EncodeUtf8(passWord);
        try {
            File file = new File("user.xls");
            FileInputStream in = new FileInputStream(file);
            POIFSFileSystem fs = new POIFSFileSystem(in);
            FileOutputStream fos = new FileOutputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(fs);
            HSSFSheet sheet = workbook.getSheetAt(0);
            for (int i = 1;i <= sheet.getLastRowNum();i++){
                HSSFRow row = sheet.getRow(i);
                HSSFCell cell = row.getCell(0);
                cell.setCellType(CellType.STRING);
                if(userName.equals(cell.getStringCellValue())){
                    System.out.println("ERROR : The user has existed");
                    return;
                }
            }
            HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
            Cell cell1 = row.createCell(0);
            Cell cell2 = row.createCell(1);
            Cell cell3 = row.createCell(2);
            Cell cell4 = row.createCell(3);
            Cell cell5 = row.createCell(4);

            cell1.setCellType(CellType.STRING);
            cell2.setCellType(CellType.STRING);
            cell3.setCellType(CellType.STRING);
            cell4.setCellType(CellType.STRING);
            cell5.setCellType(CellType.STRING);

            //创建用户默认拥有insert,delete,update,select权限
            String privileges = "insert,delete,update,select";
            cell1.setCellValue(sheet.getLastRowNum());
            cell2.setCellValue(userName);
            cell3.setCellValue(passWord);
            cell4.setCellValue(Const.Role.ROLE_USER);
            cell5.setCellValue(privileges);

            workbook.write(fos);
            fos.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UserController() {

    }

    public void grant(User user,String sql) {
        if(user.getRole().equals("user")){
            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
            return ;
        }
        //grant create to srg
        String[] strings = sql.split(" ");
        if(!StringUtils.equalsIgnoreCase(strings[0],"grant") || !StringUtils.equalsIgnoreCase(strings[2],"to")){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
        String userName = strings[3];
        String grantPrivilege = strings[1];
        String[] grantPrivileges = grantPrivilege.split(",");
        File file = new File("user.xls");
        try {
            FileInputStream in = new FileInputStream(file);
            POIFSFileSystem fis = new POIFSFileSystem(in);
            FileOutputStream fos = new FileOutputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            HSSFSheet sheet = workbook.getSheetAt(0);
            for(int i = 1;i <= sheet.getLastRowNum();i++){
                HSSFRow row = sheet.getRow(i);
                Cell cell1 = row.getCell(1);
                if(!cell1.getStringCellValue().equals(userName)){
                    continue;
                } else {
                    Cell cell2 = row.getCell(4);
                    String privileges = cell2.getStringCellValue();
                    for (int j = 0;j < grantPrivileges.length;j++){
                        if(StringUtils.containsString(privileges,grantPrivileges[j])){
                            System.out.println(Const.Error.GRANT_REPEAT_PRIVILEGE);
                            return;
                        } else {
                            privileges = privileges + "," + grantPrivileges[j];
                        }
                    }
                    cell2.setCellValue(privileges);
                }
            }
            workbook.write(fos);
            fos.close();
            fis.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void revoke(User user,String sql) {
        if(user.getRole().equals("user")){
            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
            return ;
        }
        //revoke create from srg
        String[] strings = sql.split(" ");
        if(!StringUtils.equalsIgnoreCase(strings[0],"revoke") || !StringUtils.equalsIgnoreCase(strings[2],"from")){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
        String userName = strings[3];
        String grantPrivilege = strings[1];
        String[] grantPrivileges = grantPrivilege.split(",");
        File file = new File("user.xls");
        try {
            FileInputStream in = new FileInputStream(file);
            POIFSFileSystem fis = new POIFSFileSystem(in);
            FileOutputStream fos = new FileOutputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            HSSFSheet sheet = workbook.getSheetAt(0);
            for(int i = 1;i <= sheet.getLastRowNum();i++){
                HSSFRow row = sheet.getRow(i);
                Cell cell1 = row.getCell(1);
                if(!cell1.getStringCellValue().equals(userName)){
                    continue;
                } else {
                    Cell cell2 = row.getCell(4);
                    String privileges = cell2.getStringCellValue();
                    String[] privilegeStrs = privileges.split(",");
                    List<String> privilegeList = new ArrayList<String>();
                    for (int j = 0;j < privilegeStrs.length;j++){
                        privilegeList.add(privilegeStrs[j]);
                    }
                    for(int j = 0;j < grantPrivileges.length;j++){
                        if(!privilegeList.contains(grantPrivileges[j])){
                            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
                            return;
                        } else {
                            privilegeList.remove(grantPrivileges[j]);
                        }
                    }
                    privileges = "";
                    for(int j = 0;j < privilegeList.size();j++) {
                        privileges = privileges + privilegeList.get(j) + ",";
                    }
                    privileges = privileges.substring(0,privileges.length() - 1);
                    cell2.setCellValue(privileges);
                }
            }
            workbook.write(fos);
            fos.close();
            fis.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        UserController userController = new UserController();
        User user = userController.login();
//        System.out.println("1");
//        userController.revoke(user,"revoke delete from srg");
//        userController.setUserRole(user,"set user srg manager");
//        userController.grant(user,"grant delete,select to srg");
        userController.createUser(user,"create user srg srg");
//        File file = new File("user.xls");
//        try {
//            file.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void setUserRole(User user, String str) {
        if(user.getRole().equals("user")){
            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
            return ;
        }
        String[] strings = str.split(" ");
        if(strings.length != 4 || !strings[3].equals("manager")){
            System.out.println(Const.Error.SQL_SYNTAX_ERROR);
            return;
        }
        String username = strings[2];
        try {
            File file = new File("user.xls");
            FileInputStream in = new FileInputStream(file);
            POIFSFileSystem fis = new POIFSFileSystem(in);
            FileOutputStream fos = new FileOutputStream(file);
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            HSSFSheet sheet = workbook.getSheetAt(0);
            int num = 0;
            for(int i = 1;i <= sheet.getLastRowNum();i++){
                HSSFRow row = sheet.getRow(i);
                HSSFCell cell = row.getCell(1);
                if(cell.getStringCellValue().equals(username)){
                    num = i;
                    HSSFCell cell1 = row.getCell(3);
                    if(cell1.getStringCellValue().equals("manager")){
                        System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                        return;
                    }else {
                        cell1.setCellValue("manager");
                    }
                    break;
                }
            }
            if(num == 0){
                System.out.println(Const.Error.SQL_SYNTAX_ERROR);
                return;
            }
            workbook.write(fos);
            fos.close();
            fis.close();
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showUsers() {
        File file = new File("user.xls");
        try {
            InputStream in = new FileInputStream(file);
            POIFSFileSystem fin = new POIFSFileSystem(in);
            HSSFWorkbook workbook = new HSSFWorkbook(fin);
            HSSFSheet sheet = workbook.getSheetAt(0);
            List<List<String>> rowData = new ArrayList<>();
            List<String> columnName = new ArrayList<>();
            columnName.add("id");
            columnName.add("username");
            columnName.add("role");
            columnName.add("privilege");

            for(int i = 1;i <= sheet.getLastRowNum();i++){
                HSSFRow row = sheet.getRow(i);
                HSSFCell cell1 = row.getCell(0);
                HSSFCell cell2 = row.getCell(1);
                HSSFCell cell3 = row.getCell(3);
                HSSFCell cell4 = row.getCell(4);
                List<String> temp = new ArrayList<>();
                cell1.setCellType(CellType.STRING);
                temp.add(cell1.getStringCellValue());
                temp.add(cell2.getStringCellValue());
                temp.add(cell3.getStringCellValue());
                temp.add(cell4.getStringCellValue());
                rowData.add(temp);
            }

            TableController.printTable("users",rowData,columnName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}