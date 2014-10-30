#!/bin/bash
# Configura para publicación de sensores y servidor
touch ~/locaviewer.log

# Configura la obtención de datos de los sensores
sudo cp locaviewer_blue /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_blue
sudo update-rc.d locaviewer_blue defaults

# Configura la publicación de los datos de los sensores
sudo cp locaviewer_sensors /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_sensors
sudo update-rc.d locaviewer_sensors defaults

# Configura la realización de triangulación
sudo cp locaviewer_servidor /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_servidor
sudo update-rc.d locaviewer_servidor defaults
