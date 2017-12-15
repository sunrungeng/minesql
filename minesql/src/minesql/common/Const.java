package minesql.common;

/**
 * Created by srg
 *
 * @date 2017/11/19
 */
public class Const {

    public static String DATABASE_LOAD = "database/";
    public static String VIEW_LOAD = "view/";
    public static String INDEX_LOAD = "index/";
    public static String SQL_PREFIX1 = "minesql>";
    public static String SQL_PREFIX2= "       >";
    public static String COMMON_SUCCESS = "Query Ok";
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
        String VALUE_EXISTED = "ERROR : The value is existed";
        String VALUE_TYPE_ERROR = "ERROR : The value's type is not right";
        String VIEW_EXISTED = "ERROR : The view is existed";
        String VIEW_NOT_EXISTED = "ERROR : The view is not existed";
        String VALUE_NOT_EXISTED = "ERROR : The view is not existed";
        String DATABASE_NOT_EXISTED = "ERROR : The database is not existed";
        String INDEX_NOT_EXISTED = "ERROR : The index is not existed";
    }

    public interface LOGIN {
        String SUCCESS = "Login Success!";
        String FAIL = "Login Fail!";
    }

}
