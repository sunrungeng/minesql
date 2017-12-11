import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
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
        System.out.println("values(12),(23)".indexOf("(",7));
        Scanner scanner = new Scanner(System.in);
        String string = scanner.nextLine();
        System.out.println(string);
    }

    private String getStringInBracket(String string){

        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find())
            return matcher.group();
        else
            return "";
    }

}
