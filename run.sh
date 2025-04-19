#!/bin/bash

# Compilar todos los archivos Java
javac *.java

# Verificar si la compilación fue exitosa
if [ $? -eq 0 ]; then
    echo "Compilación exitosa. Ejecutando..."
    java Evaluador
else
    echo "Error de compilación. Revisa tu código."
fi
