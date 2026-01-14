package org.java.learn.summary.suanfa;

import java.util.ArrayList;
import java.util.List;

/**
 * description: TestGetIP 输入一段字数字字符串，分析其所有 ip组合情况.
 * date: 2024/9/23 11:26
 * author: yanlongdu
 */
public class GetIPs {

    public static void main(String[] args) {
        GetIPs testApi = new GetIPs();
        String ipStr = "25525522135";
//        给出的字符串为"25525522135",
//        返回["255.255.22.135", "255.255.221.35"]
        List<String> ipArrs = testApi.getIp(ipStr);
        for(String ip1 : ipArrs){
            System.out.println("ip = " + ip1);
        }
    }

    public List<String> getIp(String ipStr){

        List<String> ipArrs = new ArrayList<>();

        if(ipStr != null && !"".equals(ipStr)&& ipStr.length()>4 && ipStr.length()<13 && !ipStr.startsWith("0")){
            List<String> ipArr = new ArrayList<>();
            checkValidate(ipStr,0, ipArrs, ipArr);
        }

        return ipArrs;
    }

    public void checkValidate(String ipStr, int idx, List<String> ipArrs, List<String> ipArr){

//        给出的字符串为"25525522135",
        if (ipArr != null && ipArr.size() >= 4) {
            if (idx == ipStr.length()) {
                ipArrs.add(String.join(".", ipArr));
            }
            return;
        }

        for(int idj = 1; idj<=3 ; idj++){
            if(idx+idj >ipStr.length()){
                return;
            }
//            System.out.println("ipStr = " + ipStr + ", idx = " + idx + ", ipArr = " + String.join(".", ipArr) );
//            System.out.println("ipArr != null "+ (ipArr != null) + "  ipArr.size() == 4 " + (ipArr.size() == 4) + " idx == ipStr.length() " + (idx == ipStr.length()) + "   "+ idx + "   "+ (ipStr.length()));

            String ip = ipStr.substring(idx, idx + idj);
            List<String> tmpArr = new ArrayList<>();
            try {
                int ipInt = Integer.parseInt(ip);
                if (0 <= ipInt && ipInt <= 255) {
                    tmpArr.addAll(ipArr);
                    tmpArr.add(ip);
                    checkValidate(ipStr, idx + idj , ipArrs,  tmpArr);
                }
            }catch (Exception exp){
                System.out.println("ipStr parseException ");
            }
        }

    }
}
