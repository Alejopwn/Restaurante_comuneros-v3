package Modelo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoUpdater {

    public static final String CURRENT_VERSION = "1.1.5";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/Alejopwn/Restaurante_comuneros-v3/main/version.txt";

    public interface UpdateProgressCallback {
        void onProgress(String status, int percentage);
    }

    /**
     * Compara dos versiones semánticas (ej. "1.0.0" y "1.1.0").
     * Retorna verdadero si la remota es más nueva.
     */
    public static boolean isNewerVersion(String current, String remote) {
        try {
            String[] currentParts = current.split("\\.");
            String[] remoteParts = remote.split("\\.");
            int length = Math.max(currentParts.length, remoteParts.length);
            for (int i = 0; i < length; i++) {
                int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i].trim()) : 0;
                int remotePart = i < remoteParts.length ? Integer.parseInt(remoteParts[i].trim()) : 0;
                if (remotePart > currentPart) return true;
                if (remotePart < currentPart) return false;
            }
        } catch (Exception e) {
            return !current.equals(remote);
        }
        return false;
    }

    /**
     * Detecta la carpeta raíz de la aplicación portátil (jpackage).
     * Si no está en modo portátil, devuelve null.
     */
    private static File detectAppRoot() {
        try {
            String javaExePath = ProcessHandle.current().info().command().orElse("");
            File javaExe = new File(javaExePath);
            // Estructura jpackage: <AppRoot>/runtime/bin/java.exe → subir 3 niveles
            File candidate = javaExe.getParentFile() != null
                ? javaExe.getParentFile().getParentFile() != null
                    ? javaExe.getParentFile().getParentFile().getParentFile()
                    : null
                : null;
            if (candidate != null && new File(candidate, "app").exists()) {
                return candidate;
            }
        } catch (Exception e) {
            System.out.println("⚠️ No se pudo detectar appRoot: " + e.getMessage());
        }
        return null;
    }

    public static boolean checkAndApply(UpdateProgressCallback callback) {
        HttpURLConnection conn = null;
        try {
            if (callback != null) callback.onProgress("Buscando actualizaciones...", 10);

            // Detectar ruta raíz UNA SOLA VEZ para todo el proceso
            File appRoot = detectAppRoot();
            boolean isPackaged = appRoot != null;
            File workDir = isPackaged ? appRoot : new File(".");
            System.out.println("🔍 Directorio de trabajo: " + workDir.getAbsolutePath());
            System.out.println("🔍 ¿Modo portable? " + isPackaged);

            URL url = new URL(VERSION_URL + "?t=" + System.currentTimeMillis());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestMethod("GET");
            
            if (conn.getResponseCode() != 200) {
                System.out.println("❌ No se pudo obtener la información de versión remota (HTTP " + conn.getResponseCode() + ")");
                return false;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String remoteVersion = reader.readLine();
            String downloadUrl = reader.readLine();
            reader.close();

            if (remoteVersion == null || downloadUrl == null) {
                System.out.println("❌ Formato de versión inválido");
                return false;
            }

            remoteVersion = remoteVersion.trim();
            downloadUrl = downloadUrl.trim();

            System.out.println("🔍 Versión local: " + CURRENT_VERSION + " | Versión remota: " + remoteVersion);

            if (isNewerVersion(CURRENT_VERSION, remoteVersion)) {
                if (callback != null) callback.onProgress("¡Actualización encontrada (" + remoteVersion + ")!", 30);
                
                // Descargar en ruta ABSOLUTA dentro de la carpeta de la app
                File tempJar = new File(workDir, "update.tmp");
                System.out.println("📥 Descargando en: " + tempJar.getAbsolutePath());
                
                if (downloadFile(downloadUrl, tempJar, callback)) {
                    if (callback != null) callback.onProgress("Preparando reinicio...", 95);
                    launchUpdaterScript(appRoot, tempJar);
                    return true;
                }
            } else {
                System.out.println("✅ La aplicación está al día.");
            }
        } catch (IOException e) {
            System.out.println("⚠️ No hay conexión a internet o el servidor no responde: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    private static boolean downloadFile(String fileURL, File destination, UpdateProgressCallback callback) {
        HttpURLConnection httpConn = null;
        try {
            URL url = new URL(fileURL);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setConnectTimeout(5000);
            httpConn.setReadTimeout(15000);
            
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                int contentLength = httpConn.getContentLength();
                InputStream inputStream = httpConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(destination);

                byte[] buffer = new byte[4096];
                int bytesRead;
                int totalBytesRead = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    if (contentLength > 0 && callback != null) {
                        int percent = 30 + (int) ((totalBytesRead * 60.0) / contentLength);
                        callback.onProgress("Descargando actualización: " + (totalBytesRead / 1024) + " KB", percent);
                    }
                }

                outputStream.close();
                inputStream.close();
                System.out.println("📥 Archivo descargado exitosamente en: " + destination.getAbsolutePath());
                return true;
            } else {
                System.out.println("❌ Error de descarga. HTTP Código: " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("❌ Error al descargar el archivo: " + e.getMessage());
        } finally {
            if (httpConn != null) httpConn.disconnect();
        }
        return false;
    }

    private static void launchUpdaterScript(File appRoot, File tempJar) {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isPackaged = appRoot != null;
        File workDir = isPackaged ? appRoot : new File(".");

        System.out.println("🔍 Ruta raíz del instalador: " + workDir.getAbsolutePath());
        System.out.println("🔍 Archivo temporal: " + tempJar.getAbsolutePath());

        try {
            if (os.contains("win")) {
                // --- Windows: usar VBScript (más fiable que .bat para abrir procesos) ---
                File vbsFile = new File(workDir, "update.vbs");
                File logFile = new File(workDir, "update_log.txt");

                String jarDest, exePath;
                if (isPackaged) {
                    jarDest = new File(appRoot, "app\\Restaurante_comuneros.jar").getAbsolutePath();
                    exePath = new File(appRoot, "Comuneros.exe").getAbsolutePath();
                } else {
                    jarDest = new File(workDir, "dist\\Restaurante_comuneros.jar").getAbsolutePath();
                    exePath = "";
                }

                PrintWriter writer = new PrintWriter(new FileWriter(vbsFile));
                writer.println("' Actualizador automatico - Comuneros POS");
                writer.println("WScript.Sleep 5000");
                writer.println("Dim fso, shell");
                writer.println("Set fso   = CreateObject(\"Scripting.FileSystemObject\")");
                writer.println("Set shell = CreateObject(\"WScript.Shell\")");
                writer.println("On Error Resume Next");
                writer.println("fso.CopyFile \"" + tempJar.getAbsolutePath() + "\", \"" + jarDest + "\", True");
                writer.println("If Err.Number <> 0 Then");
                writer.println("  Dim logErr : Open \"" + logFile.getAbsolutePath() + "\" For Append As #1");
                writer.println("  Print #1, \"[ERROR] \" & Err.Description");
                writer.println("  Close #1");
                writer.println("Else");
                writer.println("  fso.DeleteFile \"" + tempJar.getAbsolutePath() + "\"");
                if (isPackaged) {
                    writer.println("  shell.Run \"\\\"" + exePath + "\"\\\"\", 1, False");
                } else {
                    writer.println("  shell.Run \"java -cp dist\\\\Restaurante_comuneros.jar;librerias\\\\* restaurante.Restaurante\", 1, False");
                }
                writer.println("End If");
                writer.println("fso.DeleteFile WScript.ScriptFullName");
                writer.close();

                Runtime.getRuntime().exec(new String[]{"wscript.exe", "//Nologo", vbsFile.getAbsolutePath()});

            } else {
                // --- Linux/macOS: usar script .sh ---
                File shFile = new File(workDir, "update.sh");
                File logFile = new File(workDir, "update_log.txt");

                String jarDest, exePath;
                if (isPackaged) {
                    jarDest = new File(appRoot, "app/Restaurante_comuneros.jar").getAbsolutePath();
                    exePath = new File(appRoot, "Comuneros").getAbsolutePath();
                } else {
                    jarDest = "dist/Restaurante_comuneros.jar";
                    exePath = "";
                }

                PrintWriter writer = new PrintWriter(new FileWriter(shFile));
                writer.println("#!/bin/sh");
                writer.println("echo \"[LOG] Iniciando actualizacion...\" > \"" + logFile.getAbsolutePath() + "\"");
                writer.println("sleep 4");
                writer.println("mv \"" + tempJar.getAbsolutePath() + "\" \"" + jarDest + "\" >> \"" + logFile.getAbsolutePath() + "\" 2>&1");
                if (isPackaged) {
                    writer.println("\"" + exePath + "\" &");
                } else {
                    writer.println("java -cp \"dist/Restaurante_comuneros.jar:librerias/*\" restaurante.Restaurante &");
                }
                writer.println("rm -- \"$0\"");
                writer.close();
                shFile.setExecutable(true);

                Runtime.getRuntime().exec(new String[]{"/bin/sh", shFile.getAbsolutePath()});
            }

            System.out.println("🔄 Script lanzado. Cerrando JVM...");
        } catch (Exception e) {
            System.out.println("❌ Error al lanzar el actualizador: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }
}
