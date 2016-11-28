/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoconfigurerjava;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import rconnector.ConnectionType;
import rconnector.RException;

/**
 *
 * @author Miquéias Fernandes
 */
public class Autoconfigurerjava {

    private static String pathWindows = "setx path \"%PATH%;*\"";
    private static String pathLinux = "echo 'export PATH=$PATH:*'  >> ~/.bash_profile";
    private static String envWindows = "setx # \"*\" /m";
    private static String envLinux = "echo 'export #=*'  >> ~/.bashrc";
    private static String envR_HOME = "R_HOME";
    private static String envR_LIB = "R_LIB";
    private static String envProgramFilesWindows = "ProgramFiles";
    private static String envProgramFilesWindowsx86 = "ProgramFiles(x86)";
    final static String comando = "R CMD BATCH ";
    final static String comandoInstalarRJAVA
            = "if (!(\"rJava\" %in% rownames(installed.packages()))) "
            + "install.packages(\"rJava\") else print(paste((\"rJava\" "
            + "%in% rownames(installed.packages())), \"rjava is instaled!\"))";
    final static String key_installed_rjava = "TRUE rjava is instaled!";
    final static String extRout = ".Rout";
    final static String javareconf = "sudo R CMD javareconf";
    final static String jriDLLname = "jri.dll";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        print("configurando variavel de ambiente R_HOME", false);
        if (!tentaRenv(OSValidator.getOSType())) {
            print("não foi possivel configurar variavel de ambiente R_HOME", true);
            System.exit(-1);
        }else
            print("variavel R_HOME configurado.", false);

        print("instalando pacote rJava", false);
        if (!tentaRJava(OSValidator.getOSType())) {
            print("não foi possivel instalar o pacote rJava", true);
            System.exit(-2);
        }

        if (OSValidator.isWindows()) {
            try {
                print("copiando arquivos JRI", false);
                String rbim = getEnv(envR_HOME) + File.separator + "bin";
                String jri = getEnv(envR_HOME) + File.separator
                        + "library" + File.separator
                        + "rJava" + File.separator + "jri";

                if (copyJRIs(jri, "", rbim)) {

                    File folder = new File(jri);
                    File[] listOfFiles = folder.listFiles();

                    for (File listOfFile : listOfFiles) {
                        if (listOfFile.isDirectory()) {
                            copyJRIs(listOfFile.getAbsolutePath(), listOfFile.getName(), rbim);
                        }
                    }
                }
            } catch (Exception ex) {
                print("houve um erro enquanto copiava os arquivos JRI. detalhes: " + ex, true);
                System.exit(-3);
            }
            print("arquivos JRI copiados", false);
        }
        print("testando JRI", false);
        try {
            rconnector.RFacade fachada = rconnector.RFacade.getInstance(ConnectionType.LOCAL);
            int saida = fachada.getAsInt("1+1");
            print("\nresultado do teste 1+1=" + saida, false);
            print("finalizado com sucesso!", false);
        } catch (RException ex) {
            print("houve um erro enquanto testava rJava. detalhes: \n" + ex, true);
        }
        System.exit(0);
    }

    private static boolean copyJRIs(String path, String pathfrom, String pathto) {

        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile() && listOfFile.getName().equals(jriDLLname)) {
                File f = null;
                try {
                    Files.copy(
                            listOfFile.toPath(),
                            (f = new File(pathto 
                                    + File.separator 
                                    + pathfrom  
                                    + File.separator 
                                    + jriDLLname)).toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    print("não foi possivel copiar o arquivo "
                            + listOfFile.getAbsolutePath() + " para "
                            + f.getAbsolutePath() + " detalhes: \n" + ex, true);
                    return false;
                }
            }
        }
        
        return true;
        
        
    }

    private static void print(String text, boolean modoErr) {
        if (modoErr) {
            System.err.println(text);
        } else {
            System.out.println(text);
        }
    }

    private static boolean setEnvironment(boolean path, String var, OSType os, String nome) throws IOException {
        if (null == os) {
            return false;
        }
        if (path) {
            switch (os) {
                case WINDOWS:
                    Runtime.getRuntime().exec(pathWindows.replace("*", var));
                    break;
                case LINUX:
                    Runtime.getRuntime().exec(pathLinux.replace("*", var));
                    break;
                default:
                    return false;
            }
        } else {
            if (null == nome) {
                return false;
            }
            switch (os) {
                case WINDOWS:
                    Runtime.getRuntime().exec(envWindows.replace("*", var).replace("#", nome));
                    break;
                case LINUX:
                    Runtime.getRuntime().exec(envLinux.replace("*", var).replace("#", nome));
                    break;
                default:
                    return false;
            }
        }
        print("pode ser necessario reiniciar o computador", true);
        return true;
    }

    private static String getEnv(String nome) {
        return System.getenv(nome);
    }

    private static boolean tentaRenv(OSType os) {
        String renv = getEnv(envR_HOME);

        if (renv != null) {
            return true;
        }

        if (null == os) {
            print("OS não suportado. "
                    + "\nconfigure a variavel de ambiente R_HOME para o "
                    + "diretorio onde o R está instalado manualmente.", true);
            return false;
        } else {
            switch (os) {
                case WINDOWS: {
                    return rhome(getEnv(envProgramFilesWindows), getEnv(envProgramFilesWindowsx86), os);
                }
                case LINUX: {
                    String pf = "/usr/local/lib";
                    String pf2 = "/usr/local/lib64";
                    return rhome(pf, pf2, os);
                }
                default:
                    print("OS não suportado. "
                            + "\nconfigure a variavel de ambiente R_HOME para o "
                            + "diretorio onde o R está instalado manualmente.", true);
                    return false;
            }
        }
    }

    private static boolean tentaRJava(OSType so) {
        try {
            File f = File.createTempFile("script", "R");

            //print("Criando script em " + f.getAbsolutePath() + " conteudo " + comandoInstalarRJAVA, false);
            OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(f));
            os.write(comandoInstalarRJAVA, 0, comandoInstalarRJAVA.length());
            os.close();

            if (so == OSType.LINUX) {
                Runtime.getRuntime().exec(javareconf);
            }

            //print("Executando script instalar rJava, este processo pode demorar meia hora.", false);
            Process exec = Runtime.getRuntime().exec(comando + f.getAbsolutePath());

            exec.waitFor();

           // print("Verificando saida do script em " + f.getParent() + File.separator + f.getName() + extRout, false);
            Scanner sc = new Scanner(new FileReader(f.getParent() + File.separator + f.getName() + extRout));

            String saida = "";

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.contains(key_installed_rjava)) {
                    print("rJava esta instalado", false);
                    return true;
                }
                saida += line + "\n";
            }
            print("não foi possivel instalar o rJava, tente manualmente, detalhes: \n" + saida, true);
            return false;
        } catch (IOException ex) {
            print("nao foi possivel criar o arquivo de script temporario detalhes: \n " + ex, true);
        } catch (InterruptedException ex) {
            print("houve um erro enquanto instalava rJava. detalhes: \n " + ex, true);
        }
        return false;
    }

    private static boolean rhome(String pf, String pf2, OSType os) {
        print("tentano diretorio R em "
                + pf + " ou "
                + pf2, false);

        for (int k = 0; k < 2; k++) {
            File folder = new File(pf);
            File[] listOfFiles = folder.listFiles();

            for (File listOfFile : listOfFiles) {
                if (listOfFile.isDirectory()) {
                    if (listOfFile.getName().equals("R")) {
                        print("configurando variavel R_HOME=" + listOfFile.getAbsolutePath(), false);
                        String local = listOfFile.getName();
                        try {
                            setEnvironment(false, local, os, envR_HOME);
                            return true;
                        } catch (IOException ex) {
                            print("configure R_HOME manual ou tente como administrador. detalhes: " + ex, true);
                            return false;
                        }
                    }
                }
            }
            pf = pf2;
        }
        return false;
    }

}
