package user;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import javax.swing.JOptionPane;

/**
 *
 * @author Mr. Robot
 */
public class User{
    private double num1, num2, num3;
    private File userfile;
    private FileWriter filew;
    private BufferedWriter buffw;
    private ArrayList<Character> encrypt = new ArrayList<Character>();
    String enc;
    public User(File userfile){
        this.userfile = new File(".userdata"+File.separator+"info-"+userfile.getName());
        try{
            filew = new FileWriter(this.userfile.getAbsoluteFile(), true);
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Tenemos problemas para acceder a su registro", "Problemas en "+userfile.toString(), JOptionPane.WARNING_MESSAGE);
        }
        buffw = new BufferedWriter(filew);
    }
    public User(char pass[], char user[]){
        numbers(pass, user);
        enc = encrypt.toString().replaceAll(", ", "");
        enc = enc.substring(1, enc.length()-1);
        byte encoded_bytes[] = Base64.getEncoder().encode(enc.getBytes());
        enc = new String(encoded_bytes);
    }
    public User(char pass[], String user){
        numbers(pass, user.toCharArray());
        File dir = new File(".userdata");
        if (!dir.exists()){
            try{
                dir.mkdir();
                if (System.getProperty("os.name").toLowerCase().contains("windows"))
                    Runtime.getRuntime().exec("attrib +H .userdata");
            } catch(SecurityException se){
                JOptionPane.showMessageDialog(null, "No podemos crear el directorio", "ERROR DE SEGURIDAD", JOptionPane.WARNING_MESSAGE);
                return;
            } catch(IOException ex){
                //folder no oculto
            }
        }
        try{
            FileOutputStream fout=new FileOutputStream(".userdata"+File.separator+user+".dat");  
            DataOutputStream dout=new DataOutputStream(fout);
            String str = encrypt.toString().replaceAll(", ", "");
            str = str.substring(1, str.length()-1);
            byte encoded_bytes[] = Base64.getEncoder().encode(str.getBytes());
            dout.writeUTF(new String(encoded_bytes));
            dout.flush();
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Parece que estamos teniendo problemas para guardar sus datos", "ERROR EN EL GUARDADO", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void addTimes(String date){
        try{
            buffw.write(date);
            buffw.newLine();
            buffw.flush();
        } catch(Exception e){
            JOptionPane.showMessageDialog(null, "Parece que estamos teniendo problemas para guardar sus datos", "ERROR EN EL GUARDADO", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void numbers(char pass[], char user[]){
        double length_pass = pass.length, length_user = user.length, i = length_pass-1, j = length_user-1, llave_pass = (double)pass[(int)i], llave_user = (double)user[(int)j];
        while(i > -1)
            llave_pass *= ((double)pass[(int)i--]+i);
        while(j > -1)
            llave_user *= ((double)user[(int)j--]+j);
        double key = llave_pass * llave_user * length_pass * length_user * llave_pass * llave_user * length_pass * length_user;
        cipher(key);
    }
    private void cipher(double key){
        num1 = key / 7;
        num2 = key - num1;
        num3 = Math.round(Math.log(key) * Math.log(num1) * Math.log(num2));
        if (num3 > 15){
            encrypt.add((char)num3);
            cipher(key / Math.log(key));
        }
    }
    public static boolean exist(String user){
        FilenameFilter datFilter = (File file, String string) -> {return string.endsWith(".dat");};
        File userdata = new File(".userdata");
        File[] users = userdata.listFiles(datFilter);
        int i = 0, total = users.length; 
        while (i < total){
            if (users[i++].getName().equals(user+".dat"))
                return true;
        }
        return false;
    }
}
