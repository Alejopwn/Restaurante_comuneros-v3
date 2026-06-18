#!/bin/bash
set -e

echo "========================================================="
echo "   COMPILADOR DE WINDOWS .EXE DESDE LINUX (LAUNCH4J)"
echo "========================================================="
echo

# 1. Asegurar que existe el JAR más reciente
if [ ! -f "dist/Restaurante_comuneros.jar" ]; then
    echo "[!] No se encontro el archivo JAR en dist/. Compilando primero..."
    ant clean compile jar
fi

# 2. Descargar Launch4j si no está descargado
LAUNCH4J_DIR="build/launch4j_bin"
if [ ! -d "$LAUNCH4J_DIR" ]; then
    echo "[+] Descargando Launch4j para Linux..."
    mkdir -p build
    curl -L -o build/launch4j.tgz "https://sourceforge.net/projects/launch4j/files/launch4j-3/3.50/launch4j-3.50-linux-x64.tgz/download"
    echo "[+] Descomprimiendo Launch4j..."
    tar -xzf build/launch4j.tgz -C build/
    mv build/launch4j "$LAUNCH4J_DIR"
    rm build/launch4j.tgz
fi

# 3. Dar permisos al binario de Launch4j
chmod +x "$LAUNCH4J_DIR/launch4j"
chmod +x "$LAUNCH4J_DIR/bin/windres" 2>/dev/null || true
chmod +x "$LAUNCH4J_DIR/bin/ld" 2>/dev/null || true

echo "[+] Compilando executable de Windows 'ComunerosPOS.exe'..."
java -jar "$LAUNCH4J_DIR/launch4j.jar" launch4j_config.xml

echo
echo "========================================================="
echo "   [OK] ¡PROCESO COMPLETADO EXITOSAMENTE!"
echo "   Se ha creado el archivo: ComunerosPOS.exe"
echo "========================================================="
echo
