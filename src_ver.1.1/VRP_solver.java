import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class VRP_solver {
    static ArrayList<String> logList; // 最新のログをとっておくリスト
    static Integer TargetValue; // 現在最適な答え
    static ArrayList<Integer> xList; // 現在最適な答えに対応する道筋
    static Integer count;
    static String SATorUNSAT;
    static String[] filename;

	public static void main(String[] args) throws IOException {
		String io_csp_file = args[0];
        filename = io_csp_file.split("\\.", 0);
        String[] str = {"/usr/local/bin/sugar", "-vv", "-hybrid", "-o", "hy8", "-solver", "/usr/local/bin/clasp", io_csp_file};

		Process p = null;

        try {
            Runtime runtime = Runtime.getRuntime();
            p = runtime.exec(str);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        logList = new ArrayList<String>();
        xList = new ArrayList<Integer>();
        count = 1;

        while(pickupLog(br)){ // UNSATならそれ以上最適な答えはない
            System.out.println("---------------" + io_csp_file + "[" + count + "]-----------------");
            System.out.println(SATorUNSAT);
            System.out.println("count:" + count);
            System.out.println("TargetValue:" + TargetValue);
            for(Integer x : xList){
            	System.out.print(x);
            	System.out.print("→ ");
            }
            System.out.println("OK!");

            writeLog(io_csp_file);

            rewriteCSP(io_csp_file);

        	try {
                Runtime runtime = Runtime.getRuntime();
        		String[] str_after = {"/usr/local/bin/sugar", "-vv", "-hybrid", "-o", "hy8", "-solver", "/usr/local/bin/clasp", "csp/" + filename[0] + "_" + count + ".csp"};
                p = runtime.exec(str_after);
            } catch (IOException e) {
                e.printStackTrace();
            }

            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        	// xList = new ArrayList<Integer>();

            count++;
        }

        System.out.println("-------------" + filename[0] +"." + filename[1] + "[result]" + "--------------");
        System.out.println("Optimal TagetValue: " + TargetValue);
        System.out.print("Optimal Route: ");
        for(Integer x : xList){
            System.out.print(x);
            System.out.print("→ ");
        }
        System.out.println("OK!");
        System.out.println();


    }

	/*
    出力を読み取るメソッド．
    */
    private static boolean pickupLog(BufferedReader br) throws IOException {
        logList.clear();

        String line = "";
        while (line != null) {
            line = br.readLine();
            logList.add(line);
        }

        for(String a : logList) {
            if(a != null) {
                if (a.charAt(0) == 's') {
                    SATorUNSAT = a;
                    if (a.equals("s UNSATISFIABLE")) {
                        System.out.println("---------------" + filename[0] + "." + filename[1] + "[" + count + "]-----------------");
                        System.out.println("s UNSATISFIABLE");
                        return false;
                    }
                }
            }
        }

        xList = new ArrayList<Integer>();

        for(String a : logList) {
            if(a != null) {
                if (a.length() > 3 && a.substring(0, 3).equals("a x")) {
                    xList.add(Integer.parseInt(a.substring(5).trim()));
                }
                if (a.length() > 10 && a.substring(0, 10).equals("a distance")) {
                    //a.replace('a',' '); a.replaceAll("distance","");  a.trim();
                    TargetValue = Integer.parseInt(a.substring(11));
                }
            }
        }


        return true;
    }

	/*
    CSPを書き換えるメソッド．
    */
    private static void rewriteCSP(String file) throws IOException {
        ArrayList<String> list = new ArrayList<String>();
        int line = 0; // String[] filename = file.split("\\.",0);

    	if(count != 1){
    		file = "csp/" + filename[0] + "_" + (count-1) + "." +filename[1];
    	}
    	try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));) {
    		String s = br.readLine();
    		while(s != null) {
            	list.add(s);
            	s=br.readLine();
            }
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("csp/" + filename[0] + "_" + count + "." +filename[1])));) {
            int i = 0;
        	while(i<(list.size()-1)) {
            	bw.write(list.get(i));
            	bw.newLine();
            	i++;
            }
        	// String[] dis_list = list.get(i).split(" ");
        	// TargetValue = Integer.parseInt(dis_list[3].replaceAll("\\)","").trim());
        	bw.write("(le (add distance) " + (TargetValue-1) + ")");

            bw.flush();
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
 	現在のログを書き込む処理をするメソッド
 	*/
    private static void writeLog(String file) {
    	// String[] filename = file.split("\\.",0);
    	try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("log/" + filename[0] + "_" + count + ".log")));) {
            for(String a: logList) {
                if(a != null) {bw.write(a); bw.newLine();}
                //if(a != null && a!= "null") {bw.write(a); bw.newLine();}
            }
            bw.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
