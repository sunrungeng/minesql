package minesql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by srg
 *
 * @date 2017/11/20
 */
public class Init {
    public static void main(String[] args) {
//        FileWriter fw = null;
//        try {
//            Scanner sc = new Scanner(System.in);
//
//            String str = "";
//            for(int i = 0;i < 5;i++){
//                str = str + sc.nextLine() + "\r\n";
//            }
//            fw.write(str);
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        File databases = new File("database/test.txt");
        FileWriter fr = null;
        try {
            fr = new FileWriter(databases);
            fr.write("dafda");
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
