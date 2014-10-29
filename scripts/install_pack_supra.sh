#!/bin/bash
# Configura para publicación de sensores y servidor

# Configura la obtención de datos de los sensores
sudo cp locaviewer_blue.sh /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_blue.sh
sudo update-rc.d locaviewer_blue.sh defaults

# Configura la publicación de los datos de los sensores
sudo cp locaviewer_sensors.sh /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_sensors.sh
sudo update-rc.d locaviewer_sensors.sh defaults

# Configura la realización de triangulación
sudo cp locaviewer_servidor.sh /etc/init.d/
sudo chmod +x /etc/init.d/locaviewer_servidor.sh
sudo update-rc.d locaviewer_servidor.sh defaults
