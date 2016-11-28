/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package autoconfigurerjava;

/**
 *
 * @author Miquéias Fernandes
 */
public class OSValidator {

    private static String OS = System.getProperty("os.name").toLowerCase();
    private static String OSarquiteture = System.getProperty("os.arch").toLowerCase();

    public static OSType getOSType() {
        if (isWindows()) {
            return OSType.WINDOWS;
        } else if (isMac()) {
            return OSType.MAC;
        } else if (isUnix()) {
            return OSType.LINUX;
        } else if (isSolaris()) {
            return OSType.OTHER;
        } else {
            return OSType.OTHER;
        }
    }

    public static OSArq getOSArq() {
        if (OSarquiteture.contains("64")) {
            return OSArq.X64;
        }
        if (OSarquiteture.contains("86")) {
            return OSArq.X86;
        }
        return OSArq.OTHER;
    }

    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0);
    }

    public static boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    public static String getOS() {
        if (isWindows()) {
            return "win";
        } else if (isMac()) {
            return "osx";
        } else if (isUnix()) {
            return "uni";
        } else if (isSolaris()) {
            return "sol";
        } else {
            return "err";
        }
    }

}
