#!/bin/bash
# Configura para publicación de cámara y sensor

# Configura la obtención de datos de los sensores
sudo cp locaviewer_blue.sh /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_blue.sh
sudo update-rc.d locaviewer_blue.sh defaults

# Configura la publicación de los datos de los sensores
sudo cp locaviewer_sensors.sh /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_sensors.sh
sudo update-rc.d locaviewer_sensors.sh defaults

# Configura la publicación de vídeo
sudo cp locaviewer_cams.sh /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_cams.sh
sudo update-rc.d locaviewer_cams.sh defaults
