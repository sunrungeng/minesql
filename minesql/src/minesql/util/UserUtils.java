package minesql.util;

import minesql.common.Const;
import minesql.pojo.User;

/**
 * Created by srg
 *
 * @date 2017/12/6
 */
public class UserUtils {

    public static boolean judgePrivilege(User user, String privilege){
        if(!user.getPrivileges().contains(privilege)){
            System.out.println(Const.Error.DONT_HAVE_PRIVILEGE);
            return false;
        }
        return true;
    }
}
