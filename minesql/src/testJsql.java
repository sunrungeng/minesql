
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by srg
 *
 * @date 2017/11/26
 */
public class testJsql {

    public static void test_select() throws JSQLParserException {
        String sql = "select one.sno,sname from one,two where one.sno = two.sno";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select = (Select) parserManager.parse(new StringReader(sql));
        PlainSelect plain = (PlainSelect) select.getSelectBody();
        List<SelectItem> selectitems = plain.getSelectItems();
        List<String> str_items = new ArrayList<String>();
        if (selectitems != null) {
            for (int i = 0; i < selectitems.size(); i++) {
                str_items.add(selectitems.get(i).toString());
                System.out.println(selectitems.get(i).toString());
            }
        }

        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(select);
        for(String string : tableList){
            System.out.println(string);
        }

        Expression where_expression = plain.getWhere();
        String str = where_expression.toString();
        System.out.println(str);
    }

    public static void test_insert() throws JSQLParserException {

    }

    public static void main(String[] args) throws JSQLParserException {

        testJsql testJsql = new testJsql();
        testJsql.test_select();
//        testJsql.test_insert();
    }
}
