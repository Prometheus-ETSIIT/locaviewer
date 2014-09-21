![Logo Locaviewer](https://sourceforge.net/p/locaviewer/code/ci/master/tree/logo.png "Logo Locaviewer")

**Locaviewer** es un sistema de vídeo distribuido creado por estudiantes de *Grado en Ingenería Informática* y *Grado en Ingenería de Tecnologías de Telecomunicación* de la Universidad de Granada (UGR), España. Este proyecto se llevó a cabo con motivo del [**IV Desafío Tecnológico organizado**](http://etsiit.ugr.es/pages/IV_desafio_tecnologico) en la ETSIIT de la UGR.

El equipo se llama **Prometheus** y está formado por los siguientes componentes:

+ Nicolás Guerrero García
+ Israel Blancas Álvarez
+ Ignacio Cara Martín
+ Benito Palacios Sánchez

## Notas de desarrollo
Los programas están desarrollados en **Java**, **Python** y **Matlab / Octave**. Para el caso de Java se han usando los IDE *Net Beans* y *Eclipse*.
En cada programa existe un script de ejecución (*run.sh*) y otro de compilación (*build.sh*) para que no sean necesarios los IDE. Para que estos scripts funcionen correctamente tu ordenador deberá cumplir lo siguiente:

+ El sistema operativo tiene que estar basado en **GNU/Linux**.
+ En la carpeta *raíz del proyecto* (donde se encuentra este archivo) deberás colocar la **licencia de RTI Connext**.
+ El usuario ha de tener la variable de entorno **RTI_CONNEXT_PATH** apuntando a la carpeta *RTI* (en mayúscula, la que contiene *ndds5.1.0* con las bibliotecas). Puedes poner esta variable en el fichero *.bashrc* de tu carpeta personal o similar.

### Instrucciones para Net Beans
Para aquellos programas que han sido desarrollados en *Net Beans* es necesario que antes de abrirlos configures el IDE de la siguiente forma:
Ve al menú *Tools* -> *Ant Variables* y añade la una variable con el nombre *RTI_CONNEXT_PATH* y que apunte a la misma ruta que la variable de entorno previamente configurada.