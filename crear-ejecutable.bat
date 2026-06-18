@echo off
title Generador de Ejecutable Comuneros POS
echo =======================================================
echo     GENERANDO APLICACION PORTABLE PARA WINDOWS (.EXE)
echo =======================================================
echo.

:: Verificar si Java está instalado y en el PATH
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] No se encontro Java instalado o no esta en las variables de entorno.
    echo Por favor instale JDK 17 o superior para continuar.
    pause
    exit /b
)

:: Verificar si jpackage está disponible
where jpackage >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] No se encontro la herramienta 'jpackage'. 
    echo Asegurese de estar usando un JDK completo (version 17 o superior).
    pause
    exit /b
)

echo [+] Preparando carpeta temporal de empaquetado...
if exist package-temp rd /s /q package-temp
mkdir package-temp

:: Copiar el JAR principal
if not exist dist\Restaurante_comuneros.jar (
    echo [ERROR] No se encontro 'dist\Restaurante_comuneros.jar'. 
    echo Compile el proyecto primero en NetBeans o ejecute 'ant jar'.
    rd /s /q package-temp
    pause
    exit /b
)
copy dist\Restaurante_comuneros.jar package-temp\ > nul

:: Copiar todas las librerias de dependencia
if exist librerias (
    copy librerias\*.jar package-temp\ > nul
)

echo [+] Compilando aplicacion nativa de Windows con JRE integrado...
if exist Comuneros-POS rd /s /q Comuneros-POS

jpackage --type app-image ^
         --name "Comuneros" ^
         --input package-temp ^
         --main-jar Restaurante_comuneros.jar ^
         --main-class restaurante.Restaurante ^
         --dest Comuneros-POS ^
         --win-console

if %errorlevel% equ 0 (
    echo.
    echo =======================================================
    echo  [OK] ¡APLICACION GENERADA CON EXITO!
    echo =======================================================
    echo  Busque la carpeta 'Comuneros-POS\Comuneros'
    echo  Ahi dentro encontrara 'Comuneros.exe'.
    echo  Puede copiar esa carpeta a cualquier PC con Windows.
    echo =======================================================
) else (
    echo.
    echo [ERROR] Hubo un fallo al empaquetar con jpackage.
)

:: Limpiar archivos temporales
rd /s /q package-temp
pause
