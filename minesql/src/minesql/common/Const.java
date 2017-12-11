package minesql.common;

/**
 * Created by srg
 *
 * @date 2017/11/19
 */
public class Const {

    public static String DATABASELOAD = "database/";
    public static String SQL_PREFIX1 = "minesql>";
    public static String SQL_PREFIX2= "       >";

    public static String TIP = "If you don't konw how to use minesql,you can input '\\h' or 'help' to know the usage!";

    public interface  Role{
        String ROLE_ADMIN = "manager"; //超级管理员
        String ROLE_USER = "user";  //普通用户
    }

    public interface Privilege{
        int CREATE_USER = 1; //创建用户
        int CREATE = 2;      //创建数据库、表、视图、索引
        int SELECT = 3;      //select权限
        int DELETE = 4;      //delete权限
        int UPDATE = 5;      //update权限
        int INSERT = 6;      //insert权限
    }

    public interface Error{
        String SQL_SYNTAX_ERROR = "ERROR : You have an error in your SQL syntax;";
        String DONT_HAVE_PRIVILEGE = "ERROR : The user doesn't have this privilege";
        String GRANT_REPEAT_PRIVILEGE = "ERROR : This user already has this privilege";
        String NO_DB_SELECTED = "ERROR : No database selected";
    }

    public interface LOGIN {
        String SUCCESS = "Login Success!";
        String FAIL = "Login Fail!";
    }
}
