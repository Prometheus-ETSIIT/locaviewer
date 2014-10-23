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

### Configuración previa
Para el funcionamiento de los sensores *Bluetooth* se necesita el paquete *bluetooth*.
En sistemas operativos basados en *Debian* usa el siguiente comando:

``` shell
sudo apt-get install bluetooth python-bluez
```

Para compilar los programas se necesita *ant*. En sistemas basados en *Debian* usa:

``` shell
sudo apt-get install ant
```

Para ejecutar el publicador de vídeo (Gava) hace falta tener instalado GStreamer 0.10
en sistemas operativos basados en *Debian* utiliza el siguiente comando.

``` shell
sudo apt-get install libgstreamer0.10-0 gstreamer0.10-plugins-good gstreamer0.10-plugins-bad
```

Además, en el caso de *Debian* se necesita crear enlaces simbólicos ya que el nombre
de los paquetes termina en *-0*. Utiliza los siguientes comandos (preparados para
*Raspberry Pi*):

``` shell
sudo ln -s /lib/arm-linux-gnueabihf/libglib-2.0.so.0 /lib/arm-linux-gnueabihf/libglib-2.0.so

sudo ln -s /usr/lib/arm-linux-gnueabihf/libgstreamer-0.10.so.0 /usr/lib/arm-linux-gnueabihf/libgstreamer-0.10.so

sudo ln -s /usr/lib/arm-linux-gnueabihf/libgobject-2.0.so.0 /usr/lib/arm-linux-gnueabihf/libgobject-2.0.so

sudo ln -s /usr/lib/arm-linux-gnueabihf/libgstinterfaces-0.10.so.0 /usr/lib/arm-linux-gnueabihf/libgstinterfaces-0.10.so

sudo ln -s /usr/lib/arm-linux-gnueabihf/libgstbase-0.10.so.0 /usr/lib/arm-linux-gnueabihf/libgstbase-0.10.so

sudo ln -s /usr/lib/arm-linux-gnueabihf/libgstapp-0.10.so.0 /usr/lib/arm-linux-gnueabihf/libgstapp-0.10.so
```

NOTA: Si se usa una antena Wi-Fi hay que instalar los drivers de ésta también. En nuestro caso se hizo
con el siguiente comando:

``` shell
sudo apt-get install zd1211-firmware
```

Además, se ha tenido que cambiar la ruta por defecto (0.0.0.0) para que use la interfaz *wlan0*:

``` shell
sudo route add default gw 192.168.3.1 wlan0
sudo route del default gw 192.168.3.1 eth0
```

### Instrucciones para Net Beans
Para aquellos programas que han sido desarrollados en *Net Beans* es necesario que antes de abrirlos configures el IDE de la siguiente forma:
Ve al menú *Tools* -> *Ant Variables* y añade la una variable con el nombre *RTI_CONNEXT_PATH* y que apunte a la misma ruta que la variable de entorno previamente configurada.


## Generar certificados servidor
Para generar los tres archivos de certificados utilizados en el servidor se puede
consultar la siguiente web: http://docs.oracle.com/cd/E19798-01/821-1841/gjrgy/index.html 
