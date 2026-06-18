@echo off
title Generador de Ejecutable Comuneros POS
echo =======================================================
echo     GENERANDO APLICACION PORTABLE PARA WINDOWS (.EXE)
echo =======================================================
echo.

echo [+] Limpiando directorios anteriores...
if exist package-temp rd /s /q package-temp
if exist Comuneros-POS rd /s /q Comuneros-POS

echo [+] Preparando carpeta temporal de empaquetado...
mkdir package-temp

echo [+] Copiando JAR principal...
copy dist\Restaurante_comuneros.jar package-temp\

echo [+] Copiando librerias de dependencia...
copy librerias\*.jar package-temp\

echo [+] Compilando aplicacion nativa con JRE integrado...
jpackage --type app-image --name "Comuneros" --input package-temp --main-jar Restaurante_comuneros.jar --main-class restaurante.Restaurante --dest Comuneros-POS

echo.
echo =======================================================
echo  [OK] Proceso de empaquetado finalizado.
echo  Busque la carpeta 'Comuneros-POS\Comuneros'
echo  Ahi dentro encontrara 'Comuneros.exe'.
echo =======================================================
echo.

rd /s /q package-temp
pause
