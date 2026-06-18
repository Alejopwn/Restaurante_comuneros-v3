package Modelo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AutoUpdater {

    public static final String CURRENT_VERSION = "1.0.2";
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

    public static boolean checkAndApply(UpdateProgressCallback callback) {
        HttpURLConnection conn = null;
        try {
            if (callback != null) callback.onProgress("Buscando actualizaciones...", 10);
            
            URL url = new URL(VERSION_URL);
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
                
                // Descargar el nuevo JAR como archivo temporal
                File tempJar = new File("update.tmp");
                if (downloadFile(downloadUrl, tempJar, callback)) {
                    if (callback != null) callback.onProgress("Preparando reinicio...", 95);
                    
                    // Lanzar el script del sistema y salir
                    launchUpdaterScript();
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

    private static void launchUpdaterScript() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Verificar si se está ejecutando desde la versión portable (.exe)
                boolean isPackaged = new File("Comuneros.exe").exists() && new File("app").exists();
                
                File batFile = new File("update.bat");
                PrintWriter writer = new PrintWriter(new FileWriter(batFile));
                writer.println("@echo off");
                writer.println("echo Esperando a que el sistema POS se cierre...");
                writer.println("ping 127.0.0.1 -n 3 > nul");
                writer.println("echo Aplicando actualizacion...");
                
                if (isPackaged) {
                    writer.println("move /y update.tmp app\\Restaurante_comuneros.jar > nul");
                    writer.println("echo Reiniciando el sistema...");
                    writer.println("start \"\" Comuneros.exe");
                } else {
                    writer.println("move /y update.tmp dist\\Restaurante_comuneros.jar > nul");
                    writer.println("echo Reiniciando el sistema...");
                    writer.println("start \"\" java -cp \"dist\\Restaurante_comuneros.jar;librerias\\*\" restaurante.Restaurante");
                }
                
                writer.println("del \"%~f0\"");
                writer.close();
                
                Runtime.getRuntime().exec("cmd /c start /b update.bat");
            } else {
                // Generar script .sh para Linux/macOS
                boolean isPackaged = new File("Comuneros").exists() && new File("app").exists();
                
                File shFile = new File("update.sh");
                PrintWriter writer = new PrintWriter(new FileWriter(shFile));
                writer.println("#!/bin/sh");
                writer.println("sleep 2");
                
                if (isPackaged) {
                    writer.println("mv update.tmp app/Restaurante_comuneros.jar");
                    writer.println("./Comuneros &");
                } else {
                    writer.println("mv update.tmp dist/Restaurante_comuneros.jar");
                    writer.println("java -cp \"dist/Restaurante_comuneros.jar:librerias/*\" restaurante.Restaurante &");
                }
                
                writer.println("rm -- \"$0\"");
                writer.close();
                
                // Hacer ejecutable el .sh
                shFile.setExecutable(true);
                Runtime.getRuntime().exec("/bin/sh ./update.sh");
            }
            
            // Cerrar la aplicación actual para liberar el JAR
            System.out.println("🔄 Lanzando instalador en segundo plano y cerrando JVM...");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("❌ Error al lanzar el actualizador externo: " + e.getMessage());
        }
    }
}
