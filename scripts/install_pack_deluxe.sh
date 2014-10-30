#!/bin/bash
# Configura para publicación de cámara y sensor
touch ~/locaviewer.log

# Configura la obtención de datos de los sensores
sudo cp locaviewer_blue /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_blue
sudo update-rc.d locaviewer_blue defaults

# Configura la publicación de los datos de los sensores
sudo cp locaviewer_sensors /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_sensors
sudo update-rc.d locaviewer_sensors defaults

# Configura la publicación de vídeo
sudo cp locaviewer_cams /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_cams
sudo update-rc.d locaviewer_cams defaults
