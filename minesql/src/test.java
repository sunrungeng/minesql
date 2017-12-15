import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by srg
 *
 * @date 2017/11/19
 */
public class test {
//    public static void main(String[] args) {
////        FileWriter fw = null;
////        try {
////            fw = new FileWriter("resource//readme.txt");
////            fw.write("Mike Mike@163.com");
////            fw.write("John John@163.com");
////            fw.close();
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        System.out.println(Class.class.getClass().getResource("/").getPath());
//        int a = 5;
//        while(a > 3){
//            System.out.println(a);
//            if(a == 3)
//                continue;
//            a--;
//        }
//    }

    public test() {
    }

    public static void main(String[] args) {
//        Pattern pattern = Pattern.compile("((.*?))"); //中文括号
//        String s = "(A、B、D)。A.拒收和上交礼金、礼品名称、数量和金额B.出国(境)考察学习地点、同行人数、报销费用金额C.领导干部工薪情况D.其他需要报告的情况《湖北省农村信用社社务公开办法》规定，综合方面需要公开的内容包括（A、B、C、D）。A.辖内农村信用社的发展规划及改革举措B.年度工作目标及完成情况按《湖北省农村信用社社务公开办法》，下列说法中哪些属于人事教育方面需要公开的内容。（A、B、C）按《湖北省农村信用社社务公开办法》，下列说法中哪些属于财务管理方面需要公开的内容。（A、B、C、D）按《湖北省农村信用社社务公开办法》，下列说法中哪些属于信贷管理方面需要公开的内容。（A、C、D）《湖北省农村信用社内部审计工作制度》规定，稽核部门实施非现场审计包括以下过程：（A、B、C、D）《湖北省农村信用社内部审计工作制度》规定，审计人员在履行审计职责的过程中，拥有以下工作权限：（A、B、C、D、E、F）"; //中文括号
//        Matcher matcher = pattern.matcher(s);
//        while(matcher.find()){
//            System.out.println(matcher.group(0)); //  0是包括括号 1是只取内容
//        }
//        Pattern pattern = Pattern.compile("（(.*?)）"); //中文括号
//        String s = "(A、B、D)。A.拒收和上交礼金、礼品名称、数量和金额B.出国（境）考察学习地点、同行人数、报销费用金额C.领导干部工薪情况D.其他需要报告的情况《湖北省农村信用社社务公开办法》规定，综合方面需要公开的内容包括（A、B、C、D）。A.辖内农村信用社的发展规划及改革举措B.年度工作目标及完成情况按《湖北省农村信用社社务公开办法》，下列说法中哪些属于人事教育方面需要公开的内容。（A、B、C）按《湖北省农村信用社社务公开办法》，下列说法中哪些属于财务管理方面需要公开的内容。（A、B、C、D）按《湖北省农村信用社社务公开办法》，下列说法中哪些属于信贷管理方面需要公开的内容。（A、C、D）《湖北省农村信用社内部审计工作制度》规定，稽核部门实施非现场审计包括以下过程：（A、B、C、D）《湖北省农村信用社内部审计工作制度》规定，审计人员在履行审计职责的过程中，拥有以下工作权限：（A、B、C、D、E、F）"; //中文括号
//        Matcher matcher = pattern.matcher(s);
//        while (matcher.find()) {
//            System.out.println(matcher.group(1));   //0是包括括号1是只取内容
//        }
//        test t = new test();
//        System.out.println(t.getStringInBracket("((A、B、D))。A.拒收和上交礼金、礼品名称、数量和金额B.出国（境）考察学习地点"));
//        String s = "ada,adfa,ad,,,adsfa,";
//        s = s.toUpperCase();
////        s = s.replaceAll(",","");
//        System.out.println(s);
//        String[] str = s.split(",");
//        System.out.println(str[0]);
//        for (String stringItem : str) {
//            System.out.println(stringItem);
//        }
//        System.out.println(new Integer(2).compareTo(new Integer(2)));
//        System.out.println("values(12),(23)".indexOf("(",7));
//        Scanner scanner = new Scanner(System.in);
//        String string = scanner.nextLine();
//        System.out.println(string);
//        System.out.println();
//        String test = "林道到贤";
//        String str = "林_贤";
//        str = str.replaceAll("_",".");
//        str = str.replaceAll("%","(.*)");
//        Pattern pattern = Pattern.compile(str);
//        Matcher matcher = pattern.matcher(test);
////        boolean is = pattern.matcher(test);
////        System.out.println("\\S");
//        System.out.println(str);
////        System.out.println(test.matches(str));
//        System.out.println(matcher.matches());
//        List<List<String>> tt = new ArrayList<>();
//        List<String> t1 = new ArrayList<>();
//        t1.add("a");t1.add("2");t1.add("c");
//        List<String> t2 = new ArrayList<>();
//        t2.add("c");t2.add("1");t2.add("c");
//        List<String> t3 = new ArrayList<>();
//        t3.add("z");t3.add("3");t3.add("c");
//        tt.add(t1);tt.add(t2);tt.add(t3);
//        Collections.sort(tt, new Comparator<List<String>>() {
//            @Override
//            public int compare(List<String> o1, List<String> o2) {
//                return Integer.parseInt(o1.get(1))-Integer.parseInt(o2.get(1));
//            }
//        });
//
//        for(int i = 0;i < tt.size();i++){
//            for(int j = 0;j < tt.get(0).size();j++)
//                System.out.print(tt.get(i).get(j));
//            System.out.println();
//        }
//        String str = "sss";
//        String[] strings = str.split(" ");
//        String s = strings[0];
//        System.out.println(s);
//        List<Integer> num = new ArrayList<>();
//        for(int i = 1;i <= 5;i++){
//            num.add(i);
//        }
//        System.out.println(num.indexOf(3));
//        for(int i = 0;i < 5;i++)
//            num.remove((Integer) 1);
//        for(int i = 0;i < num.size();i++)
//            System.out.println(num.get(i));
        String selectString = "select sno,sname from teacher left join student on student.tno = teacher.tno and student.sno between 10 and 20 and sno in (select * from student) where student.sno = 5";
//        String selectString = "select sno,sname from student where sno in (select sno from teacher where tno > 0)";
        Statement statement = null;
        try {
            statement = CCJSqlParserUtil.parse(selectString);
            Select select = (Select) statement;
            // *********select body items内容
            List<String> str_items = test.test_select_items(selectString);
            for (int i = 0;i < str_items.size();i++){
                System.out.print(str_items.get(i));
            }
            System.out.println();
            // **********select table
            List<String> tableList = test.test_select_table(selectString);
            for (int i = 0;i < tableList.size();i++){
                System.out.print(tableList.get(i));
            }
            System.out.println();
            // **********select table with join
            List<String> tablewithjoin = test.test_select_join(selectString);
            if(tablewithjoin != null)
            for (int i = 0;i < tablewithjoin.size();i++){
                System.out.print(tablewithjoin.get(i));
            }
            System.out.println();
            // // *******select where
            String str = test.test_select_where(selectString);
            System.out.println(str);
            String sqlstring = tablewithjoin.get(0).substring(tablewithjoin.get(0).indexOf("ON") + 2,tablewithjoin.get(0).length() - 1);
            System.out.println(sqlstring);
            // // ******select group by
            List<String> str_groupby = test.test_select_groupby(selectString);
            if(str_groupby != null)
            for (int i = 0;i < str_groupby.size();i++){
                System.out.print(str_groupby.get(i));
            }
            System.out.println();
            // //**************select order by
            List<String> str_orderby = test.test_select_orderby(selectString);
            if(str_orderby != null)
            for (int i = 0;i < str_orderby.size();i++){
                System.out.print(str_orderby.get(i));
            }
            System.out.println();
        } catch (JSQLParserException e) {
            e.printStackTrace();
        }

    }

    private String getStringInBracket(String string){

        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find())
            return matcher.group();
        else
            return "";
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
    // **********TablesNamesFinder:Find all used tables within an select
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
